import React from "react";
import { Link, useNavigate } from "react-router-dom";

const Navigation = ({ setAuthenticated }) => {
  const navigate = useNavigate();

  const getCSRFToken = () => {
      return document.cookie
          .split("; ")
          .find((row) => row.startsWith("X-CSRF-Token="))
          ?.split("=")[1];
  };

  const handleLogout = () => {
      fetch("/api/logout", {
          method: "POST",
          credentials: "include", 
      }).then(() => {
          setAuthenticated(false);
          navigate("/");
      });
  };
  return (
    <nav className="navigation">
      <ul>
        <li><Link to="/">Home</Link></li>
        <li><button onClick={handleLogout}>Sair</button></li>
      </ul>
    </nav>
  );
};

export default Navigation;