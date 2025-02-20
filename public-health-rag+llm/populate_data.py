import json
from sqlalchemy.orm import Session
from app.database import SessionLocal
from app.api.v1.crud import create_district, create_facility
from app.api.v1.schemas import DistrictCreate, FacilityCreate

# Function to load districts and facilities from the combined JSON file
def load_data(db: Session):
    with open("data/health_facilities/districts_facilities_data.json") as f:
        data = json.load(f)
        
        # Load districts
        district_map = {}
        for district_data in data['districts']:
            # Use pre-assigned UUIDs from JSON
            district = DistrictCreate(**district_data)
            created_district = create_district(db, district)
            
            # Store the created district in a map to reference it later for facilities
            district_map[district_data['name']] = created_district.id

        # Load facilities
# Load facilities
        for facility_data in data['facilities']:
            # Use the district_id directly from the facility data
            district_id = facility_data['district_id']

            # Create facility
            facility = FacilityCreate(**facility_data)
            create_facility(db, facility)


# Main function to populate the database
def populate_db():
    db = SessionLocal()
    try:
        load_data(db)
    finally:
        db.close()

if __name__ == "__main__":
    populate_db()
