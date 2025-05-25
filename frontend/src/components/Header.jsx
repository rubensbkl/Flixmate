import Image from "next/image";

export default function Header() {
    return (
        <header className="w-full">
            <div className="container mx-auto py-4 flex justify-center ">
                <Image 
                    src="/flixmate-logo.svg" 
                    alt="CineMatch Logo" 
                    width={40} 
                    height={40} 
                    className="invert h-auto"
                    style={{ filter: "invert(1)" }}                />
            </div>
        </header>
    );
}