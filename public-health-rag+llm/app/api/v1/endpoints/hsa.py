from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy.sql.expression import func
from datetime import datetime
from app.api.v1.utils import (
    get_current_user, 
    retrieve_recent_chats, 
    setup_qa_chain, 
    validate_section
    )
from app.database import get_db
from app.api.v1.models import ChatHistory
from app.api.v1.schemas import Query, RandomQuestionsResponse, SectionRequest, Users

from dotenv import load_dotenv

# Load environment variables
load_dotenv()

router = APIRouter()

# Main routes

@router.post("/ask")
async def ask_question(
    query: Query,
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
):
    # Validate section
    section = validate_section(query.section_id, db)

    # Set up QA chain and get the answer
    qa = setup_qa_chain(query.section_id)
    response = qa.invoke(query.question)
    answer = response['result']

    # Save chat history
    chat_history = ChatHistory(
        question=query.question,
        response=answer,
        timestamp=datetime.now(),
        section_id=query.section_id,
        user_id=current_user.id
    )
    db.add(chat_history)
    db.commit()

    # Retrieve and return recent chats
    recent_chats = retrieve_recent_chats(current_user.id, query.section_id, db)
    return {
        "recent_chats": [
            {"question": chat.question, "response": chat.response, "timestamp": chat.timestamp} for chat in recent_chats
        ]
    }

@router.post("/recent-chats")
async def get_recent_chats(
    section_request: SectionRequest,
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
):
    # Validate section
    validate_section(section_request.section_id, db)

    # Retrieve and return recent chats
    recent_chats = retrieve_recent_chats(current_user.id, section_request.section_id, db)
    return {
        "recent_chats": [
            {"question": chat.question, "response": chat.response, "timestamp": chat.timestamp} for chat in recent_chats
        ]
    }

@router.post("/sample-questions", response_model=RandomQuestionsResponse)
async def get_sample_questions_by_section(
    section_request: SectionRequest,
    db: Session = Depends(get_db),
    current_user: Users = Depends(get_current_user)
):
    # Validate section
    validate_section(section_request.section_id, db)

    # Query for random questions from the section
    random_questions = db.query(ChatHistory).filter_by(section_id=section_request.section_id)\
        .order_by(func.random()).limit(3).all()

    # Return the random questions
    return RandomQuestionsResponse(
        sample_questions=[
            {"question": chat.question} for chat in random_questions
        ]
    )