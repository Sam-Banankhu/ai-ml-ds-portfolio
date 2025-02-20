from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import random
import smtplib
import string
from fastapi import Depends, HTTPException, status
from sqlalchemy.orm import Session
from fastapi.security import OAuth2PasswordBearer
from app.api.v1.crud import get_user_by_email
from passlib.context import CryptContext
# import jwt
# from jwt import 
from datetime import timedelta, datetime
from pydantic import UUID4
import os
from app.database import get_db
from app.api.v1.models import ChatHistory, Section,User
from app.api.v1.schemas import EmailSendRequest, Users
from langchain_chroma import Chroma
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from app.config import settings
from app.config import embedding, llm, vector_store
from dotenv import load_dotenv
from authlib.jose import jwt
# Load environment variables
load_dotenv()

# router = APIRouter()


# Constants and security configurations
SECRET_KEY = settings.JWT_SECRET_KEY
ALGORITHM = settings.ALGORITHM
ACCESS_TOKEN_EXPIRE_MINUTES = settings.ACCESS_TOKEN_EXPIRE_MINUTES
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
header = {'alg': ALGORITHM}


def create_access_token(data: dict, expires_delta: timedelta = None):
    payload = data.copy()
    if expires_delta:
        expire = datetime.now() + expires_delta
    else:
        expire = datetime.now() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload.update({"exp": expire})
    try:
        encoded_jwt = jwt.encode(header,payload,SECRET_KEY)
        # print(f"Encoded JWT: {encoded_jwt}")  # Add this for debugging
    except Exception as e:
        print(f"Error encoding JWT: {e}")
        raise
    return encoded_jwt


# Function to get the current user from the token
def get_current_user(db: Session = Depends(get_db), token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY)
        # print(f"Decoded JWT: {payload}")  # Add this for debugging
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except Exception as e:
        raise credentials_exception
    user = get_user_by_email(db, email=email)
    if user is None:
        raise credentials_exception
    return user


# Role-based access control
def check_is_instructor(current_user: Users):
    if current_user.role.name == 'hsa':
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You do not have permission to access this resource."
        )
        

def check_is_admin(user: Users):
    if user.role.name != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, 
            detail="You are not authorized to perform this action"
        )
        
        
def generate_handout_title(query_text: str) -> str:
    """
    Generate a concise and descriptive title based on the query text asynchronously.
    """
    title_template = """
    You are tasked to create handout title based on the following query for a training handout:
    "{context}"
    Provide a short, clear, and descriptive title.
    Must use normal grammer.
    Must be in line with the query.
    Do not enclose the title in quotes or any symbol or special characters.
    """
    title_prompt = PromptTemplate(
        template=title_template, input_variables=["context"]
    )

    # Generate title using LLM with title-specific prompt asynchronously
    title_qa = RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=vector_store.as_retriever(search_kwargs={'k': 3}),
        chain_type_kwargs={"prompt": title_prompt}
    )
    response = title_qa.invoke(query_text)
    title = response.get('result')
    
    # Ensure fallback title if LLM does not return result
    if not title:
        title = "Untitled Handout"
    print(title)
    return title
   


PROMPT_TEMPLATE = """Use the following pieces of context to answer the question at the end. 
If you don't know the answer, just say Sorry, I am unable to give you that information. Don't try to make up an answer.

{context}

Question: {question}
Provide a concise answer in 1-4 sentences:"""

PROMPT = PromptTemplate(
    template=PROMPT_TEMPLATE, input_variables=["context", "question"]
)



def validate_section(section_id: UUID4, db: Session):
    """Ensure the section ID is valid."""
    section = db.query(Section).filter_by(id=section_id).first()
    if section is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Section not found")
    return section


def retrieve_recent_chats(user_id: UUID4, section_id: UUID4, db: Session, limit: int = 5):
    """Retrieve the most recent chat history for the user in ascending order."""
    recent_chats = db.query(ChatHistory).filter_by(user_id=user_id, section_id=section_id)\
        .order_by(ChatHistory.timestamp.desc()).limit(limit).all()

  
    return recent_chats[::-1]


def setup_qa_chain(section_id: UUID4):
    """Set up the Retrieval QA chain for answering questions."""
    return RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=vector_store.as_retriever(
            search_type="similarity_score_threshold",
            search_kwargs={'k': 5,
                           "score_threshold": 0.8, 
                           'filter': {"metadata": {"$eq": str(section_id)}}
                           }
            ),
        chain_type_kwargs={"prompt": PROMPT}
    )


def generate_otp():
    return ''.join(random.choices(string.ascii_letters, k=6))

def verify_otp(user, otp):
    # Check if OTP matches and is not expired
    # print(f"user otp is {user.otp}")
    if user.otp == otp and user.otp_expires_at > datetime.now():
        return True
    return False

def hash_password(password: str):
    return pwd_context.hash(password)


def send_reset_password_otp_to_email(email_data: EmailSendRequest, db: Session):
    # Fetch the user by email
    user = db.query(User).filter(User.email == email_data.email).first()
    
    if user:
        sender_email = "smartcode265@gmail.com"
        
        # Set content for password reset or account verification

        subject = "Password Reset Request"
        body = f"""
        <html>
        <body>
            <h2>Dear {user.fullname},</h2>
            <p>We received a request to reset your password. Use the OTP below to reset your password:</p>
            <p style="font-weight: bold; font-size: 20px;">{email_data.otp}</p>
            <p>If you did not request a password reset, please ignore this email.</p>
            <p>Best regards,<br>Your Support Team</p>
        </body>
        </html>
        """
        # Set up the email details
        msg = MIMEMultipart()
        msg["Subject"] = subject
        msg["From"] = sender_email
        msg["To"] = email_data.email

        # Attach the HTML body to the email
        msg.attach(MIMEText(body, "html"))

        # Send email using SMTP
        try:
            with smtplib.SMTP("smtp.gmail.com", 587) as server:
                server.starttls()
                server.login(sender_email, settings.GMAIL_PASSWORD)
                server.sendmail(sender_email, email_data.email, msg.as_string())
        except Exception as e:
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Failed to send OTP email")

        return {"detail": f"OTP sent to {user.fullname} successfully"}
    
    else:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
   

def send_otp_to_email(email_data: EmailSendRequest, db: Session):
    # Fetch the user by email
    user = db.query(User).filter(User.email == email_data.email).first()
    
    if user:
        # Determine the subject and email body based on the user's role
        sender_email = "smartcode265@gmail.com"
        
        # Set the email content according to the user's role
        if user.role.name == "hsa":
            subject = "Verify Your HSA Account"
            body = f"""
            <html>
            <body>
                <h2>Dear {user.fullname}!</h2>
                <p>Your account has been created by the HSA Team. To claim your HSA account, please use the following One-Time Password (OTP):</p>
                <p style="font-weight: bold; font-size: 20px;">{email_data.otp}</p>
                <p>Please use this OTP to set your account password on the Android app provided by the HSA officials.</p>
                <p>If you have any questions or need assistance, feel free to reach out to the HSA support team.</p>
                <p>Best regards,<br>The HSA Team</p>
            </body>
            </html>
            """
        elif user.role.name == "instructor":
            subject = "Verify Your Instructor Account"
            body = f"""
            <html>
            <body>
                <h2>Dear {user.fullname}!</h2>
                <p>Your account has been created by the Instructor Team. To claim your instructor account, please use the following One-Time Password (OTP):</p>
                <p style="font-weight: bold; font-size: 20px;">{email_data.otp}</p>
                <p>Please use this OTP to set your account password in the system.</p>
                <p>If you have any questions or need assistance, feel free to reach out to the support team.</p>
                <p>Best regards,<br>The Instructor Team</p>
            </body>
            </html>
            """
        elif user.role.name == "admin":
            subject = "Verify Your Admin Account"
            body = f"""
            <html>
            <body>
                <h2>Dear {user.fullname}!</h2>
                <p>Your account has been created by the Admin Team. To claim your admin account, please use the following One-Time Password (OTP):</p>
                <p style="font-weight: bold; font-size: 20px;">{email_data.otp}</p>
                <p>Please use this OTP to set your account password in the system.</p>
                <p>If you have any questions or need assistance, feel free to reach out to the admin support team.</p>
                <p>Best regards,<br>The Admin Team</p>
            </body>
            </html>
            """
        else:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid user role")

        # Set up the email details
        msg = MIMEMultipart()
        msg["Subject"] = subject
        msg["From"] = sender_email
        msg["To"] = email_data.email

        # Attach the HTML body to the email
        msg.attach(MIMEText(body, "html"))

        # Send email using SMTP
        try:
            with smtplib.SMTP("smtp.gmail.com", 587) as server:
                server.starttls()
                server.login(sender_email, settings.GMAIL_PASSWORD)
                server.sendmail(sender_email, email_data.email, msg.as_string())
        except Exception as e:
            raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Failed to send OTP email")

        return {"detail": f"OTP sent to {user.fullname} successfully"}
    
    else:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")




def send_account_verified_email(email: str, fullname: str):
    sender_email = "smartcode265@gmail.com"
    subject = "Account Verified Successfully"
    html_message = f"""
    <html>
        <body>
            <h2>Hi {fullname},</h2>
            <p>Your account has been <strong>successfully verified</strong>. You can now log in using your new password.</p>
            <p>Please remember to keep your password safe and secure. If you ever forget it, you can reset it using the 'Forgot Password' feature on the login page.</p>
            <br>
            <p>Best regards,<br>The HSA Team</p>
        </body>
    </html>
    """
            # Set up email details
    msg = MIMEMultipart()
    msg["Subject"] = subject
    msg["From"] = sender_email
    msg["To"] = email
    msg.attach(MIMEText(html_message, "html"))
   

    # Send email using SMTP 
    try:
        with smtplib.SMTP("smtp.gmail.com", 587) as server:
            server.starttls()
            server.login(sender_email, settings.GMAIL_PASSWORD)
            server.sendmail(sender_email, email, msg.as_string())
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Failed to send notification email")

    return {"detail": f"Notification sent to {fullname} successfully"}