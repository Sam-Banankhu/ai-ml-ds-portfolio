import os
from langchain_community.document_loaders import PyPDFLoader, DirectoryLoader
# from langchain_community.document_loaders import DirectoryLoader
from langchain_openai import OpenAIEmbeddings
from langchain.text_splitter import CharacterTextSplitter
from langchain_pinecone import PineconeVectorStore
from pinecone import Pinecone, ServerlessSpec
from app.config import settings
from tqdm import tqdm

OPENAI_API_KEY = settings.OPENAI_KEY
PINECONE_API_KEY = settings.PINECONE_API_KEY

# from langchain.document_loaders import DirectoryLoader

# directory = 'data/dummy'

# # def load_docs(directory):
# #   loader = DirectoryLoader(directory)
# #   documents = loader.load()
# #   return documents

# loader = DirectoryLoader(directory,show_progress=True)
# documents = loader.load()
# # print(len(documents))

def create_or_update_index(texts, index_name, embedding,namespace):
    pc = Pinecone(api_key=PINECONE_API_KEY)
    if index_name not in pc.list_indexes().names():
        pc.create_index(
            name=index_name,
            metric='cosine',
            dimension=1536,
            spec=ServerlessSpec(
                cloud='aws',
                region='us-east-1'
            )
        )
        print(f"Created new index: {index_name}")
        vectorstore = PineconeVectorStore.from_documents(
            texts,
            embedding=embedding,
            index_name=index_name
        )
    else:
        print(f"Index {index_name} already exists")
        index = pc.Index(index_name)
        vectorstore = index.upsert(
            texts,
            vectors=embedding,
            namespace = namespace
        )

    return vectorstore

pdf_path = r"data\raw"

files_path = [os.path.join(pdf_path,path) for path in os.listdir(pdf_path)]

for file in tqdm(files_path,desc="Processing PDF files", unit="file"):
    # print(file)
    loader = PyPDFLoader(file)
    documents = loader.load()
    # print(len(documents))

    text_splitter = CharacterTextSplitter(
        chunk_size=500,
        chunk_overlap=100
    )

    texts = text_splitter.split_documents(documents)

    embedding = OpenAIEmbeddings(model="text-embedding-ada-002",api_key=OPENAI_API_KEY)
    namespace = file.split("\\")[-1].replace(".pdf","")
    index_name = "idsr"
    vectorstore = create_or_update_index(texts, index_name, embedding,namespace)