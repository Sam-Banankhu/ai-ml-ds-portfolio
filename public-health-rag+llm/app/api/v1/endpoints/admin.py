from typing import List, Union
from fastapi import APIRouter, Depends, HTTPException, status,Query
from fastapi.responses import HTMLResponse
from pydantic import UUID4
from sqlalchemy.orm import Session
from app.api.v1.models import District, Facility, Role, User
from app.database import get_db
from app.api.v1.schemas import EmailSendRequest, HSACreateRequest, UserCreate, UserResponse, Users, ChatHistoryResponse
from app.api.v1.crud import (
    create_admin_account, create_hsa_account, create_instructor_account, get_users, get_user, update_user, delete_user, 
    get_chat_histories, delete_chat_history
)
from app.api.v1.utils import check_is_admin, generate_otp, get_current_user, send_otp_to_email
from app.api.v1.schemas import UserRole
# import markdown

router = APIRouter()


@router.get("/", response_class=HTMLResponse)
def read_admin_documentation(
    current_user: Users = Depends(get_current_user)
    ):
    check_is_admin(current_user)
    with open("./docs/admin.md", "r") as file:
        readme_content = file.read()
    # html_content = markdown.markdown(readme_content)
    return HTMLResponse(content=readme_content)



# @router.post("/create-hsa/", status_code=status.HTTP_201_CREATED)
# def admin_create_hsa(hsa_data: HSACreateRequest, db: Session = Depends(get_db), current_user: Users = Depends(get_current_user)):
#     # Ensure admin role before allowing the operation
#     check_is_admin(current_user)
#     # Generate OTP for account claiming
#     otp = generate_otp()

#     # Create HSA account with OTP as a temporary password
#     new_hsa = create_hsa_account(db, hsa_data=hsa_data, otp=otp)

#   # Send OTP to HSA via email
#     email_data = EmailSendRequest(email=hsa_data.email, otp=otp)
#     send_otp_to_email(email_data,db)

#     return {"detail": "HSA created successfully. OTP sent to the provided email."}


@router.post("/create-user/", status_code=status.HTTP_201_CREATED)
def admin_create_user(
    user_data: UserCreate,  # Use the flexible UserCreate schema
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Ensure admin role before allowing the operation
    check_is_admin(current_user)
    
    # Generate OTP for account claiming
    otp = generate_otp()

    if user_data.role == "hsa":
        # Create HSA account with OTP
        new_hsa = create_hsa_account(db, hsa_data=user_data, otp=otp)
        
        # Send OTP to HSA via email
        email_data = EmailSendRequest(email=user_data.email, otp=otp)
        send_otp_to_email(email_data, db)

        return {"detail": "HSA created successfully. OTP sent to the provided email."}
    
    elif user_data.role == "instructor":
        # Create instructor account
        new_instructor = create_instructor_account(db, instructor_data=user_data, otp=otp)
        
        # Send OTP to instructor via email
        email_data = EmailSendRequest(email=user_data.email, otp=otp)
        send_otp_to_email(email_data, db)

        return {"detail": "Instructor created successfully. OTP sent to the provided email."}

    elif user_data.role == "admin":
        # Create admin account
        new_admin = create_admin_account(db, admin_data=user_data, otp=otp)
        
        # Send OTP to admin via email
        email_data = EmailSendRequest(email=user_data.email, otp=otp)
        send_otp_to_email(email_data, db)

        return {"detail": "Admin created successfully. OTP sent to the provided email."}

    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid role specified. Must be 'hsa', 'instructor', or 'admin'."
        )

@router.get("/users", response_model=list[UserResponse])
def list_users(
    role: str = Query(..., description="The role of the users to list (e.g., 'hsa' or 'instructor')"),
    db: Session = Depends(get_db), 
    current_user: User = Depends(get_current_user)
):
    check_is_admin(current_user)

    # Query users based on the provided role
    users = db.query(User).join(Role).filter(Role.name == role.lower()).all()

    if not users:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"No users found for the role: {role}."
        )

    # Iterate through the users to construct detailed user info
    serialized_users = []
    for user in users:
        if role == "hsa":
            facility = db.query(Facility).filter(Facility.id == user.facility_id).first()
            district = db.query(District).filter(District.id == facility.district_id).first() if facility else None

            serialized_users.append(
                UserResponse(
                    id=user.id,
                    fullname=user.fullname,
                    email=user.email,
                    phone=user.phone if user.phone else "",
                    role=user.role.name.upper(),
                    is_active=user.is_active,
                    managing_authority=facility.managing_authority if facility else None,
                    urban_rural=facility.urban_rural if facility else None,
                    facility_name=facility.name if facility else None,
                    facility_id=facility.id if facility else None,
                    facility_type=facility.facility_type if facility else None,
                    district_name=district.name if district else None,
                )
            )
        elif role == "instructor":
            serialized_users.append(
                UserResponse(
                    id=user.id,
                    fullname=user.fullname,
                    email=user.email,
                    phone=user.phone if user.phone else "",
                    role=user.role.name.upper(),
                    is_active=user.is_active,
                    managing_authority=None,
                    urban_rural=None,
                    facility_name=None,
                    facility_id=None,
                    facility_type=None,
                    district_name=None,
                )
            )
            
        elif role == 'admin':
             serialized_users.append(
                UserResponse(
                    id=user.id,
                    fullname=user.fullname,
                    email=user.email,
                    phone=user.phone if user.phone else "",
                    role=user.role.name.upper(),
                    is_active=user.is_active,
                    managing_authority=None,
                    urban_rural=None,
                    facility_name=None,
                    facility_id=None,
                    facility_type=None,
                    district_name=None,
                )
            )         

    return serialized_users


# Route to return all Admins
@router.get("/users/admins", response_model=list[Users])
def list_admins(
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
):
    check_is_admin(current_user)
    # Filter users by the role 'admin'
    admins = db.query(User).filter(User.role.has(name="admin")).all()
    
    serialized_admins = [
        {
            "id": str(user.id),
            "fullname": user.fullname,
            "email": user.email,
            "phone": user.phone if user.phone else "",
            "role": user.role.name,
            "is_active": user.is_active,
            "facility_id": str(user.facility_id) if user.facility_id else None
        }
        for user in admins
    ]
    
    return serialized_admins


# Get user by ID
@router.get("/users/{user_id}", response_model=Users)
def get_user_detail(
    user_id: int, 
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)
):
    check_is_admin(current_user)
    user = get_user(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="Users not found")
    return user

# Update user
@router.put("/users/{user_id}", response_model=Users)
def update_user_detail(
    user_id: int, 
    user_update: UserCreate, 
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)
):
    check_is_admin(current_user)
    user = update_user(db, user_id, user_update)
    if not user:
        raise HTTPException(status_code=404, detail="Users not found")
    return user

# Delete user
@router.delete("/users/{user_id}", response_model=Users)
def delete_user_detail(
    user_id: int, 
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)
):
    check_is_admin(current_user)
    user = delete_user(db, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="Users not found")
    return user

# Get all chat histories
@router.get("/chats")
def list_chat_histories(section_id: UUID4 = Query(None), db: Session = Depends(get_db)):
    if not section_id:
        raise HTTPException(status_code=400, detail="Section ID is required")
    
    return get_chat_histories(db, section_id)

# Delete chat history
@router.delete("/chats/{chat_id}", response_model=ChatHistoryResponse)
def delete_chat(
    chat_id: int, 
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)
):
    check_is_admin(current_user)
    chat = delete_chat_history(db, chat_id)
    if not chat:
        raise HTTPException(status_code=404, detail="Chat history not found")
    return chat



@router.put("/deactivate-user/{user_id}", status_code=status.HTTP_200_OK)
def admin_deactivate_user(
    user_id: UUID4,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Ensure the requesting user is an admin
    check_is_admin(current_user)

    # Retrieve the user from the database
    user = db.query(User).filter(User.id == user_id).first()

    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")

    # Only HSAs and instructors can be deactivated
    if user.role.name not in ["hsa", "instructor"]:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Only HSAs and instructors can be deactivated")
    
    # Deactivate the user
    user.is_active = False
    user.password = None  # Reset password to None

    # Commit the changes to the database
    db.commit()

    return {"detail": f"{user.role.name.title()} account for {user.fullname} has been deactivated."}