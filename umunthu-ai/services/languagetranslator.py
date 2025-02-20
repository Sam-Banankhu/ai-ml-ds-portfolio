import openai
import os
import requests
import json
from dotenv import load_dotenv
load_dotenv()

MODEL = "gpt-4"
google_api_key = os.getenv("GOOGLE_API_KEY")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
PINECONE_API_KEY=os.getenv("PINECONE_API_KEY")
openai.api_key=OPENAI_API_KEY
def gpt_translate_text(query, context):
    # context_str = ''
    # print(context)
    # for obj in context:
    #     content = obj.get('content', '')
    #     metadata = obj.get('content', {}).get('metadata', {})
    #     chapter = metadata.get('chapter', 'N/A')
    #     section = metadata.get('section', 'N/A')
        
    #     context_str += f"Chapter: {chapter}\nSection: {section}\nContent: {content}\n\n"
    
    try:
        prompt= f"Context\n\n: {context}.\n\nQuery:\n{query}"
        response = openai.chat.completions.create(
            model=MODEL,
            messages=[
                {"role": "system", "content": "You are a helpful assistant to structure responses from a context and query and return response in chichewa only."},
                {"role": "user", "content":prompt},
            ],
            temperature=0,
        )
        message = response.choices[0].message.content.strip()
        return message
    except Exception as e:
        return f"Error during API call: {str(e)}"
    
    
    
    

def google_translate_text(text, target_language='ny'):
    # Google Translate API URL
    url = f"https://translation.googleapis.com/language/translate/v2"
    
    # Parameters for the API request
    params = {
        'q': text,                   # The text you want to translate
        'target': target_language,    # The language to translate into (e.g., 'ny' for Chichewa)
        'key': google_api_key                # Your API key
    }

    # Make the request to Google Translate API
    response = requests.get(url, params=params)
    
    # Handle the response
    if response.status_code == 200:
        result = response.json()
        translated_text = result['data']['translations'][0]['translatedText']
        print(f'RESULT: {result}')
        return translated_text
    else:
        print(f"Error: {response.status_code}")
        print(response.text)
        return None

# Example usage with your API key

# translate_text( "Hello, how are you?", target_language='ny')

