from fastapi import FastAPI
from recommender import train, recommend
from pydantic import BaseModel
from typing import List

app = FastAPI()

class Rating(BaseModel):
    user: str
    movie: str
    rating: int

class TrainRequest(BaseModel):
    ratings: List[Rating]

class RecommendRequest(BaseModel):
    user: str
    candidate_ids: List[str]

@app.post("/train")
def train_endpoint(req: TrainRequest):
    ratings = [r.dict() for r in req.ratings]
    return train(ratings)

@app.post("/recommend")
def recommend_endpoint(req: RecommendRequest):
    return recommend(req.user, req.candidate_ids)
