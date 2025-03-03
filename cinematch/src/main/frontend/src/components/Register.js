import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import '../output.css';

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
    <div className="flex w-full h-screen overflow-auto">
    <div className="w-full flex items-center justify-center lg:w-1/2 p-4">
      <div className="bg-white px-6 py-10 md:px-10 md:py-20 rounded-3xl border-2 border-gray-200 max-w-md w-full">
        <h1 className="text-3xl md:text-5xl font-semibold">CineMatch Registro</h1>
        <p className="subtitle">
          Crie sua conta
        </p>

     
        <form onSubmit={handleSubmit} className="mt-8">
          <div>
            <label className="text-sm md:text-lg font-medium" htmlFor="firstName">
              Nome
            </label>
            <input
              id="firstName"
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              placeholder="Escreva seu nome"
              required
              className="w-full border-2 border-gray-200 rounded-xl p-2 md:p-3 m-1 bg-transparent"
            />
          </div>
          <div>
            <label className="text-sm md:text-lg font-medium" htmlFor="lastName">
              Sobrenome
            </label>
            <input
              id="lastName"
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              placeholder="Escreva seu sobrenome"
              required
              className="w-full border-2 border-gray-200 rounded-xl p-2 md:p-3 m-1 bg-transparent"
            />
          </div>
          <div>
            <label className="text-sm md:text-lg font-medium" htmlFor="email">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Escreva seu email"
              required
              className="w-full border-2 border-gray-200 rounded-xl p-2 md:p-3 m-1 bg-transparent"
            />
          </div>
          <div>
            <label className="text-sm md:text-lg font-medium" htmlFor="password">
              Senha
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Escreva sua senha"
              required
              className="w-full border-2 border-gray-200 rounded-xl p-2 md:p-3 m-1 bg-transparent"
            />
          </div>
          <div>
            <label className="text-sm md:text-lg font-medium" htmlFor="confirmPassword">
              Confirme sua senha
            </label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirme sua senha"
              required
              className="w-full border-2 border-gray-200 rounded-xl p-2 md:p-3 m-1 bg-transparent"
            />
          </div>
          <div>
            <label className="text-sm md:text-lg font-medium" htmlFor="gender">
              Gênero
            </label>
            <select
              id="gender"
              value={gender}
              onChange={(e) => setGender(e.target.value)}
              required
              className="w-full border-2 border-gray-200 rounded-xl p-2 md:p-3 m-1 bg-transparent"
            >
              <option value="M">Masculino</option>
              <option value="F">Feminino</option>
              <option value="O">Outro</option>
            </select>
          </div>
          <div className="mt-8 flex flex-col gap-y-4">
            <button className=" text-white  active:scale-[.98] active:duration-75 transition-all hover:scale-[1.01] ease-in-out transform py-2 md:py-3 bg-violet-500 rounded-xl font-bold text-sm md:text-lg">
              Registrar
            </button>
            <button className="flex items-center justify-center gap-2 active:scale-[.98] active:duration-75 transition-all hover:scale-[1.01] ease-in-out transform py-2 md:py-3 rounded-xl text-gray-700 font-semibold text-sm md:text-lg border-2 border-gray-100">
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
              Registrar com Google
            </button>
          </div>
          <div className="mt-8 flex justify-center items-center">
            <p className="font-medium text-sm md:text-base">Já tem uma conta?</p>
            <button
              className="ml-2 font-medium text-sm md:text-base text-violet-500"
              type="button"
              onClick={() => navigate("/login")}
            >
              Entrar
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

export default Register;