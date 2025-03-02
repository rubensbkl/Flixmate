import React from "react";
import { Link, useNavigate } from "react-router-dom";

const Navigation = ({ setAuthenticated }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
      fetch("/api/logout", {
          method: "POST",
          credentials: "include",
          headers: {
              "X-CSRF-Token": sessionStorage.getItem("csrf_token"),
          },
      }).then(() => {
          setAuthenticated(false);
          sessionStorage.removeItem("authenticated");
          sessionStorage.removeItem("csrf_token");
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