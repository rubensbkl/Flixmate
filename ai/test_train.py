import requests

ratings = [
    {"user": "6", "movie": "541671", "rating": 1}
]

res = requests.post("http://localhost:5005/train", json={"ratings": ratings})
print(res.json())