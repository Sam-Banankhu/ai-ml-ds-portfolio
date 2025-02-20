from datetime import datetime, timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import UUID4, BaseModel, EmailStr
from sqlalchemy.orm import Session
from app.api.v1.models import User, Facility, District
from app.database import get_db
from app.api.v1.schemas import (
    EmailReset,
    EmailSendRequest,
    HSAUserResponse,
    UserUpdate, 
    UserResponse, 
    PasswordUpdate,UserUpdateResponse)
from app.api.v1.crud import (
    get_user, update_user_info_in_db, verify_and_update_user_password
)
from app.api.v1.utils import generate_otp, get_current_user, send_otp_to_email, send_reset_password_otp_to_email

router = APIRouter()



@router.get("/me", response_model=HSAUserResponse)
def get_user_info(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if current_user.role.name.lower() in ["admin", "instructor"]:
         return HSAUserResponse(
            id=current_user.id,
            fullname=current_user.fullname,
            email=current_user.email,
            phone=current_user.phone or "",
            is_active=current_user.is_active,
            role=current_user.role.name.upper(),
            managing_authority="",
            facility_id="",
            urban_rural="",
            facility_name="",
            facility_type="",
            district_name=""
        )

    # For 'hsa' users, include facility and district information
    facility = db.query(Facility).filter(Facility.id == current_user.facility_id).first()
    if not facility:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Facility not found for the user."
        )
    district = db.query(District).filter(District.id == facility.district_id).first()
    if not district:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="District not found for the facility."
        )

    return HSAUserResponse(
        id=current_user.id,
        fullname=current_user.fullname,
        email=current_user.email,
        phone=current_user.phone or "",
        is_active=current_user.is_active,
        role=current_user.role.name.upper(),
        managing_authority=facility.managing_authority,
        facility_id=str(current_user.facility_id),
        urban_rural=facility.urban_rural,
        facility_name=facility.name,
        facility_type=facility.facility_type,
        district_name=district.name
    )


@router.put("/me/update", response_model=UserUpdateResponse)
def update_user_info(
    user_update: UserUpdate, 
    db: Session = Depends(get_db), 
    current_user: User = Depends(get_current_user)
):
    """
    Update user's full name, email, and phone number.
    Returns the updated full name, email, and phone number.
    """
    updated_user = update_user_info_in_db(
        db, 
        user_id=current_user.id, 
        fullname=user_update.fullname, 
        email=user_update.email, 
        phone=user_update.phone
    )

    if not updated_user:
        raise HTTPException(status_code=404, detail="User not found")

    # Return the updated user information
    return {
        "fullname": updated_user.fullname,
        "email": updated_user.email,
        "phone": updated_user.phone
    }


@router.put("/me/password", response_model=dict)
def update_user_password(
    password_update: PasswordUpdate, 
    db: Session = Depends(get_db), 
    current_user: User = Depends(get_current_user)
):
    """
    Update user's password after verifying the old password.
    Returns a success message if the password is updated.
    """
    updated_user = verify_and_update_user_password(
        db, 
        user_id=current_user.id, 
        old_password=password_update.old_password, 
        new_password=password_update.new_password
    )

    if not updated_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Old password is incorrect"
        )

    # Return a success message instead of full user data
    return {"message": "Password changed successfully"}
    
@router.post("/forgot-password", status_code=status.HTTP_200_OK)
def forgot_password(user_data:EmailReset, db: Session = Depends(get_db)
):
    # Fetch user by email
    user = db.query(User).filter(User.email == user_data.email).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User with provided email not found."
        )
    
    # Generate OTP and set expiration time (e.g., 10 minutes from now)
    otp = generate_otp()
    otp_expires_at = datetime.now() + timedelta(minutes=10)
    try:
        # Update user's OTP, OTP expiration, deactivate account, and clear password
        user.otp = otp
        user.otp_expires_at = otp_expires_at
        # user.is_active = False
        # user.password = None  # Clearing the password
        
        db.commit()
        email_data = EmailSendRequest(email=user.email, otp=otp)
        # Send OTP to the user's email
        send_reset_password_otp_to_email(db=db,email_data=email_data)
    except:
        raise HTTPException(detail=f"failed to send OTP to {user.fullname}")
    
    return {"message": f"OTP has been sent to {user.email} for password reset."}
