from sqlalchemy.orm import Session
from sqlalchemy import text
from app.api.v1.models import Section
from app.database import SessionLocal
from app.database import engine, Base
import uuid

# # Function to drop all tables with CASCADE
# def drop_all_tables():
#     with engine.connect() as conn:
#         conn.execute(text("DROP TABLE IF EXISTS chat_histories CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS faqs CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS handouts CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS sections CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS users CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS roles CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS facilities CASCADE"))
#         conn.execute(text("DROP TABLE IF EXISTS districts CASCADE"))
#     print("All tables dropped successfully.")

# Function to create all tables
def create_all_tables():
    Base.metadata.create_all(bind=engine)
    print("All tables created successfully.")

# Function to populate sections with specific UUIDs
def populate_sections(db: Session):
    sections = [
        {"id": "440c49bd-c539-4f4d-b6ca-ef299d7a3b03", "name": "Report Priority Diseases, Conditions and Events"},
        {"id": "5873ebb0-78e4-4fd7-b981-5c095db9c79a", "name": "Responding to Outbreaks and Other Public Health Events"},
        {"id": "5ee88974-4049-45ba-a2a6-62f40911d8e6", "name": "Tailoring IDSR to Emergency or Fragile Health System Contexts"},
        {"id": "7dad4114-2f20-4f77-939d-f2009ae62818", "name": "Monitor, Supervise, Evaluate and Provide Feedback to Improve the Surveillance and Response System"},
        {"id": "84117349-e425-4f22-9f7c-b69f843c848e", "name": "Risk Communication"},
        {"id": "90bcde30-9de4-4b74-812f-354f5365c70f", "name": "Electronic IDSR (EIDSR)"},
        {"id": "c2ac4183-a5ae-496a-98f0-48413c625caf", "name": "Detect And Record Cases of Priority Diseases, Conditions and Events"},
        {"id": "c4ef9e32-1b62-467d-8a7f-78df9670da17", "name": "Prepare To Respond to Outbreaks and Other Public Health Events"},
        {"id": "e2f9539c-97c7-435b-ac3f-5cee46c39218", "name": "Investigate and Confirm Suspected Outbreaks and Other Public Health Events"},
        {"id": "fc812ad2-edd4-4684-86a3-db98b2bb3ec1", "name": "Analyse Data"}
    ]
    
    for section in sections:
        db.add(Section(id=uuid.UUID(section["id"]), name=section["name"]))  # Add section with UUID and name
    db.commit()
    print("Sections added successfully")

# Function to populate the database
def populate_db():
    db = SessionLocal()
    try:
        populate_sections(db)
    finally:
        db.close()

if __name__ == "__main__":
    # Drop all tables
    # drop_all_tables()
    
    # Create all tables
    # create_all_tables()
    
    # Populate sections
    populate_db()
