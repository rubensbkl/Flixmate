"use client"

import React, { useState, useEffect } from "react";
import TinderCard from "react-tinder-card";
import { db } from "@/db/firebase"; // Importing the db from Firebase
import { ref, get, child } from "firebase/database"; // Importing Realtime Database functions
import "@/styles/TinderCards.css"

function TinderCards() {
    const [people, setPeople] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            const dbRef = ref(db);
            get(child(dbRef, `people`)).then((snapshot) => {
                if (snapshot.exists()) {
                    // Convert the snapshot to an array
                    const data = snapshot.val();
                    const peopleArray = Object.keys(data).map(key => data[key]);
                    setPeople(peopleArray);
                } else {
                    console.log("No data available");
                    setPeople([]);
                }
            }).catch((error) => {
                console.error(error);
            });
        };
    
        fetchData();
    }, []);


    return (
        <div>
            <h1>Tinder Cards</h1>

            <div className="tinderCards__cardContainer">
                {people.map((person) => (
                    <TinderCard 
                        className="swipe" 
                        key={person.name} 
                        preventSwipe={["up", "down"]}
                    >
                        <div 
                            style={{ backgroundImage: `url(${person.url})` }}
                            className="card"
                        > 
                            <h3>{person.name}</h3>
                        </div>
                    </TinderCard>
                ))}
            </div>
        </div>
    )
}

export default TinderCards