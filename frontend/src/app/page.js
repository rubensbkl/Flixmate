export const dynamic = "force-dynamic";

import TinderCards from "@/components/TinderCards";

export default async function Home() {
  let apiStatus = "Verificando...";

  const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:6789/api";
  try {
    const res = await fetch(`${API_BASE}/ping`, {
      cache: "no-store",
    });
    const data = await res.json();
    apiStatus = data.message;
  } catch (err) {
    apiStatus = "❌ Falha ao conectar com a API";
    console.error("Erro ao buscar ping:", err);
  }

  return (
    <div>
      <main>
        <TinderCards />
        <div style={{ marginTop: "2rem", fontWeight: "bold" }}>
          API: {apiStatus}
        </div>
      </main>
      <footer>
        <div>Footer</div>
      </footer>
    </div>
  );
}