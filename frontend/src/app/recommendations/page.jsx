import Navbar from '@/components/Navbar'
import MovieCard from '@/components/MovieCard'

export default function HistoricoPage() {
  const historico = [
    {
      id: 1,
      title: 'Carros',
      studio: 'Disney Pixar',
      description: 'Ação, Carros, Aventura',
      image: '/carros.jpg', // certifique-se que a imagem está em /public
    },
    // Pode adicionar mais itens
  ];

  return (
    <main className="min-h-screen pb-16 md:pb-4">
      <h1 className="text-2xl font-bold p-4">Histórico</h1>

      <section className="px-4">
        {/* Lista dinâmica de histórico usando MovieCard */}
        <ul className="space-y-4">
          {historico.map((movie) => (
            <li key={movie.id}>
              <MovieCard movie={movie} />
            </li>
          ))}
        </ul>
      </section>

      <Navbar />
    </main>
  );
}
