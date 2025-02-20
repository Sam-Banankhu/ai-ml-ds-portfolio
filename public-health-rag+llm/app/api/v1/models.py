from sqlalchemy import Column, String, Boolean, DateTime, ForeignKey, UUID, Text, Integer
from sqlalchemy.orm import relationship
import uuid
from datetime import datetime
from app.database import Base

class Role(Base):
    __tablename__ = "roles"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    name = Column(String, unique=True, nullable=False)

    # Relationship to users
    users = relationship("User", back_populates="role")



class User(Base):
    __tablename__ = "users"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    fullname = Column(String, nullable=False)
    email = Column(String, unique=True, nullable=False)
    phone = Column(String, nullable=True)
    password = Column(String, nullable=True)
    role_id = Column(UUID(as_uuid=True), ForeignKey('roles.id'))
    is_active = Column(Boolean, default=False)
    facility_id = Column(UUID(as_uuid=True), ForeignKey("facilities.id"), nullable=True)
    otp = Column(String, nullable=True)
    otp_expires_at = Column(DateTime, nullable=True)
    
    role = relationship("Role", back_populates="users")
    facility = relationship("Facility", back_populates="users")
    faqs = relationship("FAQ", back_populates="hsa")
    chat_histories = relationship("ChatHistory", back_populates="user") 
    handouts = relationship("Handout", back_populates="created_by") 
    

class Facility(Base):
    __tablename__ = "facilities"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String, nullable=False)
    facility_type = Column(String, nullable=False)
    managing_authority = Column(String, nullable=False)
    urban_rural = Column(String, nullable=False)
    district_id = Column(UUID(as_uuid=True), ForeignKey("districts.id"))

    district = relationship("District", back_populates="facilities")
    users = relationship("User", back_populates="facility")

class District(Base):
    __tablename__ = "districts"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String, nullable=False)
    
    facilities = relationship("Facility", back_populates="district")

class Section(Base):
    __tablename__ = "sections"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String, nullable=False)
    
    # Define the relationship with Handouts
    handouts = relationship("Handout", back_populates="section")
    # Define the relationship with ChatHistory
    chat_histories = relationship("ChatHistory", back_populates="section")


class Handout(Base):
    __tablename__ = "handouts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    title = Column(String, nullable=False)
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=datetime.now)

    # Instructor relation
    created_by_id = Column(UUID(as_uuid=True), ForeignKey('users.id'))
    created_by = relationship("User")

    # Section relation
    section_id = Column(UUID(as_uuid=True), ForeignKey('sections.id'))
    section = relationship("Section", back_populates="handouts")
    created_by = relationship("User", back_populates="handouts")


class ChatHistory(Base):
    __tablename__ = "chat_histories"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    question = Column(String, nullable=False)
    response = Column(String, nullable=False)
    timestamp = Column(DateTime, default=datetime.now)
    section_id = Column(UUID(as_uuid=True), ForeignKey('sections.id'))
    user_id = Column(UUID(as_uuid=True), ForeignKey('users.id'))  # Foreign key for User

    # Define the relationships
    section = relationship("Section", back_populates="chat_histories")
    user = relationship("User", back_populates="chat_histories")  # Relationship with user


class FAQ(Base):
    __tablename__ = "faqs"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    question = Column(String, nullable=False)
    answer = Column(String, nullable=False)
    frequency = Column(Integer, default=0)
    chat_history_id = Column(UUID(as_uuid=True), ForeignKey("chat_histories.id"))
    hsa_id = Column(UUID(as_uuid=True), ForeignKey("users.id"))

    chat_history = relationship("ChatHistory")
    hsa = relationship("User", back_populates="faqs")
