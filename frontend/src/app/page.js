"use client";

import TinderCards from "@/components/TinderCards";
import { useEffect, useState } from "react";

export default function Home() {
  const [apiStatus, setApiStatus] = useState(null);

  useEffect(() => {
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/ping`)
      .then((res) => res.json())
      .then((data) => setApiStatus(data.message))
      .catch(() => setApiStatus("❌ Falha ao conectar com a API"));
  }, []);

  return (
    <div>
      <main>
        <TinderCards />
        <div style={{ marginTop: "2rem", fontWeight: "bold" }}>
          API: {apiStatus || "Verificando..."}
        </div>
      </main>
      <footer>
        <div>Footer</div>
      </footer>
    </div>
  );
}