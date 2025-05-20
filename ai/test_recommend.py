import requests

feedbacks = [
    {"user": "6", "movie": "541671", "feedback": 1},
    {"user": "6", "movie": "11", "feedback": 0},
    {"user": "6", "movie": "1359977", "feedback": 1},
    {"user": "6", "movie": "1094274", "feedback": 0},
    {"user": "6", "movie": "870028", "feedback": 1},
    {"user": "6", "movie": "896536", "feedback": 0},
    {"user": "6", "movie": "696374", "feedback": 1},
    {"user": "6", "movie": "330459", "feedback": 0},
    {"user": "6", "movie": "1151039", "feedback": 1},
    {"user": "6", "movie": "650033", "feedback": 0}
]

candidate_ids = [9836, 870028, 896536, 1122099, 40096, 539, 713364]

payload = {
    "feedbacks": feedbacks,
    "candidate_ids": list(map(str, candidate_ids))
}

res = requests.post("http://localhost:5005/recommend", json=payload)

print(res.json())