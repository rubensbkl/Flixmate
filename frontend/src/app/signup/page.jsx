"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function SignUpPage() {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const router = useRouter();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          username: formData.username,
          email: formData.email,
          password: formData.password,
        })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || "Erro ao criar conta");
      }

      // Redireciona para login após registro bem-sucedido
      router.push("/login?registered=true");
    } catch (err) {
      setError(err.message || "Ocorreu um erro ao criar sua conta");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-4 bg-white">
      <div className="w-full max-w-md">
        <div className="flex items-center justify-center mb-8">
          <img 
            src="/flixmate-logo.svg" 
            alt="Flixmate" 
            className="h-12"
          />
        </div>
        
        <h1 className="text-2xl font-bold text-center mb-2">Crie uma conta</h1>
        <p className="text-center text-gray-600 mb-8">
          Utilize seu email para entrar no Flixmate
        </p>

        {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <input
              type="text"
              name="username"
              placeholder="Nome de usuário"
              className="w-full p-3 border border-gray-300 rounded-lg"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>
          
          
          <div>
            <input
              type="email"
              name="email"
              placeholder="Email"
              className="w-full p-3 border border-gray-300 rounded-lg"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>
          
          <div>
            <input
              type="password"
              name="password"
              placeholder="Senha"
              className="w-full p-3 border border-gray-300 rounded-lg"
              value={formData.password}
              onChange={handleChange}
              required
              minLength={6}
            />
          </div>
      
          <button
            type="submit"
            className="w-full p-3 bg-black text-white rounded-lg font-medium"
            disabled={isLoading}
          >
            {isLoading ? "Cadastrando..." : "Cadastrar"}
          </button>
        </form>
        
        <p className="text-center mt-6 text-gray-600">
          Já tem uma conta?{" "}
          <Link href="/login" className="text-blue-600 font-medium">
            Entre aqui
          </Link>
        </p>
      </div>
    </div>
  );
}
