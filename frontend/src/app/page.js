import TinderCards from "@/components/TinderCards";

export default function Home({ apiStatus }) {
  return (
    <div>
      <main>
        <TinderCards />
        <div style={{ marginTop: "2rem", fontWeight: "bold" }}>
          API: {apiStatus || "❌ Falha ao conectar com a API"}
        </div>
      </main>
      <footer>
        <div>Footer</div>
      </footer>
    </div>
  );
}

// SSR: Executa no servidor (dentro do container, com acesso à rede interna)
export async function getServerSideProps() {
  try {
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/ping`);
    const data = await res.json();
    return {
      props: {
        apiStatus: data.message || "OK",
      },
    };
  } catch (error) {
    return {
      props: {
        apiStatus: "❌ Falha ao conectar com a API",
      },
    };
  }
}