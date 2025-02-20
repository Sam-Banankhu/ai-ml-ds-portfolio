from fastapi import FastAPI, APIRouter
from fastapi.responses import RedirectResponse
from app.api.v1.endpoints import (
    auth,
    chat, 
    hsa,
    reports,
    sections, 
    training,
    admin,
    location,
    user
    )
from app.middleware import add_cors_middleware


app = FastAPI(title="IDSR", version="1.0.0")
add_cors_middleware(app)

router = APIRouter()
@router.get("/",include_in_schema=False)
def read_root():
    return RedirectResponse("/docs")


app.include_router(router,prefix="", tags=["Index"])
app.include_router(auth.router, prefix="/api/v1/auth", tags=["Authentication"],responses={404: {"description": "Not found"}})
# app.include_router(chat.router, prefix="/api/v1/chat", tags=["Chat"])
app.include_router(hsa.router, prefix="/api/v1/hsa", tags=["Health Surveillance Assitant"],responses={404: {"description": "Not found"}})
app.include_router(training.router, prefix="/api/v1/training", tags=["Training"],responses={404: {"description": "Not found"}})

app.include_router(reports.router,prefix="/api/v1/reports",tags=["Reports"],responses={404: {"description": "Not found"}})
app.include_router(user.router, prefix="/api/v1/user", tags=["User"],responses={404: {"description": "Not found"}})
app.include_router(admin.router, prefix="/api/v1/admin", tags=["Admin"],responses={404: {"description": "Not found"}})
app.include_router(sections.router, prefix="/api/v1/sections", tags=["Sections"],responses={404: {"description": "Not found"}})
app.include_router(location.router, prefix="/api/v1/location", tags=["Location"],responses={404: {"description": "Not found"}})