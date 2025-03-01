import React from "react";
import { Link, useNavigate } from "react-router-dom";

const Navigation = ({ setAuthenticated }) => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      const response = await fetch("/api/logout", {
        method: "POST",
        credentials: "include",
      });

      if (response.ok) {
        setAuthenticated(false);
        navigate("/login");
      } else {
        console.error("Erro ao sair.");
      }
    } catch (error) {
      console.error("Erro ao desconectar:", error);
    }
  };

  return (
    <nav className="navigation">
      <ul>
        <li><Link to="/">Home</Link></li>
        <li><Link to="/recommendations">Recomendações</Link></li>
        <li><button onClick={handleLogout}>Sair</button></li>
      </ul>
    </nav>
  );
};

export default Navigation;