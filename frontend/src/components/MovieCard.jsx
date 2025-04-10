"use client";

import Image from 'next/image';

export default function MovieCard({ movie }) {
  return (
    <div className="relative h-[70vh] w-[90vw] max-w-md rounded-3xl overflow-hidden shadow-xl">
      {/* Fundo do cartão */}
      <div className="absolute inset-0 w-full h-full bg-red-500">
        <Image
          src={movie.image}
          alt={movie.title}
          fill
          style={{ objectFit: 'cover' }}
          className="rounded-3xl"
        />
      </div>
      
      {/* Informações do filme na parte inferior com gradiente */}
      <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/80 to-transparent text-white rounded-b-3xl">
        <div className="text-sm text-gray-300">{movie.studio}</div>
        <div className="text-2xl font-bold">{movie.title}</div>
        <p className="mt-1 text-sm text-gray-200">{movie.description}</p>
      </div>
    </div>
  );
}