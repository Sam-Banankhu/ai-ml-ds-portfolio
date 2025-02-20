from pydantic import BaseModel, UUID4, EmailStr, Field, constr, model_validator
from enum import Enum
from datetime import datetime
from typing import List, Optional

class UserRole(str, Enum):
    ADMIN = "admin"
    INSTRUCTOR = "instructor"
    HSA = "hsa"

# Token response schema with role
class TokenResponse(BaseModel):
    access_token: str
    token_type: str
    # user_id:str
    # role: UserRole


class UserCreate(BaseModel):
    fullname: str = constr(min_length=3)
    email: EmailStr
    phone: Optional[str] = None
    role: UserRole
    facility_id: Optional[UUID4] = Field(None, description="Required only for HSAs")
    
    @model_validator(mode="before")
    def validate_facility(cls, values):
        role = values.get("role")
        facility_id = values.get("facility_id")

        if role == UserRole.HSA and not facility_id:
            raise ValueError("facility_id is required for HSAs.")
        if role in [UserRole.INSTRUCTOR, UserRole.ADMIN] and facility_id:
            raise ValueError(f"{role.capitalize()} should not be assigned a facility.")
        return values

    class Config:
        from_attributes = True

class Users(BaseModel):
    id: UUID4
    fullname: str
    email: EmailStr
    phone: str
    is_active: bool
    role: UserRole
    facility_id: Optional[UUID4]

    class Config:
        from_attributes = True


class ClaimAccountRequest(BaseModel):
    email: EmailStr
    otp: str
    new_password: str
    confirm_password: str

class VerifyOTP(BaseModel):
    email:EmailStr
    otp:str
    
    
class HSACreateRequest(BaseModel):
    fullname: str = constr(min_length=2)
    email: EmailStr
    facility_id: UUID4
    
    class Config:
        from_attributes = True


class EmailSendRequest(BaseModel):
    email: EmailStr
    # fullname:str
    # ot:str = Field(..., min_length=1)
    otp: str = constr(min_length=6, max_length=6) 

    class Config:
        from_attributes = True

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class MakeQuery(BaseModel):
    query: str

class Section(BaseModel):
    id: UUID4
    name: str

    class Config:
        from_attributes = True


class ChatCreate(BaseModel):
    title: str
    section_id: UUID4

class Chat(BaseModel):
    id: UUID4
    title: str
    created_at: datetime
    section: Section

    class Config:
        from_attributes = True
        

class ChatHistoryCreate(BaseModel):
    question: str
    response: str
    section_id: UUID4
    
    
class ChatHistoryResponse(BaseModel):
    id: UUID4
    question: str
    response: str
    timestamp: datetime
    section_id: UUID4

    class Config:
        from_attributes = True

class ChatWithHistory(BaseModel):
    id: UUID4
    title: str
    created_at: datetime
    section: Section
    chat_histories: List[ChatHistoryResponse]

    class Config:
        from_attributes = True

class Facility(BaseModel):
    id: UUID4
    name: str
    facility_type: str
    managing_authority: str
    urban_rural: str
    district_id: UUID4

    class Config:
        from_attributes = True

class District(BaseModel):
    id: UUID4
    name: str
    facilities: Optional[List[Facility]] = []

    class Config:
        from_attributes = True

class FAQ(BaseModel):
    id: UUID4
    question: str
    answer: str
    frequency: int
    chat_history_id: UUID4
    hsa: Users

    class Config:
        from_attributes = True

class FAQQueryByDistrict(BaseModel):
    district_id: UUID4

class FAQQueryByFacility(BaseModel):
    facility_id: UUID4

class HandoutCreate(BaseModel):
    title: str
    content: str
    section_id: UUID4  # The selected section

class Handout(BaseModel):
    id: UUID4
    title: str
    content: str
    created_at: datetime
    section_id: UUID4  # Section association

    class Config:
        from_attributes = True

class HandoutQuery(BaseModel):
    section_id: UUID4
    topic: str 

# Schemas for creating District and Facility
class DistrictCreate(BaseModel):
    id: UUID4
    name: str

class FacilityCreate(BaseModel):
    id: UUID4
    name: str
    facility_type: str
    managing_authority: str
    urban_rural: str
    district_id: UUID4

# Schema for facility lookup based on district and facility ID
class FacilityByDistrictAndIDRequest(BaseModel):
    district_id: UUID4
    facility_id: UUID4

# Schemas for querying by District ID or Facility ID
class DistrictIDRequest(BaseModel):
    district_id: UUID4

class FacilityIDRequest(BaseModel):
    facility_id: UUID4

# Section base schema
class SectionBase(BaseModel):
    section: int
    title: str


class UserUpdate(BaseModel):
    fullname: Optional[str]
    phone: Optional[str]

    class Config:
        from_attributes = True


class PasswordUpdate(BaseModel):
    old_password: str
    new_password: str

    class Config:
        from_attributes = True


from typing import Optional

class UserResponse(BaseModel):
    id: UUID4
    fullname: str
    email: str
    phone: Optional[str]
    role: str
    is_active: bool = False
    facility_name: Optional[str] = None
    facility_id: Optional[UUID4] = None
    district_name: Optional[str] = None
    # Make sure facility-related fields are optional
    facility_type: Optional[str] = None
    managing_authority: Optional[str] = None
    urban_rural: Optional[str] = None


    class Config:
        from_attributes = True
        

class HSAUserResponse(BaseModel):
    id: UUID4
    fullname: str
    email: EmailStr
    phone: str 
    is_active: bool
    role: str
    managing_authority: str
    facility_id: str 
    urban_rural: str
    facility_name: str 
    facility_type: str 
    district_name: str 
     
    
# Input model for updating the user
class UserUpdate(BaseModel):
    fullname: str
    email: EmailStr
    phone: str

# Response model for returning updated user info
class UserUpdateResponse(BaseModel):
    fullname: str
    email: EmailStr
    phone: str

    class Config:
        from_attributes = True
        
        
        
# Schema for creating a new section
class SectionCreate(BaseModel):
    name: str

# Schema for response
class SectionResponse(BaseModel):
    id: UUID4
    name: str

    class Config:
        from_attributes = True
        

class SectionRequest(BaseModel):
    section_id: UUID4
    
    
# Define Pydantic models for the request parameters
class SectionIDRequest(BaseModel):
    section_id: UUID4

class SectionUpdateRequest(BaseModel):
    section: SectionCreate
    
    

# Schema for the request (section_id)
class SectionRequest(BaseModel):
    section_id: UUID4

class Query(BaseModel):
    section_id: UUID4
    question: str

# Schema for each question in the response
class QuestionResponse(BaseModel):
    question: str
    # timestamp: datetime

# Schema for the response (list of random questions)
class RandomQuestionsResponse(BaseModel):
    # section_id: UUID4
    sample_questions: List[QuestionResponse]


# Schema for individual handout details
class HandoutDetails(BaseModel):
    title: str
    created_at: datetime

# Schema for the handout response with recent handouts
class HandoutResponse(BaseModel):
    id: UUID4
    instructor_name: str
    instructor_id: UUID4
    title: str
    content: str
    section_name: str  
    created_at: datetime

    class Config:
        from_attributes = True


class HandoutRequest(BaseModel):
    handout_id: UUID4


class HandoutSortOrder(str, Enum):
    ASC = "asc"
    DESC = "desc"
    
class HandoutQueryParams(BaseModel):
    order: HandoutSortOrder = HandoutSortOrder.DESC  
    # section_id: Optional[UUID4] = None 
    

# Define the response model using Pydantic
class TotalPagesResponse(BaseModel):
    total_handouts: int
    total_pages: int
    
    

class RoleUserCount(BaseModel):
    role_name: str
    user_count: int

class RoleUserCountResponse(BaseModel):
    roles: List[RoleUserCount]
    
    
# Define a response schema to structure the count data
class SectionHandoutCountResponse(BaseModel):
    section_id: UUID4
    section_name: str
    handout_count: int



class EmailReset(BaseModel):
    email:EmailStr