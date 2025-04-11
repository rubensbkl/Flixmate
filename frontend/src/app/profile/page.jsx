"use client";

import Navbar from '@/components/Navbar';
import ProfileSection from '@/components/ProfileSection';
import { UserIcon } from '@heroicons/react/24/outline';

export default function ProfilePage() {
  const recentMovies = [
    'https://via.placeholder.com/150',
    'https://via.placeholder.com/150',
    'https://via.placeholder.com/150',
  ];

  const favoriteMovies = [
    'https://via.placeholder.com/150',
    'https://via.placeholder.com/150',
    'https://via.placeholder.com/150',
  ];

  const recommendedMovies = [
    'https://via.placeholder.com/150',
    'https://via.placeholder.com/150',
    'https://via.placeholder.com/150',
  ];

  return (
    <main className="min-h-screen pb-16 md:pb-4 bg-white flex flex-col items-center justify-center">
      {/* Seção do perfil do usuário */}
      <section className="flex flex-col items-center space-y-3 p-4 bg-transparent rounded-md mb-8">
        <div className="h-16 w-16 bg-gray-200 rounded-full flex items-center justify-center">
          <UserIcon className="h-8 w-8 text-gray-600" />
        </div>
        <span className="text-xl font-semibold text-gray-900">Nome Usuário</span>
      </section>

      {/* Seções de filmes utilizando o componente ProfileSection */}
      <ProfileSection title="Filmes recentes" images={recentMovies} />
      <ProfileSection title="Filmes Favoritos" images={favoriteMovies} />
      <ProfileSection title="Recomendados" images={recommendedMovies} />

      {/* Navbar */}
      <Navbar />
    </main>
  );
}