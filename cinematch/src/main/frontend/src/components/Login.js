import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const Login = ({ setAuthenticated }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
        credentials: "include", // Mantém cookies de sessão
      });

      if (response.ok) {
        setAuthenticated(true);
        navigate("/"); // Redireciona para a página inicial
      } else {
        const data = await response.json();
        setError(data.message || "Login falhou!");
      }
    } catch (error) {
      console.error("Erro ao fazer login:", error);
      setError("Erro ao conectar ao servidor.");
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      {error && <p className="error">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>Email:</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label>Senha:</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        <button type="submit">Entrar</button>
      </form>
    </div>
  );
};

export default Login;