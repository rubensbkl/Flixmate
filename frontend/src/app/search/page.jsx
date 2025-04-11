"use client";

import Header from '@/components/Header';
import Navbar from '@/components/Navbar';
import Searchbar from '@/components/Searchbar'; // Importando o componente Searchbar

export default function SearchPage() {
  const handleSearch = (query) => {
    console.log('Buscando por:', query);
    // Adicione a lógica de busca aqui
  };

  return (
    <main className="min-h-screen pb-16 md:pb-4 bg-gray-100">
      <Header />

      {/* Barra de pesquisa */}
      <section className="flex justify-center items-center p-4">
        <Searchbar onSearch={handleSearch} />
      </section>

      <Navbar />
    </main>
  );
}