import "@/styles/globals.css";
import { Ubuntu, Ubuntu_Sans_Mono } from "next/font/google";
import { AuthProvider } from "@/contexts/AuthContext";
import SimpleAuthGuard from '@/components/SimpleAuthGuard';

const ubuntu = Ubuntu({
    variable: "--font-ubuntu",
    subsets: ["latin"],
    weight: ["300", "400", "500", "700"],
    style: ["normal", "italic"],
});

const ubuntuMono = Ubuntu_Sans_Mono ({
    variable: "--font-ubuntu-mono",
    subsets: ["latin"],
    weight: ["400", "700"],
});

export const metadata = {
    title: "Flixmate",
    description: " — filme certo, na hora certa.",
};

export default function RootLayout({ children }) {
    return (
        <html lang="en">
            <body
                className={`${ubuntu.variable} ${ubuntuMono.variable} antialiased bg-background`}
            >
                <AuthProvider>
                    <SimpleAuthGuard>
                        {children}
                    </SimpleAuthGuard>
                </AuthProvider>
            </body>
        </html>
    );
}