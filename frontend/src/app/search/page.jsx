import Header from '@/components/Header'
import Navbar from '@/components/Navbar'

export default function HistoricoPage() {
  return (
    <main className="min-h-screen pb-16 md:pb-4">

      <Header />

      <h1 className="text-2xl font-bold p-4">Histórico</h1>
      
      <section className="px-4">
        {/* Exemplo de dados de histórico */}
        <ul className="space-y-4">
          <li className="border p-4 rounded shadow-sm">Item 1 do histórico</li>
          <li className="border p-4 rounded shadow-sm">Item 2 do histórico</li>
          <li className="border p-4 rounded shadow-sm">Item 3 do histórico</li>
        </ul>
      </section>

      <Navbar />
    </main>
  )
}
