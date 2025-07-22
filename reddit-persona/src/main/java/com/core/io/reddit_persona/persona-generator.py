import json
import sys
import requests
from bs4 import BeautifulSoup
from datetime import datetime

username = sys.argv[1]

with open("comments.json", "r") as f:
    comments = json.load(f)
with open("posts.json", "r") as f:
    posts = json.load(f)

def get_cake_day(username):
    url = f"https://www.reddit.com/user/{username}/"
    headers = {
        "User-Agent": "Mozilla/5.0"
    }
    res = requests.get(url, headers=headers)
    soup = BeautifulSoup(res.text, "html.parser")

    for tag in soup.find_all("span"):
        if "Redditor for" in tag.text:
            try:
                years = int(tag.text.split("Redditor for")[1].split()[0])
                return datetime.utcnow().year - years
            except:
                return None
    return None

estimated_age = get_cake_day(username)
if estimated_age is None:
    estimated_age = 25

all_text = "\n".join([
    p['data']['title'] + "\n" + p['data'].get('selftext', '')
    for p in posts['data']['children'][:5]
]) + "\n" + "\n".join([
    c['data']['body'] for c in comments['data']['children'][:5]
])

prompt = f"""
You are an AI trained to generate personas.
Based on the user's Reddit data, write a detailed persona in JSON with:
- name
- age (estimated)
- location (if hinted)
- occupation (if mentioned)
- status (e.g., single, married)
- archetype
- traits (list of adjectives)
- motivations (6 categories, 1-5)
- personality (8 traits, 1-10 scale)
- behaviours, frustrations, goals (as bullet lists)
- a short user quote

Reddit Username: {username}
Estimated Age: {estimated_age}
Recent Posts & Comments:
{all_text}

Respond in JSON only.
"""

# === Switch between OpenAI and local model (Ollama) ===
use_openai = False  # set to True if you want to use OpenAI again

if use_openai:
    import openai
    openai.api_key = "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"
    try:
        response = openai.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[{"role": "user", "content": prompt}]
        )
        persona_json = response.choices[0].message.content
        print(persona_json)
    except Exception as e:
        print("ERROR:", str(e))
else:
    # Use Ollama with a lightweight local model (e.g. tinyllama)
    try:
        response = requests.post(
            "http://localhost:11434/api/generate",
            json={"model": "tinyllama", "prompt": prompt, "stream": False}
        )
        print("RAW RESPONSE:", response.status_code, response.text)
        if response.ok:
            data = response.json()
            print(data.get("response", "No 'response' key in Ollama result"))
        else:
            print("ERROR:", response.status_code, response.text)
    except Exception as e:
        print("EXCEPTION:", str(e))