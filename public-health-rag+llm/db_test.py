from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.database import Base  # Import your Base
from app.api.v1.models import Role  # Import your Role model
from app.config import settings
import os
from dotenv import load_dotenv
load_dotenv()

# Define your database URL (make sure it's the same as the one used in your application)
# DATABASE_URL = f"postgresql://{settings.POSTGRES_USER}:{settings.POSTGRES_PASSWORD}@{settings.POSTGRES_HOST}:{settings.POSTGRES_PORT}/{settings.POSTGRES_DB}"
DATABASE_URL = f"postgresql://postgres.bxihzatdtpjawlkkrobp:{os.getenv('SUPERBASE_PASSWORD')}@aws-0-eu-central-1.pooler.supabase.com:6543/postgres"

from app.database import engine, Base

# Create all tables
Base.metadata.drop_all(bind=engine)
Base.metadata.create_all(bind=engine)
# # Create a configured "Session" class
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# # Create a new session instance
db = SessionLocal()

# # Create the roles
roles = ["admin", "instructor", "hsa"]

try:
    # Check if roles already exist to avoid duplicates
    existing_roles = db.query(Role).filter(Role.name.in_(roles)).all()
    existing_role_names = {role.name for role in existing_roles}

    # Add roles if they do not exist
    for role_name in roles:
        if role_name not in existing_role_names:
            role = Role(name=role_name)
            db.add(role)
    db.commit()
    print("Roles have been added successfully.")
except Exception as e:
    db.rollback()
    print(f"An error occurred: {e}")
finally:
    db.close()

# print(settings.SUPERBASE_PASSWORD)