import os
from dotenv import load_dotenv
load_dotenv()
from langchain_chroma import Chroma
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from pydantic_settings import BaseSettings

# Settings class to load environment variables
class Settings(BaseSettings):
    POSTGRES_USER: str = os.getenv("POSTGRES_USER")
    POSTGRES_PASSWORD: str = os.getenv("POSTGRES_PASSWORD")
    SUPERBASE_PASSWORD: str = os.getenv("SUPERBASE_PASSWORD")
    POSTGRES_DB: str = os.getenv("POSTGRES_DB")
    POSTGRES_HOST: str = os.getenv("POSTGRES_HOST")
    POSTGRES_PORT: str = os.getenv("POSTGRES_PORT")
    JWT_SECRET_KEY: str = os.getenv("JWT_SECRET_KEY")
    OPENAI_KEY: str = os.getenv("OPENAI_KEY")
    TVLY_API: str = os.getenv("TVLY_API")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 999999
    PINECONE_API_KEY: str = os.getenv("PINECONE_API_KEY")
    GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY")
    GMAIL_PASSWORD: str = os.getenv("GMAIL_PASSWORD")

    class Config:
        env_file = ".env"

# Initialize settings
settings = Settings()

# AIInitializer class
class AIInitializer:
    def __init__(self, settings: Settings):
        # Set up the OpenAI API key
        os.environ["OPENAI_API_KEY"] = settings.OPENAI_KEY
        
        # Initialize the embeddings and vector store
        self.embedding = OpenAIEmbeddings(model="text-embedding-ada-002")
        self.vector_store = Chroma(
            collection_name="idsr",
            embedding_function=self.embedding,
            persist_directory="data/idsr_vectors"
        )

        # Initialize the language model
        self.llm = ChatOpenAI(
            model_name='gpt-4o',
            temperature=0.1
        )

    def get_embedding(self):
        """Returns the initialized embedding."""
        return self.embedding

    def get_vector_store(self):
        """Returns the initialized vector store."""
        return self.vector_store

    def get_llm(self):
        """Returns the initialized LLM."""
        return self.llm


ai_initializer = AIInitializer(settings)

# Access the LLM and embedding
llm = ai_initializer.get_llm()
embedding = ai_initializer.get_embedding()
vector_store = ai_initializer.get_vector_store()
