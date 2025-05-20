import requests

feedbacks = [
    {"user": "6", "movie": "870028", "feedback": 0}
]

res = requests.post("http://localhost:5005/train", json={"feedbacks": feedbacks})
print(res.json())