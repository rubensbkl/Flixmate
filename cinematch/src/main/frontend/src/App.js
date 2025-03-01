// src/main/frontend/src/App.js
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Home from './components/Home';
import MovieDetail from './components/MovieDetail';
import Recommendations from './components/Recommendations';
import Navigation from './components/Navigation';
import './App.css';

function App() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Verificar se o usuário está autenticado
    const checkAuth = async () => {
      try {
        const response = await fetch('/api/check-session', { credentials: "include" });
        if (response.ok) {
          const data = await response.text();
          setAuthenticated(data.includes("User email in session"));
        } else {
          setAuthenticated(false);
        }
      } catch (error) {
        console.error("Auth check failed:", error);
        setAuthenticated(false);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  if (loading) {
    return <div className="loading">Carregando...</div>;
  }

  return (
    <Router>
      <div className="app">
        {authenticated && <Navigation setAuthenticated={setAuthenticated} />}
        <div className="content">
          <Routes>
            <Route path="/login" element={
              !authenticated ? 
                <Login setAuthenticated={setAuthenticated} /> : 
                <Navigate to="/" />
            } />
            <Route path="/register" element={
              !authenticated ? 
                <Register setAuthenticated={setAuthenticated} /> : 
                <Navigate to="/" />
            } />
            <Route path="/" element={
              authenticated ? 
                <Home /> : 
                <Navigate to="/login" />
            } />
            <Route path="/movie/:id" element={
              authenticated ? 
                <MovieDetail /> : 
                <Navigate to="/login" />
            } />
            <Route path="/recommendations" element={
              authenticated ? 
                <Recommendations /> : 
                <Navigate to="/login" />
            } />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;