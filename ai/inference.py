from fastapi import FastAPI
from recommender import train, recommend_after_training, surprise
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI()

class Rating(BaseModel):
    user: str
    movie: str
    rating: int  # 1 ou 0

class TrainRequest(BaseModel):
    ratings: List[Rating]

class RecommendRequest(BaseModel):
    ratings: List[Rating]
    candidate_ids: Optional[List[str]]

class SurpriseRequest(BaseModel):
    user: str
    candidate_ids: List[str]

@app.post("/train")
def train_endpoint(req: TrainRequest):
    ratings = [r.dict() for r in req.ratings]
    return train(ratings)

@app.post("/recommend")
def recommend_endpoint(req: RecommendRequest):
    ratings = [r.dict() for r in req.ratings]
    return recommend_after_training(ratings, req.candidate_ids)

@app.post("/surprise")
def surprise_endpoint(req: SurpriseRequest):
    return surprise(req.user, req.candidate_ids)
