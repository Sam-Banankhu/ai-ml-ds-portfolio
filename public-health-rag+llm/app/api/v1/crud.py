from datetime import datetime, timedelta
from fastapi import HTTPException, status
# from grpc import Status
from sqlalchemy.orm import Session
from app.api.v1.models import (
    Handout, User, Role, ChatHistory, FAQ, District, Facility, Section
)
from app.api.v1.schemas import (
    HSACreateRequest, HandoutCreate, SectionCreate, UserCreate, ChatCreate, ChatHistoryCreate, DistrictCreate,
    FacilityCreate, UserRole
)
from passlib.context import CryptContext
from uuid import uuid4
from pydantic import UUID4
from sqlalchemy.exc import IntegrityError

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")




def get_user_by_email(db: Session, email: str):
    return db.query(User).filter(User.email == email).first()

def get_user_by_phone(db: Session, phone: str):
    return db.query(User).filter(User.phone == phone).first()

def create_user(db: Session, user: UserCreate):
    role = db.query(Role).filter(Role.name == user.role.lower()).first()
    if not role:
        raise ValueError(f"Role '{user.role}' Does not exist.")

    hashed_password = pwd_context.hash(user.password)

    db_user = User(
        id=uuid4(),
        fullname=user.fullname.title(),
        email=user.email.lower(),
        phone=user.phone,
        password=hashed_password,
        role=role
    )

    if user.role == UserRole.HSA:
        if not user.facility_id:
            raise ValueError("facility_id is required for HSAs.")
        db_user.facility_id = user.facility_id

    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user




def create_hsa_account(db: Session, hsa_data: HSACreateRequest, otp: str):
    # Check if the user already exists by email
    existing_user = db.query(User).filter(User.email == hsa_data.email).first()
    if existing_user:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User with this email already exists")

    # Set OTP expiration time (e.g., 10 minutes from now)
    otp_expiry_time = datetime.now() + timedelta(minutes=100)

    # Create the new HSA account
    new_hsa = User(
        id=uuid4(),
        fullname=hsa_data.fullname.title(),
        email=hsa_data.email.lower(),
        phone = hsa_data.phone if hsa_data.phone else "",
        password=None,  # No password at the moment since OTP is being used for account claiming
        facility_id=hsa_data.facility_id,
        role_id=db.query(Role).filter(Role.name == "hsa").first().id,
        otp=otp,  # Store OTP in the otp field
        otp_expires_at=otp_expiry_time  # Store the expiration time
    )

    # Add the new user to the database
    db.add(new_hsa)
    db.commit()
    db.refresh(new_hsa)

    return new_hsa


def create_instructor_account(db: Session, instructor_data: UserCreate, otp: str):
    # Check if the user already exists by email
    existing_user = db.query(User).filter(User.email == instructor_data.email).first()
    if existing_user:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User with this email already exists")

    # Set OTP expiration time (e.g., 10 minutes from now)
    otp_expiry_time = datetime.now() + timedelta(minutes=100)

    # Create the new instructor account
    new_instructor = User(
        id=uuid4(),
        fullname=instructor_data.fullname.title(),
        email=instructor_data.email.lower(),
        phone = instructor_data.phone if instructor_data.phone else "",
        password=None, 
        role_id=db.query(Role).filter(Role.name == "instructor").first().id,
        otp=otp,  # Store OTP in the otp field
        otp_expires_at=otp_expiry_time  # Store the expiration time
    )

    # Add the new instructor to the database
    db.add(new_instructor)
    db.commit()
    db.refresh(new_instructor)

    return new_instructor

def create_admin_account(db: Session, admin_data: UserCreate, otp: str):
    # Check if the user already exists by email
    existing_user = db.query(User).filter(User.email == admin_data.email).first()
    if existing_user:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User with this email already exists")

    # Set OTP expiration time (e.g., 10 minutes from now)
    otp_expiry_time = datetime.now() + timedelta(minutes=100)

    # Create the new admin account
    new_admin = User(
        id=uuid4(),
        fullname=admin_data.fullname.title(),
        email=admin_data.email.lower(),
        phone = admin_data.phone if admin_data.phone else "",
        password=None, 
        role_id=db.query(Role).filter(Role.name == "admin").first().id,
        otp=otp,  # Store OTP in the otp field
        otp_expires_at=otp_expiry_time  # Store the expiration time
    )

    # Add the new admin to the database
    db.add(new_admin)
    db.commit()
    db.refresh(new_admin)

    return new_admin



def create_chat_history(db: Session, chat_history: ChatHistoryCreate, section_id: UUID4, user_id: UUID4):
    db_chat_history = ChatHistory(
        id=uuid4(),
        question=chat_history.question,
        response=chat_history.response,
        section_id=section_id,
        user_id=user_id  
    )
    db.add(db_chat_history)
    db.commit()
    db.refresh(db_chat_history)
    return db_chat_history


# def get_chats(db: Session, user_id: UUID4):
#     return db.query(Chat).filter(Chat.user_id == user_id).all()

# def get_chat(db: Session, chat_id: UUID4):
#     return db.query(Chat).filter(Chat.id == chat_id).first()

# def delete_chat(db: Session, chat_id: UUID4):
#     chat = db.query(Chat).filter(Chat.id == chat_id).first()
#     if chat:
#         db.delete(chat)
#         db.commit()
#     return chat

def get_chat_histories(db: Session, section_id: UUID4):
    return db.query(ChatHistory).filter(ChatHistory.section_id == section_id).all()


def delete_chat_history(db: Session, chat_history_id: UUID4):
    chat_history = db.query(ChatHistory).filter(ChatHistory.id == chat_history_id).first()
    if chat_history:
        db.delete(chat_history)
        db.commit()
    return chat_history

def create_district(db: Session, district: DistrictCreate):
    existing_district = db.query(District).filter_by(id=district.id).first()
    if existing_district:
        return existing_district

    db_district = District(
        id=district.id,
        name=district.name
    )
    db.add(db_district)
    try:
        db.commit()
        db.refresh(db_district)
    except IntegrityError:
        db.rollback()
        raise
    return db_district

def create_facility(db: Session, facility: FacilityCreate):
    db_facility = Facility(
        id=facility.id,
        name=facility.name,
        facility_type=facility.facility_type,
        managing_authority=facility.managing_authority,
        urban_rural=facility.urban_rural,
        district_id=facility.district_id
    )
    db.add(db_facility)
    db.commit()
    db.refresh(db_facility)
    return db_facility


# Get all sections
def get_sections(db: Session):
    return db.query(Section).all()

# Get section by ID
def get_section(db: Session, section_id: UUID4):
    return db.query(Section).filter(Section.id == section_id).first()

# Create new section (optional)
def create_section(db: Session, section: SectionCreate):
    db_section = Section(name=section.name)
    db.add(db_section)
    db.commit()
    db.refresh(db_section)
    return db_section

# Update section (optional)
def update_section(db: Session, section_id: UUID4, section: SectionCreate):
    db_section = db.query(Section).filter(Section.id == section_id).first()
    if db_section:
        db_section.name = section.name
        db.commit()
        db.refresh(db_section)
    return db_section

# Delete section (optional)
def delete_section(db: Session, section_id: UUID4):
    db_section = db.query(Section).filter(Section.id == section_id).first()
    if db_section:
        db.delete(db_section)
        db.commit()
    return db_section

def get_faqs_by_district(db: Session, district_id: UUID4):
    return db.query(FAQ).join(User).join(Facility).filter(Facility.district_id == district_id).all()

def get_faqs_by_facility(db: Session, facility_id: UUID4):
    return db.query(FAQ).filter(FAQ.hsa_id == User.id, User.facility_id == facility_id).all()


# Get all users
def get_users(db: Session):
    return db.query(User).all()


# Get user by ID
def get_user(db: Session, user_id: UUID4):
    return db.query(User).filter(User.id == user_id).first()


# Update user
def update_user(db: Session, user_id: UUID4, user_update: UserCreate):
    user = db.query(User).filter(User.id == user_id).first()
    if user:
        user.fullname = user_update.fullname
        user.email = user_update.email
        user.phone = user_update.phone
        user.role_id = db.query(Role).filter(Role.name == user_update.role.lower()).first().id
        user.facility_id = user_update.facility_id  # Update facility ID if applicable
        db.commit()
        db.refresh(user)
    return user


# Delete user
def delete_user(db: Session, user_id: UUID4):
    user = db.query(User).filter(User.id == user_id).first()
    if user:
        db.delete(user)
        db.commit()
    return user



# Get user by ID
def get_user(db: Session, user_id: UUID4):
    return db.query(User).filter(User.id == user_id).first()


# # Update user's full name and phone number
# def update_user_fullname_phone(db: Session, user_id: UUID4, fullname: str, phone: str):
#     user = db.query(User).filter(User.id == user_id).first()
#     if user:
#         user.fullname = fullname.title()
#         user.phone = phone
#         db.commit()
#         db.refresh(user)
#     return user


# Update user's password
def update_user_password(db: Session, user_id: UUID4, new_password: str):
    user = db.query(User).filter(User.id == user_id).first()
    if user:
        hashed_password = pwd_context.hash(new_password)
        user.password = hashed_password
        db.commit()
        db.refresh(user)
    return user


def verify_and_update_user_password(db: Session, user_id: UUID4, old_password: str, new_password: str):
    user = db.query(User).filter(User.id == user_id).first()

    if not user:
        return None

    # Verify old password
    if not pwd_context.verify(old_password, user.password):
        return None

    # Hash the new password and update
    user.password = pwd_context.hash(new_password)
    db.commit()
    db.refresh(user)
    return user



def update_user_info_in_db(
    db: Session, user_id: int, fullname: str, email: str, phone: str
) -> User:
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        return None
    user.fullname = fullname
    user.email = email
    user.phone = phone
    db.commit()
    db.refresh(user)
    return user




# Create a new handout
def create_handout(db: Session, handout: HandoutCreate, user_id: UUID4):
    new_handout = Handout(
        title=handout.title,
        content=handout.content,
        created_by_id=user_id,
        section_id=handout.section_id,
        created_at=datetime.now()
    )
    db.add(new_handout)
    db.commit()
    db.refresh(new_handout)
    return new_handout

# Get recent handouts for a specific user
def get_recent_handouts(db: Session, user_id: UUID4, limit: int = 5):
    return db.query(Handout).filter(Handout.created_by_id == user_id).order_by(Handout.created_at.desc()).limit(limit).all()


# Delete a handout
def delete_handout(db: Session, handout_id: UUID4, user_id: UUID4):
    # Find the handout by ID
    handout = db.query(Handout).filter(Handout.id == handout_id).first()

    if handout is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Handout not found")

    # Check if the user is authorized to delete the handout (instructor/admin)
    if handout.created_by_id != user_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not authorized to delete this handout")

    # Delete the handout
    db.delete(handout)
    db.commit()

    return {"detail": "Handout deleted successfully"}