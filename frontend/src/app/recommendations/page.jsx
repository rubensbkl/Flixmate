"use client";

import Header from "@/components/Header";
import Navbar from "@/components/Navbar";
import { useState } from "react";

import {
  ClockIcon as ClockOutline,
  StarIcon as StarOutline,
  TrashIcon as TrashOutline,
} from "@heroicons/react/24/outline";

import {
  ClockIcon as ClockSolid,
  StarIcon as StarSolid,
  TrashIcon as TrashSolid,
} from "@heroicons/react/24/solid";

export default function HistoricoPage() {
  const [activeIcons, setActiveIcons] = useState({});

  const toggleIcon = (movieId, iconType) => {
    setActiveIcons((prev) => ({
      ...prev,
      [movieId]: {
        ...prev[movieId],
        [iconType]: !prev[movieId]?.[iconType],
      },
    }));
  };

  const historico = [
    {
      id: 1,
      title: "Carros",
      studio: "Disney Pixar",
      description: "Ação, Carros, Aventura",
      image: "/carros.jpg",
    },
    {
      id: 2,
      title: "Toy Story",
      studio: "Disney Pixar",
      description: "Animação, Amizade, Aventura",
      image: "/toystory.jpg",
    },
  ];

  return (
    <div className="bg-gray-100 md:flex">

        {/* Navbar */}
        <Navbar />

        <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] md:h-screen">

            <Header />
            
            <h1 className="text-2xl font-bold p-4">Histórico</h1>

            <section className="w-full max-w-md space-y-4">
                {historico.map((movie) => (
                <div
                    key={movie.id}
                    className="flex items-center bg-white shadow-md rounded-lg p-4 space-x-4"
                >
                    <img
                    src={movie.image}
                    alt={movie.title}
                    className="w-12 h-12 rounded-md object-cover"
                    />
                    <div className="flex-1">
                    <h2 className="text-base font-bold">{movie.title}</h2>
                    <p className="text-sm text-gray-500">{movie.studio}</p>
                    <p className="text-xs text-gray-400">{movie.description}</p>
                    </div>
                    <div className="flex space-x-2">
                    <button
                        className={`text-base font-bold ${
                        activeIcons[movie.id]?.clock ? "text-blue-600" : "text-base font-bold"
                        }`}
                        onClick={() => toggleIcon(movie.id, "clock")}
                    >
                        {activeIcons[movie.id]?.clock ? (
                        <ClockSolid className="w-5 h-5" />
                        ) : (
                        <ClockOutline className="w-5 h-5" />
                        )}
                    </button>
                    <button
                        className={`text-base font-bold ${
                        activeIcons[movie.id]?.star ? "text-yellow-400" : "text-base font-bold"
                        }`}
                        onClick={() => toggleIcon(movie.id, "star")}
                    >
                        {activeIcons[movie.id]?.star ? (
                        <StarSolid className="w-5 h-5" />
                        ) : (
                        <StarOutline className="w-5 h-5" />
                        )}
                    </button>
                    <button
                        className={`text-base font-bold ${
                        activeIcons[movie.id]?.hoverTrash || activeIcons[movie.id]?.trash
                            ? "text-red-600"
                            : "text-base font-bold"
                        }`}
                        onClick={() => toggleIcon(movie.id, "trash")}
                        onMouseEnter={() =>
                        setActiveIcons((prev) => ({
                            ...prev,
                            [movie.id]: { ...prev[movie.id], hoverTrash: true },
                        }))
                        }
                        onMouseLeave={() =>
                        setActiveIcons((prev) => ({
                            ...prev,
                            [movie.id]: { ...prev[movie.id], hoverTrash: false },
                        }))
                        }
                    >
                        {activeIcons[movie.id]?.hoverTrash || activeIcons[movie.id]?.trash ? (
                        <TrashSolid className="w-5 h-5" />
                        ) : (
                        <TrashOutline className="w-5 h-5" />
                        )}
                    </button>
                    </div>
                </div>
                ))}
            </section>
        </main>
    </div>
  );
}