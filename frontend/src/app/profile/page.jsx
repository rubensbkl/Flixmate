"use client";

import Navbar from '@/components/Navbar';
import ProfileSection from '@/components/ProfileSection';
import { UserIcon } from '@heroicons/react/24/outline';
import { useState } from 'react';

export default function ProfilePage() {
  const [profileImage, setProfileImage] = useState(null); // Estado para armazenar a URL da imagem do perfil

  const recentMovies = [
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/4VtkIaj76TpQNfhDHXQDdT9uBN5.jpg',
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/hSljn1jfCClIbHNdlz6cWQEjncV.jpg',
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/knIEw2jWOxrb6ugNPEoaiYI6p77.jpg',
  ];

  const favoriteMovies = [
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/4VtkIaj76TpQNfhDHXQDdT9uBN5.jpg',
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/hSljn1jfCClIbHNdlz6cWQEjncV.jpg',
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/knIEw2jWOxrb6ugNPEoaiYI6p77.jpg',
  ];

  const recommendedMovies = [
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/4VtkIaj76TpQNfhDHXQDdT9uBN5.jpg',
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/hSljn1jfCClIbHNdlz6cWQEjncV.jpg',
    'https://image.tmdb.org/t/p/w600_and_h900_bestv2/knIEw2jWOxrb6ugNPEoaiYI6p77.jpg',
  ];

  return (
    <div className="bg-gray-100 md:flex">

        {/* Navbar */}
        <Navbar />

        <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] md:h-screen">
            {/* Seção do perfil do usuário */}
            <section className="flex flex-col items-center space-y-3 p-4 bg-transparent rounded-md mb-8">
                <div className="h-16 w-16 bg-gray-200 rounded-full flex items-center justify-center overflow-hidden">
                {profileImage ? (
                    <img src={profileImage} alt="Foto de perfil" className="h-full w-full object-cover" />
                ) : (
                    <UserIcon className="h-8 w-8 text-gray-600" />
                )}
                </div>
                <span className="text-xl font-semibold text-gray-900">Nome Usuário</span>
            </section>

            {/* Seções de filmes utilizando o componente ProfileSection */}
            <ProfileSection title="Filmes recentes" images={recentMovies} />
            <ProfileSection title="Filmes Favoritos" images={favoriteMovies} />
            <ProfileSection title="Recomendados" images={recommendedMovies} />


        </main>
    </div>
  );
}