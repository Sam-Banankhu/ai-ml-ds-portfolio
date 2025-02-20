from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# app = FastAPI()



# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=origins,
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )

origins = [
    "http://localhost.tiangolo.com",
    "https://localhost.tiangolo.com",
    "http://localhost",
    "http://localhost:8000",
    "http://127.0.0.1:8000",
    "http://127.0.0.1:3000",
    "https://tot-35es.onrender.com/",
    "https://tot-35es.onrender.com"
    "http://localhost:8080",
    "http://localhost:3000",
    "http://localhost:5173",
    "http://192.168.43.89:3000",
    "http://192.168.5.225:8000",
    "https://7604-102-70-10-130.ngrok-free.app",
    "*",
    ]

def add_cors_middleware(app):
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins,  # Allows all origins
        allow_credentials=True,
        allow_methods=["*"],  # Allows all methods
        allow_headers=["*"],  # Allows all headers
    )
