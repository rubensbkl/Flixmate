// src/main/frontend/src/App.js
import React, { useEffect, useState } from "react";
import {
    Navigate,
    Route,
    BrowserRouter as Router,
    Routes,
} from "react-router-dom";
import Home from "./components/Home";
import LandingPage from "./components/LandingPage";
import Login from "./components/Login";
import Navigation from "./components/Navigation";
import Register from "./components/Register";
import './output.css';

function App() {
    const [authenticated, setAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Verificar se o usuário está autenticado
        const checkAuth = async () => {
            try {
                const response = await fetch("/api/check-session", {
                    credentials: "include",
                });
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
                {authenticated && (
                    <Navigation setAuthenticated={setAuthenticated} />
                )}
                <div className="content">
                    <Routes>
                        <Route
                            path="/login"
                            element={
                                !authenticated ? (
                                    <Login
                                        setAuthenticated={setAuthenticated}
                                    />
                                ) : (
                                    <Navigate to="/home" />
                                )
                            }
                        />
                        <Route
                            path="/register"
                            element={
                                !authenticated ? (
                                    <Register
                                        setAuthenticated={setAuthenticated}
                                    />
                                ) : (
                                    <Navigate to="/home" />
                                )
                            }
                        />
                        <Route
                            path="/home"
                            element={
                                authenticated ? (
                                    <Home />
                                ) : (
                                    <Navigate to="/login" />
                                )
                            }
                        />
                        <Route path="/" element={<LandingPage />} />
                    </Routes>
                </div>
            </div>
        </Router>
    );
}

export default App;