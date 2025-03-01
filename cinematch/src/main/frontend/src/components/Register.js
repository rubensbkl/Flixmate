import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

const Register = ({ setAuthenticated }) => {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [gender, setGender] = useState("M");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      setError("As senhas não coincidem.");
      return;
    }

    try {
      const response = await fetch("/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ first_name: firstName, last_name: lastName, email, password, gender }),
        credentials: "include",
      });

      if (response.ok) {
        setAuthenticated(true);
        navigate("/"); // Redireciona para a página inicial após registro bem-sucedido
      } else {
        const data = await response.json();
        setError(data.message || "Falha ao registrar.");
      }
    } catch (error) {
      console.error("Erro ao registrar:", error);
      setError("Erro ao conectar ao servidor.");
    }
  };

  return (
    <div className="register-container">
      <h2>Registro</h2>
      {error && <p className="error">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>Nome:</label>
        <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />

        <label>Sobrenome:</label>
        <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required />

        <label>Email:</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />

        <label>Senha:</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

        <label>Confirme sua senha:</label>
        <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />

        <label>Gênero:</label>
        <select value={gender} onChange={(e) => setGender(e.target.value)} required>
          <option value="M">Masculino</option>
          <option value="F">Feminino</option>
          <option value="O">Outro</option>
        </select>

        <button type="submit">Registrar</button>
      </form>
    </div>
  );
};

export default Register;