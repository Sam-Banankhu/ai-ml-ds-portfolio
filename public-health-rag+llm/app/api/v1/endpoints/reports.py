from typing import List
from sqlalchemy.orm import Session
from fastapi import APIRouter, Depends
from app.api.v1.models import Handout, Role, Section, User
from app.api.v1.schemas import RoleUserCount, RoleUserCountResponse, SectionHandoutCountResponse, Users
from app.api.v1.utils import check_is_admin, get_current_user
from app.database import get_db
from sqlalchemy import func

router = APIRouter()

@router.get("/user-count-by-role", response_model=RoleUserCountResponse)
def get_user_count_by_role(db: Session = Depends(get_db),current_user:Users = Depends(get_current_user)):
    check_is_admin(current_user)
    role_counts = (
        db.query(Role.name, func.count(User.id))
        .join(User, Role.id == User.role_id)
        .group_by(Role.name)
        .all()
    )

    response_data = [
        RoleUserCount(role_name=role_name, user_count=user_count)
        for role_name, user_count in role_counts
    ]
    return RoleUserCountResponse(roles=response_data)


@router.get("/section-handout-count", response_model=List[SectionHandoutCountResponse], include_in_schema=True)
async def get_section_handout_count(db: Session = Depends(get_db),current_user:Users = Depends(get_current_user)):
    check_is_admin(current_user)
    """
    Retrieve the number of sections and the count of handouts in each section.
    """
    # Query all sections and the handout count per section
    section_handouts = (
        db.query(Section.id, Section.name, func.count(Handout.id).label("handout_count"))
        .join(Handout, Section.id == Handout.section_id, isouter=True)  # Left join to include sections with zero handouts
        .group_by(Section.id, Section.name)
        .all()
    )

    # Format the results as a list of SectionHandoutCountResponse objects
    response = [
        SectionHandoutCountResponse(
            section_id=section.id,
            section_name=section.name,
            handout_count=section.handout_count
        )
        for section in section_handouts
    ]

    return response