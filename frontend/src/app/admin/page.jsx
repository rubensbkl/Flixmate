"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import Navbar from "@/components/Navbar";

export default function AdminPage() {
    const { user, loading } = useAuth();
    const router = useRouter();
    const [users, setUsers] = useState([]);
    const [isLoadingUsers, setIsLoadingUsers] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const verifyAdmin = async () => {
            if (loading) return;
            if (!user) {
                router.push("/login");
                return;
            }

            try {
                const token = localStorage.getItem("token");
                const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/private`, {
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (res.ok) {
                    const data = await res.json();
                    if (!data.user?.isAdmin) {
                        router.push("/profile/" + user.userId);
                    }
                } else {
                    router.push("/login");
                }
            } catch (err) {
                router.push("/");
            }
        };
        verifyAdmin();
    }, [user, loading, router]);

    useEffect(() => {
        const fetchUsers = async () => {
            if (!user) return;
            try {
                const token = localStorage.getItem("token");
                const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users`, {
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (!res.ok) throw new Error("Erro ao carregar usuários");
                const data = await res.json();
                setUsers(data.users || []);
            } catch (err) {
                setError(err.message);
            } finally {
                setIsLoadingUsers(false);
            }
        };
        fetchUsers();
    }, [user]);


    const handleResetPassword = async (id) => {
        const newPassword = prompt("Digite a nova senha para este usuário (mínimo 6 caracteres):");
        if (!newPassword) return;
        if (newPassword.length < 6) {
            alert("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        try {
            const token = localStorage.getItem("token");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${id}/password`, {
                method: "PUT",
                headers: { 
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ password: newPassword })
            });

            if (!res.ok) {
                const data = await res.json();
                throw new Error(data.error || "Erro ao atualizar senha");
            }

            alert("Senha atualizada com sucesso!");
        } catch (err) {
            alert("Erro: " + err.message);
        }
    };

    const handleDelete = async (id) => {
        if (!confirm("Tem certeza que deseja deletar este usuário?")) return;
        try {
            const token = localStorage.getItem("token");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/admin/users/${id}`, {
                method: "DELETE",
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Erro ao deletar");
            setUsers(users.filter((u) => u.id !== id));
        } catch (err) {
            alert(err.message);
        }
    };

    if (loading || !user) return <div className="min-h-screen bg-background flex items-center justify-center text-primary">Carregando...</div>;

    return (
        <div className="flex h-screen bg-background text-primary">
            <Navbar />
            <main className="flex-1 overflow-y-auto p-4 md:p-8 bg-surface">
                <div className="max-w-6xl mx-auto">
                    <h1 className="text-3xl font-bold mb-8">Painel Admin</h1>
                    
                    {error && (
                        <div className="bg-red-500/10 border border-red-500 text-red-500 p-4 rounded-xl mb-6">
                            {error}
                        </div>
                    )}

                    <div className="bg-foreground rounded-2xl shadow-xl overflow-hidden border border-white/5">
                        <div className="overflow-x-auto">
                            <table className="w-full text-left">
                                <thead className="bg-black/20 text-secondary text-sm font-medium uppercase tracking-wider">
                                    <tr>
                                        <th className="px-6 py-4">ID</th>
                                        <th className="px-6 py-4">Nome</th>
                                        <th className="px-6 py-4">Email</th>
                                        <th className="px-6 py-4">Admin</th>
                                        <th className="px-6 py-4">Ações</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-white/5">
                                    {isLoadingUsers ? (
                                        <tr>
                                            <td colSpan="5" className="px-6 py-8 text-center text-secondary">
                                                Carregando usuários...
                                            </td>
                                        </tr>
                                    ) : users.length === 0 ? (
                                        <tr>
                                            <td colSpan="5" className="px-6 py-8 text-center text-secondary">
                                                Nenhum usuário encontrado.
                                            </td>
                                        </tr>
                                    ) : (
                                        users.map((u) => (
                                            <tr key={u.id} className="hover:bg-white/5 transition-colors">
                                                <td className="px-6 py-4 text-secondary">{u.id}</td>
                                                <td className="px-6 py-4">{u.firstName} {u.lastName}</td>
                                                <td className="px-6 py-4">{u.email}</td>
                                                <td className="px-6 py-4">
                                                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${u.isAdmin ? 'bg-accent/20 text-accent' : 'bg-secondary/20 text-secondary'}`}>
                                                        {u.isAdmin ? 'Sim' : 'Não'}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="flex gap-2">
                                                        <button 
                                                            className="text-xs bg-red-500/20 text-red-500 hover:bg-red-500 hover:text-white px-3 py-1 rounded-lg transition-colors"
                                                            onClick={() => handleDelete(u.id)}
                                                        >
                                                            Deletar
                                                        </button>
                                                    <button 
                                                            className="text-xs bg-accent/20 text-accent hover:bg-accent hover:text-black px-3 py-1 rounded-lg transition-colors"
                                                            onClick={() => handleResetPassword(u.id)}
                                                        >
                                                            Nova Senha
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
