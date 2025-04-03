import TinderCards from "@/components/TinderCards";

export default async function Home() {
  let apiStatus = "Verificando...";

  try {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/ping`, {
      cache: "no-store", // evita cache em dev/SSR
    });
    const data = await res.json();
    apiStatus = data.message;
    console.log("API Status:", apiStatus);
  } catch (err) {
    apiStatus = "❌ Falha ao conectar com a API";
    console.error("Error fetching API status:", err);
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