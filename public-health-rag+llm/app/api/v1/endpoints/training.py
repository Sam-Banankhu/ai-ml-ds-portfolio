from math import ceil
from typing import List, Optional
from uuid import UUID
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import or_
from sqlalchemy.orm import Session, joinedload
from pydantic import UUID4
from app.api.v1.crud import create_handout, delete_handout
from app.api.v1.utils import check_is_instructor, generate_handout_title, get_current_user
from app.database import get_db
from app.api.v1.models import Handout, Section
from app.api.v1.schemas import HandoutQuery, HandoutQueryParams, HandoutRequest, HandoutResponse, HandoutSortOrder, TotalPagesResponse, Users
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from app.config import llm, vector_store
from datetime import datetime

router = APIRouter()


handout_template = """
You are a highly skilled trainer tasked with creating a well-structured and detailed teaching handout based on Malawian Technical Guidelines for Integrated Disease Surveliance and Response that are used to train Public Health Surveliance Assintants on the following Topic: {context}.
The handout should contain:

Slide 1. An introduction that outlines the topic.
Slide 2. Key points that cover essential details about the topic.
Slide 3. Detailed concept.
Slide 4. Detailed concept (continued..).
Slide 5. Detailed concept (continued..).
Slide 6. Detailed concept (continued..).
Slide 7. Detailed concept (continued..).
Slide 9. Examples to illustrate the concepts.
Slide 10. A conclusion summarizing the key takeaways.

Generate a handout in the format described above. 
Do not include any word saying Detailed concept or Slide number. Rather, replace it with the appropriate slide title.
Headings should be formatted as follows:
# for heading 1
## for heading 2
### for heading 3
#### for heading 4
** ** for bullet points

Each slide should only contain 2-3 subheadings below is just an Example:
# Slide title
** Subheading ** \n\n
    - Sentence 1 \n
    - Sentence 2 \n
    - Sentence 3 \n
    - Sentence 4 \n\n
** Subheading ** \n\n
    - Sentence 1 \n
    - Sentence 2 \n
    - Sentence 3 \n
    - Sentence 4 \n\n
    
otherwise, if slide has more bullet points, add other subheadings to a next new slide with same heading but slide title must have a Cont'd.
You are not limited to the provided number of slides but must be over 15.
Use "\n\n---\n\n" to separate slides.
Ensure that the handout is well-structured, easy to follow, and includes examples to illustrate the concepts.
Ensure that the handout is concise and easy to read but reach.
Ensure that the handout is well-organized and easy to navigate.
Ensure that the handout is free of any errors or inaccuracies.
Ensure the that handout has enough number of slides.
"""

PROMPT = PromptTemplate(
    template=handout_template, input_variables=["context"]
)


from asyncio import sleep  # For demonstration purposes if needed to simulate wait time.

@router.post("/generate-handout", response_model=HandoutResponse)
async def generate_handout(
    query: HandoutQuery,
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
):
    # Ensure user is an instructor
    check_is_instructor(current_user)

    # Fetch the section
    section = db.query(Section).filter_by(id=query.section_id).first()
    if not section:
        raise HTTPException(status_code=404, detail="Section not found")

    # Generate title asynchronously
    title = generate_handout_title(query.topic)

    # Generate handout content asynchronously
    qa = RetrievalQA.from_chain_type(
        llm=llm,
        chain_type="stuff",
        retriever=vector_store.as_retriever(search_kwargs={'k': 5}),
        chain_type_kwargs={"prompt": PROMPT}
    )
    response = qa.invoke(query.topic)
    handout_content = response.get('result')
    
    if not handout_content:
        raise HTTPException(status_code=500, detail="Failed to generate handout content")

    # Create and save new handout in the database
    handout = Handout(
        title=title,
        content=handout_content,
        created_by_id=current_user.id,
        section_id=query.section_id,
        created_at=datetime.now()
    )
    db.add(handout)
    db.commit()
    db.refresh(handout)

    # Return the newly created handout in the same format as the HandoutResponse model
    return HandoutResponse(
            id=handout.id,
            instructor_name=handout.created_by.fullname,  # Use fullname directly
            instructor_id = handout.created_by.id,
            title=handout.title,
            content=handout.content,
            section_name=handout.section.name,
            created_at=handout.created_at
        )


@router.get("/handouts", response_model=List[HandoutResponse], include_in_schema=True)
async def get_all_handouts(
    order: HandoutSortOrder = HandoutSortOrder.DESC,  # Sort asc or desc
    section_id: Optional[UUID4] = None,              # Optional section/module ID
    user_id: Optional[UUID] = None,                  # Optional user ID filter
    limit: int = Query(5, ge=1),                     # Limit of handouts per page (5 in this case)
    offset: int = Query(0, ge=0),                    # Offset for pagination
    searchQuery: Optional[str] = None,               # Search query (optional)
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
):
    # Ensure the user is an instructor
    check_is_instructor(current_user)

    # Base query for handouts
    query = db.query(Handout).options(
        joinedload(Handout.created_by),  # Load the related Users (instructor) data
        joinedload(Handout.section)      # Load the related Section data
    )

    # Filter by section if section_id is provided
    if section_id:
        query = query.filter(Handout.section_id == section_id)

    # Filter by user ID if provided
    if user_id:
        query = query.filter(Handout.created_by_id == user_id)

    # Apply search filter if the search query is provided (excluding Users search)
    if searchQuery:
        search_term = f"%{searchQuery}%"
        query = query.filter(
            or_(
                Handout.title.ilike(search_term),       # Search by title
                Handout.content.ilike(search_term)      # Search by content
            )
        )

    # Apply sorting based on the order
    if order == HandoutSortOrder.ASC:
        query = query.order_by(Handout.created_at.asc())
    else:
        query = query.order_by(Handout.created_at.desc())

    # Apply pagination using limit and offset
    handouts = query.offset(offset).limit(limit).all()

    if not handouts:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No handouts found.")

    # Return the response with handout details
    return [
        HandoutResponse(
            id=handout.id,
            instructor_name=handout.created_by.fullname,  # Use fullname directly
            instructor_id = handout.created_by.id,
            title=handout.title,
            content=handout.content,
            section_name=handout.section.name,
            created_at=handout.created_at
        )
        for handout in handouts
    ]

@router.get("/handouts/pages", response_model=TotalPagesResponse, include_in_schema=True)
async def get_total_pages(
      # default to 5 handouts per page
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
    ):
    # Ensure the user is an instructor
    check_is_instructor(current_user)
    limit: int = 5
    # Count the total number of handouts in the database
    total_handouts = db.query(Handout).count()

    # Calculate the total number of pages (chunks)
    total_pages = ceil(total_handouts / limit)

    return TotalPagesResponse(total_handouts=total_handouts, total_pages=total_pages)

    
@router.get("/handouts/{handout_id}", response_model=HandoutResponse,include_in_schema=True)
async def get_handout_by_id(
    handout_id: UUID4,
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)):
    
    # Ensure the user is an instructor
    check_is_instructor(current_user)

    # Query the handout by ID, including relationships to created_by (Users) and section
    handout = db.query(Handout).options(joinedload(Handout.created_by), joinedload(Handout.section)).filter_by(id=handout_id).first()

    # Raise a 404 error if handout is not found
    if handout is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Handout not found.")

    # Return the handout details
    return HandoutResponse(
            id=handout.id,
            instructor_name=handout.created_by.fullname, 
            instructor_id = handout.created_by.id,
            title=handout.title,
            content=handout.content,
            section_name=handout.section.name,
            created_at=handout.created_at
        )



@router.delete("/delete/{handout_id}", status_code=status.HTTP_200_OK,include_in_schema=True)
async def remove_handout(
    handout_id: UUID4,
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)):
    """
    Delete a handout by ID. Only the instructor who created it or an admin can delete it.
    """
    check_is_instructor(current_user)
    return delete_handout(db=db, handout_id=handout_id, user_id=current_user.id)