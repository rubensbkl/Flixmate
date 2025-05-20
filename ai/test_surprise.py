import requests

payload = {
    "user": "6",
    "candidate_ids": ["870028", "228967", "869291", "1738", "40096", "539", "1738"]
}

res = requests.post("http://localhost:5005/surprise", json=payload)
print(res.json())