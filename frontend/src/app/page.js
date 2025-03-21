import Image from "next/image";
import Header from "@/components/Header";
import TinderCards from "@/components/TinderCards";

export default function Home() {
  return (
    <div>
      <main>
        <TinderCards />
      </main>
      <footer>
        <div>Footer bct</div>
      </footer>
    </div>
  );
}
