import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const Login = ({ setAuthenticated }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetch("/api/session", { credentials: "include" })
      .then((res) => res.json())
      .then((data) => {
        if (data.authenticated) {
          setAuthenticated(true);
        } else {
          setAuthenticated(false);
        }
      })
      .catch(() => setAuthenticated(false));
  }, [setAuthenticated]);

  const handleSubmit = async (e) => {
      e.preventDefault();

      try {
          const response = await fetch("/api/login", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ email, password }),
              credentials: "include", // Ensures session cookie is sent
          });

          const data = await response.json();
          if (data.status === "success") {
              setAuthenticated(true);
              sessionStorage.setItem("csrf_token", data.csrf_token); // ✅ Store CSRF token
              navigate("/");
          } else {
              setError(data.message || "Login failed!");
          }
      } catch (error) {
          console.error("Error during login:", error);
          setError("Failed to connect to the server.");
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