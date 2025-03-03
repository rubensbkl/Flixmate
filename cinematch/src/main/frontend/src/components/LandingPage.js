import React from 'react';
import '../output.css';

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col justify-center items-center">
      <header className="w-full bg-blue-600 text-white p-4 text-center">
        <h1 className="text-3xl font-bold">Bem-vindo ao CineMatch</h1>
      </header>
      <main className="flex-grow flex flex-col justify-center items-center">
        <h2 className="text-2xl font-semibold mb-4">Encontre o filme perfeito para assistir</h2>
        <p className="text-lg mb-8">Cadastre-se ou faça login para começar</p>
        <div>
          <a href="/register" className="bg-blue-600 text-white px-4 py-2 rounded mr-4">Cadastre-se</a>
          <a href="/login" className="bg-gray-600 text-white px-4 py-2 rounded">Login</a>
        </div>
      </main>
      <footer className="w-full bg-gray-800 text-white p-4 text-center">
        <p>&copy; 2025 CineMatch. Todos os direitos reservados.</p>
      </footer>
    </div>
  );
};

export default LandingPage;