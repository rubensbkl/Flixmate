import requests

ratings = [
    {"user": "6", "movie": "541671", "rating": 1},
    {"user": "6", "movie": "11", "rating": 0},
    {"user": "6", "movie": "1359977", "rating": 1},
    {"user": "6", "movie": "1094274", "rating": 0},
    {"user": "6", "movie": "870028", "rating": 1},
    {"user": "6", "movie": "896536", "rating": 0},
    {"user": "6", "movie": "696374", "rating": 1},
    {"user": "6", "movie": "330459", "rating": 0},
    {"user": "6", "movie": "1151039", "rating": 1},
    {"user": "6", "movie": "650033", "rating": 0}
]

candidate_ids = [9836, 870028, 896536, 1122099, 40096, 539, 713364]

payload = {
    "ratings": ratings,
    "candidate_ids": list(map(str, candidate_ids))
}

res = requests.post("http://localhost:5005/recommend", json=payload)

print(res.json())