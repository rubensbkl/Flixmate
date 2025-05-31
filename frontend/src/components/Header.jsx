import Image from "next/image";

export default function Header() {
    return (
        <header className="w-full">
            <div className="container mx-auto py-4 flex justify-center ">
                <Image 
                    src="/flixmate-logo-teste1.svg" 
                    alt="Flixmate Logo" 
                    width={40} 
                    height={40} 
                    className="h-auto"           />
            </div>
        </header>
    );
}