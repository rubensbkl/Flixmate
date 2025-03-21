"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function DashboardPage() {
  const [user, setUser] = useState(null);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      router.push("/login");
      return;
    }

    fetch("http://localhost:6789/api/protected", {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
      .then(async res => {
        if (!res.ok) {
          router.push("/login");
          return;
        }
        const data = await res.json();
        setUser(data.message);
      })
      .catch(() => router.push("/login"));
  }, []);

  return (
    <div>
      <h1>Dashboard</h1>
      {user ? <p>Bem-vindo, {user}</p> : <p>Carregando...</p>}
    </div>
  );
}
