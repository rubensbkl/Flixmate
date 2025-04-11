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
    <div className="bg-gray-100 md:flex">

        {/* Navbar */}
        <Navbar />

        <main className="flex-1 overflow-hidden flex flex-col h-[calc(100vh-4rem)] md:h-screen">
            <Header />

            {/* Barra de pesquisa */}
            <section className="flex justify-center items-center p-4">
                <Searchbar onSearch={handleSearch} />
            </section>

        </main>
    </div>
  );
}