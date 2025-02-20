from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from app.api.v1.schemas import SectionIDRequest, SectionResponse, SectionUpdateRequest, Users
from app.api.v1.crud import get_sections, get_section, update_section, delete_section
from app.api.v1.utils import check_is_admin, get_current_user
from app.database import get_db
from pydantic import UUID4

router = APIRouter()

# Get all sections (authenticated users only)
@router.get("/", response_model=List[SectionResponse])
def list_sections(
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)  # Ensure user is authenticated
):
    sections = get_sections(db)
    return sections

# Get a section by ID (authenticated users only)
@router.post("/get-section", response_model=SectionResponse)
def read_section(
    section_request: SectionIDRequest,
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)  # Ensure user is authenticated
):
    section = get_section(db, section_request.section_id)
    if section is None:
        raise HTTPException(status_code=404, detail="Section not found")
    return section

# Update an existing section (admin-only)
@router.put("/update-section", response_model=SectionResponse)
def update_existing_section(
    update_request: SectionUpdateRequest, 
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)
):
    # Check if the current user is an admin
    check_is_admin(current_user)

    updated_section = update_section(db, update_request.section_id, update_request.section)
    if updated_section is None:
        raise HTTPException(status_code=404, detail="Section not found")
    return updated_section

# Delete a section (admin-only)
@router.delete("/delete-section", response_model=SectionResponse)
def remove_section(
    section_request: SectionIDRequest,
    db: Session = Depends(get_db), 
    current_user: Users = Depends(get_current_user)
):
    # Check if the current user is an admin
    check_is_admin(current_user)

    deleted_section = delete_section(db, section_request.section_id)
    if deleted_section is None:
        raise HTTPException(status_code=404, detail="Section not found")
    return deleted_section