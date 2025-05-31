from fastapi import FastAPI
from recommender import train, recommend, recommend_without_candidates
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

class RecommendSimpleRequest(BaseModel):
    user: str
    top_n: int = 10


@app.post("/train")
def train_endpoint(req: TrainRequest):
    ratings = [r.dict() for r in req.ratings]
    return train(ratings)

@app.post("/recommend")
def recommend_endpoint(req: RecommendRequest):
    return recommend(req.user, req.candidate_ids)

@app.post("/recommend_simple")
def recommend_simple(req: RecommendSimpleRequest):
    return recommend_without_candidates(req.user, req.top_n)