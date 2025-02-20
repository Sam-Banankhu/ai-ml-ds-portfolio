import os
from openai import OpenAI
from pinecone import Pinecone, ServerlessSpec
from langchain_core.documents import Document
from langchain_openai import OpenAIEmbeddings
from langchain_pinecone import PineconeVectorStore
from services.gptconversation import structure_response
from services.languagetranslator import google_translate_text
from services.languagetranslator import gpt_translate_text
import os
from dotenv import load_dotenv  
load_dotenv()

OPENAI_API_KEY=os.getenv("OPENAI_API_KEY")
# Initialize OpenAI and Pinecone with API keys
pinecone_api_key = os.getenv("PINECONE_API_KEY")


pc = Pinecone(api_key=pinecone_api_key)
index_name = 'umunthu'

# Create OpenAI embeddings
embeddings = OpenAIEmbeddings(openai_api_key=OPENAI_API_KEY)

def get_vectorstore_results(query):
    # Create a new index if not exists
    if index_name not in pc.list_indexes().names():
        pc.create_index(
            name=index_name,
            dimension=1536,
            metric="cosine",
            spec=ServerlessSpec(cloud="aws", region="us-east-1"),
        )

    docs = []  # Add your documents here
    vectorstore = PineconeVectorStore.from_documents(docs, embeddings, index_name=index_name)
    results = vectorstore.similarity_search_with_score(query, k=2)
    formatted_results = ''
    for result in results:
        formatted_results +=f"\n\n {result[0].page_content}"
    return formatted_results
    # return get_translation_results(query,formatted_results)



