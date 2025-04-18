"use client";

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';
import Navbar from '@/components/Navbar';
import ProtectedRoute from '@/components/ProtectedRoute';
import Searchbar from '@/components/Searchbar';
import { fetchUsers } from '@/lib/api'; // Importando a função que criamos

export default function SearchPage() {
  const [users, setUsers] = useState([]);
  const [filteredUsers, setFilteredUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const router = useRouter();

  // Buscar todos os usuários ao carregar a página
  useEffect(() => {
    const loadUsers = async () => {
      try {
        setLoading(true);
        const userData = await fetchUsers();
        setUsers(userData);
        setFilteredUsers(userData);
      } catch (err) {
        console.error('Erro ao buscar usuários:', err);
        setError('Falha ao carregar usuários. Tente novamente mais tarde.');
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, []);

  // Filtrar usuários com base na consulta
  const handleSearch = (query) => {
    if (!query) {
      setFilteredUsers(users);
      return;
    }

    const filtered = users.filter(user => 
      user.firstName?.toLowerCase().includes(query.toLowerCase()) || 
      user.lastName?.toLowerCase().includes(query.toLowerCase()) ||
      user.email?.toLowerCase().includes(query.toLowerCase())
    );
    
    setFilteredUsers(filtered);
  };

  // Redirecionar para o perfil do usuário
  const handleUserClick = (userId) => {
    router.push(`/profile/${userId}`);
  };

  return (
    <ProtectedRoute>
      <div className="bg-gray-100 md:flex">
        {/* Navbar */}
        <Navbar />

        <main className="flex-1 overflow-auto flex flex-col h-[calc(100vh-4rem)] md:h-screen">
          <Header />

          {/* Barra de pesquisa */}
          <section className="flex justify-center items-center p-4">
            <Searchbar onSearch={handleSearch} />
          </section>

          {/* Resultados da pesquisa */}
          <section className="flex-1 p-4">
            {loading ? (
              <div className="flex justify-center items-center h-full">
                <div className="w-12 h-12 border-t-4 border-blue-500 border-solid rounded-full animate-spin"></div>
              </div>
            ) : error ? (
              <div className="text-red-500 text-center">{error}</div>
            ) : filteredUsers.length === 0 ? (
              <div className="text-center text-gray-500">Nenhum usuário encontrado</div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredUsers.map(user => (
                  <div 
                    key={user.id} 
                    className="bg-white rounded-lg shadow-md p-4 flex items-center gap-4 cursor-pointer hover:bg-gray-50 transition"
                    onClick={() => handleUserClick(user.id)}
                  >
                    <div className="w-12 h-12 bg-gray-200 rounded-full flex items-center justify-center">
                      {user.profileImage ? (
                        <img 
                          src={user.profileImage} 
                          alt={`${user.firstName} ${user.lastName}`} 
                          className="w-full h-full object-cover rounded-full" 
                        />
                      ) : (
                        <span className="text-gray-500 text-xl">{user.firstName?.charAt(0)}</span>
                      )}
                    </div>
                    <div>
                      <h3 className="font-medium">{user.firstName} {user.lastName}</h3>
                      <p className="text-sm text-gray-500">{user.email}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        </main>
      </div>
    </ProtectedRoute>
  );
}