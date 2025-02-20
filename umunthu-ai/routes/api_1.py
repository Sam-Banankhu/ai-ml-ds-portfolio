from flask import Blueprint, request, jsonify
from services.pinecone_service import PineconeService
from services.langchain_service import LangchainService
# from services.langchain_service import Embedding

api_blueprint = Blueprint('api', __name__)
pinecone_service = PineconeService()
langchain_service = LangchainService()

@api_blueprint.route('/upsert', methods=['POST'])
def upsert():
    data = request.json.get('vectors')
    pinecone_service.upsert_data(data)
    return jsonify({'message': 'Data upserted successfully'}), 200

@api_blueprint.route('/query', methods=['POST'])
def query():
    query_vector = request.json.get('vector')
    results = pinecone_service.query_data(query_vector)
    return jsonify(results), 200

@api_blueprint.route('/generate', methods=['POST'])
def generate():
    prompt = request.json.get('prompt')
    response = langchain_service.generate_response()
    return jsonify({'response': response}), 200
