from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from fastapi.responses import HTMLResponse
from app.api.v1.schemas import ClaimAccountRequest, UserCreate, LoginRequest, TokenResponse, VerifyOTP
from app.api.v1.crud import get_user_by_email, create_user, get_user_by_phone
from app.api.v1.models import Facility, User
from app.database import get_db
from app.api.v1.utils import create_access_token, hash_password, pwd_context, send_account_verified_email, verify_otp
from datetime import datetime, timedelta
from app.config import settings

router = APIRouter()

@router.get("/", response_class=HTMLResponse)
def read_auth_documentation():
    with open("./docs/location.md", "r") as file:
        readme_content = file.read()
    # html_content = markdown.markdown(readme_content)
    return HTMLResponse(content=readme_content)


@router.post("/signup/", status_code=status.HTTP_201_CREATED, response_model=TokenResponse)
def signup(user: UserCreate, db: Session = Depends(get_db)):
    # Check if email or phone is already registered
    db_user = get_user_by_email(db, email=user.email)
    db_phone = get_user_by_phone(db, phone=user.phone)
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )
    if db_phone:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Phone already registered"
        )

    try:
        # Create new user and assign the facility (if applicable)
        new_user = create_user(db, user)

        # Convert role to string if it's an object or enum
        role_name = new_user.role.name if hasattr(new_user.role, 'name') else str(new_user.role)

        # Create a JWT token for the newly created user
        access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        access_token = create_access_token(
            data={
                "sub": new_user.email,
                "role": role_name,
                "user_id": str(new_user.id)  # Convert UUID to string
            },
            expires_delta=access_token_expires
        )

        # Return the token, user_id, and role
        return {
            "access_token": access_token,
            "token_type": "bearer"
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail= "Error creating account"
        )


@router.post("/login/", status_code=status.HTTP_200_OK, response_model=TokenResponse)
def login_for_access_token(login_request: LoginRequest, db: Session = Depends(get_db)):
    user = get_user_by_email(db, email=login_request.email)
    if not user or not pwd_context.verify(login_request.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password"
        )
    
    # Ensure HSA account is claimed before allowing login
    if user.role.name == "hsa" and not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account not yet claimed. Please claim your account using the OTP sent to your email."
        )

    try:
        access_token = create_access_token(
            data={
                "sub": user.email,
                "role": user.role.name if hasattr(user.role, 'name') else str(user.role),
                "user_id": str(user.id)
            },
            expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        )
        user.otp = None
        user.otp_expires_at = None
        db.commit()
        db.refresh(user)
        return {
            "access_token": access_token,
            "token_type": "bearer"
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Login failed"
        )



# @router.post("/claim-account/", status_code=status.HTTP_200_OK,response_model=dict)
# def claim_hsa_account(user:ClaimAccountRequest, db: Session = Depends(get_db)):
#     # Fetch the HSA account using email
#     hsa_user = get_user_by_email(db, email=user.email)
    
#     if not hsa_user or hsa_user.role.name != "hsa":
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="HSA not found")

#     # Ensure HSA is inactive before claiming
#     if hsa_user.is_active:
#         raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Account already active")

#     # Verify the OTP
#     if not verify_otp(hsa_user, user.otp):
#         # print(f"The otp is {user.otp}")
#         raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

#     # Check if new password matches confirm password
#     if user.new_password != user.confirm_password:
#         raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Passwords do not match")

#     # OTP is valid, update password and clear OTP fields
#     hashed_password = pwd_context.hash(user.new_password)
#     hsa_user.password = hashed_password
#     hsa_user.otp = None
#     hsa_user.otp_expires_at = None
#     hsa_user.is_active = True

#     db.commit()
#     db.refresh(hsa_user)
#     send_account_verified_email(hsa_user.email, hsa_user.fullname)
#     return {"detail": "Account verified successfully. You can now log in with your new password."}



# @router.post("/claim-account/", status_code=status.HTTP_200_OK,response_model=dict)
# def claim_hsa_account(user:ClaimAccountRequest, db: Session = Depends(get_db)):
#     # Fetch the HSA account using email
#     hsa_user = get_user_by_email(db, email=user.email)
    
#     if not hsa_user or hsa_user.role.name != "hsa":
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="HSA not found")

#     # Ensure HSA is inactive before claiming
#     if hsa_user.is_active:
#         raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Account already active")

#     # Verify the OTP
#     if not verify_otp(hsa_user, user.otp):
#         # print(f"The otp is {user.otp}")
#         raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

#     # Check if new password matches confirm password
#     if user.new_password != user.confirm_password:
#         raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Passwords do not match")

#     # OTP is valid, update password and clear OTP fields
#     hashed_password = pwd_context.hash(user.new_password)
#     hsa_user.password = hashed_password
#     hsa_user.otp = None
#     hsa_user.otp_expires_at = None
#     hsa_user.is_active = True

#     db.commit()
#     db.refresh(hsa_user)
#     send_account_verified_email(hsa_user.email, hsa_user.fullname)
#     return {"detail": "Account verified successfully. You can now log in with your new password."}   



@router.post("/claim-account/", status_code=status.HTTP_200_OK, response_model=dict)
def claim_user_account(user: ClaimAccountRequest, db: Session = Depends(get_db)):
    # Fetch the user account using email
    user_obj = get_user_by_email(db, email=user.email)

    if not user_obj:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    # Ensure the user is inactive before claiming the account
    if user_obj.is_active:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Account already active")

    # Verify the OTP
    if not verify_otp(user_obj, user.otp):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

    # Check if new password matches confirm password
    if user.new_password != user.confirm_password:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Passwords do not match")

    # OTP is valid, update password and clear OTP fields
    hashed_password = pwd_context.hash(user.new_password)
    user_obj.password = hashed_password
    user_obj.otp = None
    user_obj.otp_expires_at = None
    user_obj.is_active = True

    db.commit()
    db.refresh(user_obj)
    send_account_verified_email(user_obj.email, user_obj.fullname)
    return {"detail": "Account verified successfully. You can now log in with your new password."}

    
# @router.post("/verify-otp/",status_code=status.HTTP_200_OK,response_model=dict)
# def verify_otp_validity(user:VerifyOTP, db: Session = Depends(get_db)):
#     hsa_user = get_user_by_email(db, email=user.email)
    
#     if not hsa_user or hsa_user.role.name != "hsa":
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="HSA not found")

#     # Ensure HSA is inactive before claiming
#     if hsa_user.is_active:
#         raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Account already active")

#     # Verify the OTP
#     if not verify_otp(hsa_user, user.otp):
#         # print(f"The otp is {user.otp}")
#         raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP") 
    
#     return {"detail": "Please proceed to set your new password."}



@router.post("/verify-otp/", status_code=status.HTTP_200_OK, response_model=dict)
def verify_otp_validity(user: VerifyOTP, db: Session = Depends(get_db)):
    # Fetch user by email
    user_obj = get_user_by_email(db, email=user.email)

    # Check if the user exists and is inactive
    if not user_obj:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    
    # Ensure the user is inactive before allowing OTP verification
    if user_obj.is_active:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Account already active")

    # Verify the OTP
    if not verify_otp(user_obj, user.otp):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid or expired OTP")

    return {"detail": "Please proceed to set your new password."}
