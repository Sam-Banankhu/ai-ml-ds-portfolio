import openai
import os
from dotenv import load_dotenv 
load_dotenv()
OPENAI_API_KEY= os.getenv("OPENAI_API_KEY")
openai.api_key=OPENAI_API_KEY
def structure_response(query,context):
    try:
        MODEL = "gpt-4"
        prompt= f"structure this text from rag\n\n: {context}.\n\nbased on this question:\n{query}"
        response = openai.chat.completions.create(
            model=MODEL,
            
            messages=[
                {"role": "system", "content": "You are a helpful assistant to structure responses in chichewa."},
                # {"role": "assistant", "content": "Yes I can provide me with the text"},
                {"role": "user", "content": prompt},
            ],
            temperature=0,
        )
        print(query)
        generated_text = response.choices[0].message.content
        return generated_text
    except Exception as e:
        return f"Error during API call: {str(e)}"