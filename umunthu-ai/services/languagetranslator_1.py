import openai
import os
import requests
import json
from dotenv import load_dotenv

load_dotenv()

MODEL = "gpt-4"
google_api_key = os.getenv("GOOGLE_API_KEY")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
PINECONE_API_KEY = os.getenv("PINECONE_API_KEY")
openai.api_key = OPENAI_API_KEY

def gpt_translate_text(query, context):
    """
    Translate text using OpenAI's GPT model.

    Args:
        query (str): The query to be translated.
        context (str): The context for the translation.

    Returns:
        str: The translated text or an error message.
    """
    try:
        prompt = f"Context\n\n: {context}.\n\nQuery:\n{query}"
        response = openai.chat.completions.create(
            model=MODEL,
            messages=[
                {"role": "system", "content": "You are a helpful assistant to structure responses from a context and query and return response in Chichewa only."},
                {"role": "user", "content": prompt},
            ],
            temperature=0,
        )
        message = response.choices[0].message.content.strip()
        return message
    except Exception as e:
        return f"Error during API call: {str(e)}"

def google_translate_text(text, target_language='ny'):
    """
    Translate text using Google Translate API.

    Args:
        text (str): The text to be translated.
        target_language (str): The language to translate into (default is 'ny' for Chichewa).

    Returns:
        str: The translated text or None if an error occurred.
    """
    url = "https://translation.googleapis.com/language/translate/v2"
    params = {
        'q': text,
        'target': target_language,
        'key': google_api_key
    }

    response = requests.get(url, params=params)
    
    if response.status_code == 200:
        result = response.json()
        translated_text = result['data']['translations'][0]['translatedText']
        return translated_text
    else:
        print(f"Error: {response.status_code}")
        print(response.text)
        return None
