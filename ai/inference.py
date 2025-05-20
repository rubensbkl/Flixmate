from fastapi import FastAPI, Request
from recommender import train, recommend_after_training, surprise
from pydantic import BaseModel
from typing import List, Optional

app = FastAPI()

class Feedback(BaseModel):
    user: str
    movie: str
    feedback: int  # 1 ou 0

class TrainRequest(BaseModel):
    feedbacks: List[Feedback]

class RecommendRequest(BaseModel):
    feedbacks: List[Feedback]
    candidate_ids: Optional[List[str]]

class SurpriseRequest(BaseModel):
    user: str
    candidate_ids: List[str]

@app.post("/train")
def train_endpoint(req: TrainRequest):
    feedbacks = [f.dict() for f in req.feedbacks]
    return train(feedbacks)

@app.post("/recommend")
def recommend_endpoint(req: RecommendRequest):
    feedbacks = [f.dict() for f in req.feedbacks]
    candidate_ids = req.candidate_ids
    return recommend_after_training(feedbacks, candidate_ids)

@app.post("/surprise")
def surprise_endpoint(req: SurpriseRequest):
    return surprise(req.user, req.candidate_ids)
