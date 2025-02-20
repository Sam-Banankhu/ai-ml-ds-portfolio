from flask import Flask, jsonify, request
from services.vectorstore import get_vectorstore_results
from services.languagetranslator import  gpt_translate_text
import os

app = Flask(__name__)


@app.route('/search', methods=['GET'])
def search():
    query = 'generate gender laws'  # Default query if none provided
    results = get_vectorstore_results(query)
    # results_chichewa = get_translation_results(query,results)
    # return jsonify(results_chichewa)
    return gpt_translate_text(query,results)


# @app.route('/search1', methods=['GET'])
# def search1():
#     query = request.args.get('query', 'gender laws')  # Default query if none provided
#     results = get_translation_results(query)
#     return jsonify(results)

if __name__ == '__main__':
    app.run(debug=True)
