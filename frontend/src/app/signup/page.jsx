'use client';

import { useAuth } from "@/contexts/AuthContext";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useEffect } from "react";

export default function SignUpPage() {
  const { login } = useAuth();
  const router = useRouter();

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    gender: "",
  });

  useEffect(() => {
    return () => {
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        gender: "",
      });
    };
  }, []);

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) throw new Error(data.error || "Erro ao criar conta");

      login(data.user, data.token); // login automático
      router.push("/");
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
          <img src="/flixmate-logo.svg" alt="Flixmate" className="h-12" />
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

        <form onSubmit={handleSubmit} className="space-y-4" key="signup-form" autoComplete="off">
          <input
            type="text"
            name="firstName"
            placeholder="Nome"
            className="w-full p-3 border border-gray-300 rounded-lg"
            value={formData.firstName}
            onChange={handleChange}
            required
            autoComplete="new-firstName"
          />
          <input
            type="text"
            name="lastName"
            placeholder="Sobrenome"
            className="w-full p-3 border border-gray-300 rounded-lg"
            value={formData.lastName}
            onChange={handleChange}
            required
            autoComplete="new-lastName"
          />
          <input
            type="email"
            name="email"
            placeholder="Email"
            className="w-full p-3 border border-gray-300 rounded-lg"
            value={formData.email}
            onChange={handleChange}
            required
            autoComplete="new-email"
          />
          <input
            type="password"
            name="password"
            placeholder="Senha (mín. 6 caracteres)"
            className="w-full p-3 border border-gray-300 rounded-lg"
            value={formData.password}
            onChange={handleChange}
            required
            minLength={6}
            autoComplete="new-password"
            
          />
          <select
            name="gender"
            className="w-full p-3 border border-gray-300 rounded-lg"
            value={formData.gender}
            onChange={handleChange}
            required
            autoComplete="new-gender"
          >
            <option value="">Selecione o gênero</option>
            <option value="M">Masculino</option>
            <option value="F">Feminino</option>
            <option value="O">Outro</option>
          </select>

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