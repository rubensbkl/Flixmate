import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import '../output.css';


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
                navigate("/home");
            } else {
                setError(data.message || "Login failed!");
            }
        } catch (error) {
            console.error("Error during login:", error);
            setError("Failed to connect to the server.");
        }
    };

    return (
      <div className="flex w-full h-screen">
      <div className="w-full flex items-center justify-center lg:w-1/2 p-4">
          <div className="bg-white px-10 py-20 rounded-3xl border-2 border-gray-200 max-w-md w-full">
              <h1 className="text-black text-5xl font-semibold">CineMatch</h1>
              <p className="font-medium text-lg text-gray-500 mt-4">
                  Bem-vindo de volta! Entre na sua conta
              </p>

              {error && (
                  <div
                      className="flex items-center p-4 mb-4 text-sm text-red-800 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400"
                      role="alert"
                  >
                      <svg
                          className="shrink-0 inline w-4 h-4 me-3"
                          aria-hidden="true"
                          xmlns="http://www.w3.org/2000/svg"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                      >
                          <path d="M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM9.5 4a1.5 1.5 0 1 1 0 3 1.5 1.5 0 0 1 0-3ZM12 15H8a1 1 0 0 1 0-2h1v-3H8a1 1 0 0 1 0-2h2a1 1 0 0 1 1 1v4h1a1 1 0 0 1 0 2Z" />
                      </svg>
                      <span className="sr-only">Info</span>
                      <div>
                          <span className="font-medium">Erro</span> {error}
                      </div>
                  </div>
              )}
              <form onSubmit={handleSubmit} className="mt-8">
                  <div>
                      <label
                          className="text-black text-lg font-medium"
                          htmlFor="emailinpt"
                      >
                          Email
                      </label>
                      <input
                          id="emailinpt"
                          type="email"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          placeholder="Escreva seu email"
                          required
                          className="text-black w-full border-2 border-gray-200 rounded-xl p-4 m-1 bg-transparent"
                      />
                  </div>
                  <div>
                      <label
                          className="text-black text-lg font-medium"
                          htmlFor="pwd"
                      >
                          Senha
                      </label>
                      <input
                          type="password"
                          id="pwd"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          placeholder="Escreva sua senha"
                          required
                          className="text-black w-full border-2 border-gray-200 rounded-xl p-4 m-1 bg-transparent"
                      />
                  </div>
                  <div className="mt-8 flex justify-between items-center">
                      <div>
                          <input type="checkbox" name="" id="remember" />
                          <label
                              htmlFor="remember"
                              className="ml-2 font-medium text-base text-violet-500"
                          >
                              Remember for 30 days
                          </label>
                      </div>
                      <div>
                          <a
                              href="#"
                              className="font-medium text-base text-violet-500"
                          >
                              Esqueceu Senha?
                          </a>
                      </div>
                  </div>
                  <div className="mt-8 flex flex-col gap-y-4">
                      <button className="active:scale-[.98] active:duration-75 transition-all hover:scale-[1.01] ease-in-out transform py-4 bg-violet-500 rounded-xl text-white font-bold text-lg">
                          Sign in
                      </button>
                      <button className="flex items-center justify-center gap-2 active:scale-[.98] active:duration-75 transition-all hover:scale-[1.01] ease-in-out transform py-4 rounded-xl text-gray-700 font-semibold text-lg border-2 border-gray-100">
                          <svg
                              width="24"
                              height="24"
                              viewBox="0 0 24 24"
                              fill="none"
                              xmlns="http://www.w3.org/2000/svg"
                          >
                              <path
                                  d="M5.26644 9.76453C6.19903 6.93863 8.85469 4.90909 12.0002 4.90909C13.6912 4.90909 15.2184 5.50909 16.4184 6.49091L19.9093 3C17.7821 1.14545 15.0548 0 12.0002 0C7.27031 0 3.19799 2.6983 1.24023 6.65002L5.26644 9.76453Z"
                                  fill="#EA4335"
                              />
                              <path
                                  d="M16.0406 18.0142C14.9508 18.718 13.5659 19.0926 11.9998 19.0926C8.86633 19.0926 6.21896 17.0785 5.27682 14.2695L1.2373 17.3366C3.19263 21.2953 7.26484 24.0017 11.9998 24.0017C14.9327 24.0017 17.7352 22.959 19.834 21.0012L16.0406 18.0142Z"
                                  fill="#34A853"
                              />
                              <path
                                  d="M19.8342 20.9978C22.0292 18.9503 23.4545 15.9019 23.4545 11.9982C23.4545 11.2891 23.3455 10.5255 23.1818 9.81641H12V14.4528H18.4364C18.1188 16.0119 17.2663 17.2194 16.0407 18.0108L19.8342 20.9978Z"
                                  fill="#4A90E2"
                              />
                              <path
                                  d="M5.27698 14.2663C5.03833 13.5547 4.90909 12.7922 4.90909 11.9984C4.90909 11.2167 5.03444 10.4652 5.2662 9.76294L1.23999 6.64844C0.436587 8.25884 0 10.0738 0 11.9984C0 13.918 0.444781 15.7286 1.23746 17.3334L5.27698 14.2663Z"
                                  fill="#FBBC05"
                              />
                          </svg>
                          Sign in with Google
                      </button>
                  </div>
                  <div className="mt-8 flex justify-center items-center">
                      <p className="text-black font-medium text-base">
                          Don't have an account?
                      </p>
                      <button
                          className="ml-2 font-medium text-base text-violet-500"
                          type="button"
                          onClick={() => navigate("/register")}
                      >
                          Sign up
                      </button>
                  </div>
              </form>
          </div>
      </div>
      <div className="hidden relative lg-flex-fix lg:flex h-full items-center w-1/2 justify-center bg-gray-200">
          <div className="bolinha w-60 h-60 bg-gradient-to-tr from-violet-500 to-pink-500 rounded-full animate-bounce"></div>
          <div className="w-full h-1/2 absolute bottom-0 bg-white/1 backdrop-blur-lg"></div>
      </div>
  </div>
    );
};

export default Login;

