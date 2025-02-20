from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import HTMLResponse
from pydantic import UUID4
from sqlalchemy.orm import Session
from app.api.v1.crud import create_district, create_facility
from app.api.v1.schemas import (
    DistrictCreate, 
    FacilityCreate, 
    DistrictIDRequest, 
    FacilityIDRequest, 
    FacilityByDistrictAndIDRequest,
    Users
    )
from app.api.v1.utils import check_is_admin, get_current_user
from app.database import get_db
from app.api.v1.models import District, Facility

router = APIRouter()


@router.get("/", response_class=HTMLResponse)
def read_location_documentation():
    with open("./docs/auth.md", "r") as file:
        readme_content = file.read()
    # html_content = markdown.markdown(readme_content)
    return HTMLResponse(content=readme_content)

# Get all districts
@router.get("/districts/", status_code=status.HTTP_200_OK)
def get_all_districts(db: Session = Depends(get_db)):
    districts = db.query(District).all()
    return districts

# Get all facilities
@router.get("/facilities/", status_code=status.HTTP_200_OK)
def get_all_facilities(db: Session = Depends(get_db)):
    facilities = db.query(Facility).limit(10).all()
    return facilities

# @router.get("/facilities", status_code=status.HTTP_200_OK)
# def get_facilities_by_district(district_id: Optional[UUID4] = None, db: Session = Depends(get_db)):
#     if district_id:
#         facilities = db.query(Facility).filter(Facility.district_id == district_id).all()
#         if not facilities:
#             raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No facilities found for this district")
#     else:
#         facilities = db.query(Facility).all()
#     return facilities

# Get facility by id (JSON input)
@router.post("/facilities/facility_id", status_code=status.HTTP_200_OK)
def get_facility_by_id(facility_id_request: FacilityIDRequest, db: Session = Depends(get_db)):
    facility = db.query(Facility).filter(Facility.id == facility_id_request.facility_id).first()
    if not facility:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Facility not found")
    return facility

# Get district by id (JSON input)
@router.post("/districts/district_id", status_code=status.HTTP_200_OK)
def get_district_by_id(district_id_request: DistrictIDRequest, db: Session = Depends(get_db)):
    district = db.query(District).filter(District.id == district_id_request.district_id).first()
    if not district:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="District not found")
    return district

# Get facility by district_id and facility_id (JSON input)
@router.post("/facilities/by_district_and_id", status_code=status.HTTP_200_OK)
def get_facility_by_district_and_id(request: FacilityByDistrictAndIDRequest, db: Session = Depends(get_db)):
    facility = db.query(Facility).filter(
        Facility.district_id == request.district_id,
        Facility.id == request.facility_id
    ).first()

    if not facility:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Facility not found for this district and facility ID")
    
    return facility



# # Delete a district
# @router.delete("/districts/{district_id}", status_code=status.HTTP_204_NO_CONTENT)
# def delete_district(district_id: int, db: Session = Depends(get_db), current_user: Users = Depends(get_current_user)):
#     # Check if the user is an admin
#     check_is_admin(current_user)

#     # Find the district by ID
#     district = db.query(District).filter(District.id == district_id).first()
#     if not district:
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="District not found")

#     # Delete the district
#     db.delete(district)
#     db.commit()

#     return {"detail": "District deleted successfully"}

# # Delete a facility
# @router.delete("/facilities/{facility_id}", status_code=status.HTTP_204_NO_CONTENT)
# def delete_facility(facility_id: int, db: Session = Depends(get_db), current_user: Users = Depends(get_current_user)):
#     # Check if the user is an admin
#     check_is_admin(current_user)

#     # Find the facility by ID
#     facility = db.query(Facility).filter(Facility.id == facility_id).first()
#     if not facility:
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Facility not found")

#     # Delete the facility
#     db.delete(facility)
#     db.commit()

#     return {"detail": "Facility deleted successfully"}

# # Update a district
# @router.put("/districts/{district_id}", status_code=status.HTTP_200_OK)
# def update_district(district_id: int, district_data: DistrictCreate, db: Session = Depends(get_db), current_user: Users = Depends(get_current_user)):
#     # Check if the user is an admin
#     check_is_admin(current_user)

#     # Find the district by ID
#     district = db.query(District).filter(District.id == district_id).first()
#     if not district:
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="District not found")

#     # Update district details
#     district.name = district_data.name
#     district.region = district_data.region

#     db.commit()
#     db.refresh(district)

#     return district

# # Update a facility
# @router.put("/facilities/{facility_id}", status_code=status.HTTP_200_OK)
# def update_facility(facility_id: int, facility_data: FacilityCreate, db: Session = Depends(get_db), current_user: Users = Depends(get_current_user)):
#     # Check if the user is an admin
#     check_is_admin(current_user)

#     # Find the facility by ID
#     facility = db.query(Facility).filter(Facility.id == facility_id).first()
#     if not facility:
#         raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Facility not found")

#     # Update facility details
#     facility.name = facility_data.name
#     facility.district_id = facility_data.district_id

#     db.commit()
#     db.refresh(facility)

#     return facility
