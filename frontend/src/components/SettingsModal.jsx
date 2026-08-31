"use client";

import React, { useState } from "react";
import { XMarkIcon, EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";

import { useAuth } from "@/contexts/AuthContext";

export default function SettingsModal({ isOpen, onClose }) {
    const { user, login } = useAuth(); // Need login to update the user in context if needed, or we just rely on page refresh/context update
    const [activeTab, setActiveTab] = useState("seguranca");
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState({ type: "", text: "" });
    const [email, setEmail] = React.useState(user?.email || "");
    const [emailMessage, setEmailMessage] = React.useState({ type: "", text: "" });
    const [passwordModal, setPasswordModal] = React.useState(null);
    const [showPassword, setShowPassword] = React.useState({ current: false, new: false, confirm: false });

    
    const handleUpdateEmail = async (e) => {
        e.preventDefault();
        if (!email.trim() || !user) return;
        
        setIsLoading(true);
        setEmailMessage({ type: "", text: "" });
        try {
            const token = localStorage.getItem("token");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/profile/update`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ email })
            });

            const data = await res.json();
            if (res.ok) {
                setEmailMessage({ type: "success", text: "Email atualizado com sucesso!" });
                login(data.user, token); // Update local context
            } else {
                setEmailMessage({ type: "error", text: data.error || "Erro ao atualizar email." });
            }
        } catch (error) {
            setEmailMessage({ type: "error", text: "Erro de conexão com o servidor." });
        } finally {
            setIsLoading(false);
        }
    };

    const handlePasswordUpdate = async (e) => {
        e.preventDefault();
        if (!newPassword) {
            setMessage({ type: "error", text: "A nova senha é obrigatória." });
            return;
        }
if (newPassword !== confirmPassword) {
            setMessage({ type: "error", text: "A nova senha e a confirmação não coincidem." });
            return;
        }
        if (newPassword.length < 6) {
            setMessage({ type: "error", text: "A nova senha deve ter pelo menos 6 caracteres." });
            return;
        }
        if (user?.hasPassword && !currentPassword) {
            setMessage({ type: "error", text: "A senha atual é obrigatória." });
            return;
        }

        setIsLoading(true);
        setMessage({ type: "", text: "" });
        try {
            const token = localStorage.getItem("token");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/profile/password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ currentPassword, newPassword })
            });

            const data = await res.json();
            if (res.ok) {
                setMessage({ type: "success", text: "Senha atualizada com sucesso!" });
                setCurrentPassword("");
                setNewPassword("");
                setConfirmPassword("");
                setTimeout(() => window.location.reload(), 1000);
            } else {
                setMessage({ type: "error", text: data.error || "Erro ao atualizar senha." });
            }
        } catch (error) {
            setMessage({ type: "error", text: "Erro de conexão com o servidor." });
        } finally {
            setIsLoading(false);
        }
    };

    
    const handleRemovePassword = async (e) => {
        if (e) e.preventDefault();
        if (!currentPassword) {
            setMessage({ type: "error", text: "Digite a sua senha atual para removê-la." });
            return;
        }

        setIsLoading(true);
        setMessage({ type: "", text: "" });
        try {
            const token = localStorage.getItem("token");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/profile/password`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ currentPassword })
            });

            const data = await res.json();
            if (res.ok) {
                setMessage({ type: "success", text: "Senha removida com sucesso!" });
                setCurrentPassword("");
                setNewPassword("");
                setConfirmPassword("");
                // Atualizar o estado do usuário (hasPassword)
                setTimeout(() => window.location.reload(), 1000);
            } else {
                setMessage({ type: "error", text: data.error || "Erro ao remover senha." });
            }
        } catch (error) {
            setMessage({ type: "error", text: "Erro de conexão com o servidor." });
        } finally {
            setIsLoading(false);
        }
    };

    const handleOAuthConnect = async (provider) => {
        localStorage.setItem('oauth_action', 'connect');
        try {
            const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6789';
            const res = await fetch(`${baseUrl}/api/oauth/${provider}/url`);
            const data = await res.json();
            if (data.url) {
                window.location.href = data.url;
            } else {
                setMessage({ type: "error", text: data.error || "Erro ao conectar com provedor." });
            }
        } catch (err) {
            setMessage({ type: "error", text: "Erro ao se conectar ao servidor." });
        }
    };

    const handleOAuthDisconnect = async (provider) => {
        if (!confirm(`Tem certeza que deseja desconectar o ${provider}?`)) return;
        setIsLoading(true);
        try {
            const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6789';
            const token = localStorage.getItem("token");
            const res = await fetch(`${baseUrl}/api/profile/oauth/disconnect`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ provider })
            });
            const data = await res.json();
            if (res.ok) {
                setMessage({ type: "success", text: `${provider} desconectado com sucesso!` });
                // We update the local context user object manually
                const updatedUser = { ...user };
                if (provider === 'google') updatedUser.googleConnected = false;
                if (provider === 'github') updatedUser.githubConnected = false;
                login(updatedUser, token);
            } else {
                setMessage({ type: "error", text: data.error || "Erro ao desconectar provedor." });
            }
        } catch (err) {
            setMessage({ type: "error", text: "Erro ao se conectar ao servidor." });
        } finally {
            setIsLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="bg-background border border-foreground w-full max-w-2xl rounded-2xl shadow-2xl flex flex-col md:flex-row overflow-hidden max-h-[90vh]">
                
                {/* Sidebar do Modal */}
                <div className="w-full md:w-64 bg-foreground/50 border-b md:border-b-0 md:border-r border-foreground p-4">
                    <div className="flex justify-between items-center md:mb-6">
                        <h2 className="text-xl font-bold text-primary">Configurações</h2>
                        <button 
                            onClick={onClose}
                            className="md:hidden p-1 text-secondary hover:text-primary transition-colors"
                        >
                            <XMarkIcon className="w-6 h-6" />
                        </button>
                    </div>
                    
                    <nav className="flex md:flex-col gap-2 overflow-x-auto md:overflow-x-visible pb-2 md:pb-0">
                        <button 
                            onClick={() => setActiveTab("seguranca")}
                            className={`px-4 py-2 text-left rounded-lg transition-colors whitespace-nowrap ${activeTab === "seguranca" ? "bg-accent/20 text-accent font-medium" : "text-secondary hover:bg-white/5 hover:text-primary"}`}
                        >
                            Segurança
                        </button>
                        {/* Podemos adicionar outras abas no futuro */}
                    </nav>
                </div>

                {/* Conteúdo Principal do Modal */}
                <div className="flex-1 p-6 overflow-y-auto relative">
                    <button 
                        onClick={onClose}
                        className="hidden md:block absolute top-6 right-6 p-1 text-secondary hover:text-primary transition-colors bg-foreground/50 rounded-full"
                    >
                        <XMarkIcon className="w-5 h-5" />
                    </button>

                    {activeTab === "seguranca" && (
                        <div className="space-y-8 animate-fadeIn">
                            <div className="pb-6 border-b border-foreground">
                                <h3 className="text-lg font-medium text-primary mb-1">Email da Conta</h3>
                                <p className="text-sm text-secondary mb-4">Atualize o endereço de email principal da sua conta.</p>
                                
                                <form onSubmit={handleUpdateEmail} className="space-y-4 max-w-md">
                                    {emailMessage.text && (
                                        <div className={`p-3 rounded-lg text-sm ${emailMessage.type === 'error' ? 'bg-red-500/20 text-red-500 border border-red-500/30' : 'bg-green-500/20 text-green-500 border border-green-500/30'}`}>
                                            {emailMessage.text}
                                        </div>
                                    )}
                                    <div className="flex gap-3">
                                        <input 
                                            type="email" 
                                            placeholder="Seu email"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            className="flex-1 p-3 bg-foreground border border-foreground rounded-lg focus:border-accent focus:outline-none transition-colors text-primary"
                                        />
                                        <button 
                                            type="submit" 
                                            disabled={isLoading || email === user?.email}
                                            className="px-4 py-2 bg-white/10 text-white rounded-lg font-medium hover:bg-white/20 transition-colors disabled:opacity-50"
                                        >
                                            Atualizar
                                        </button>
                                    </div>
                                </form>
                            </div>

                            <div>
                                <h3 className="text-lg font-medium text-primary mb-1">
                                    Segurança - Credencial: 
                                    <span className={user?.hasPassword ? "text-green-500 ml-2" : "text-red-500 ml-2"}>
                                        {user?.hasPassword ? "Cadastrada" : "Não cadastrada"}
                                    </span>
                                </h3>
                                <p className="text-sm text-secondary mb-4">
                                    {user?.hasPassword ? "Você pode alterar ou remover a sua senha." : "Você não possui uma senha. Cadastre uma abaixo."}
                                </p>
                                
                                <div className="flex gap-3">
                                    {user?.hasPassword ? (
                                        <>
                                            <button 
                                                onClick={() => { setPasswordModal('change'); setMessage({type:'', text:''}); setCurrentPassword(''); setNewPassword(''); setConfirmPassword(''); }}
                                                className="px-4 py-2 bg-accent text-background rounded-lg font-medium hover:bg-accent/90 transition-colors"
                                            >
                                                Mudar Senha
                                            </button>
                                            <button 
                                                onClick={() => { setPasswordModal('remove'); setMessage({type:'', text:''}); setCurrentPassword(''); }}
                                                className="px-4 py-2 bg-red-500/20 text-red-500 border border-red-500/30 rounded-lg font-medium hover:bg-red-500 hover:text-white transition-colors"
                                            >
                                                Remover
                                            </button>
                                        </>
                                    ) : (
                                        <button 
                                            onClick={() => { setPasswordModal('define'); setMessage({type:'', text:''}); setNewPassword(''); setConfirmPassword(''); }}
                                            className="px-4 py-2 bg-accent text-background rounded-lg font-medium hover:bg-accent/90 transition-colors"
                                        >
                                            Cadastrar Senha
                                        </button>
                                    )}
                                </div>
                            </div>

                            <div className="pt-6 border-t border-foreground">
                                <h3 className="text-lg font-medium text-primary mb-1">Contas Vinculadas (OAuth)</h3>
                                <p className="text-sm text-secondary mb-4">Conecte sua conta para fazer login mais rápido.</p>
                                
                                <div className="space-y-3">
                                    <div className="flex items-center justify-between p-4 bg-foreground/30 rounded-xl border border-foreground">
                                        <div className="flex items-center gap-3">
                                            <div className="bg-white p-2 rounded-full">
                                                <svg className="w-5 h-5" viewBox="0 0 24 24">
                                                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                                </svg>
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium text-primary">Google</p>
                                                <p className="text-xs text-secondary">Acesse com Google</p>
                                            </div>
                                        </div>
                                        {user?.googleConnected ? (
                                            <button 
                                                onClick={() => handleOAuthDisconnect('google')}
                                                disabled={isLoading}
                                                className="px-3 py-1.5 bg-red-500/20 hover:bg-red-500/30 text-red-500 text-sm font-medium rounded-lg transition-colors border border-red-500/30"
                                            >
                                                Desconectar
                                            </button>
                                        ) : (
                                            <button 
                                                onClick={() => handleOAuthConnect('google')}
                                                disabled={isLoading}
                                                className="px-3 py-1.5 bg-foreground hover:bg-white/10 text-primary text-sm font-medium rounded-lg transition-colors border border-white/5"
                                            >
                                                Conectar
                                            </button>
                                        )}
                                    </div>
                                    
                                    <div className="flex items-center justify-between p-4 bg-foreground/30 rounded-xl border border-foreground">
                                        <div className="flex items-center gap-3">
                                            <div className="bg-white p-2 rounded-full">
                                                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                                                    <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd" />
                                                </svg>
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium text-primary">GitHub</p>
                                                <p className="text-xs text-secondary">Acesse com GitHub</p>
                                            </div>
                                        </div>
                                        {user?.githubConnected ? (
                                            <button 
                                                onClick={() => handleOAuthDisconnect('github')}
                                                disabled={isLoading}
                                                className="px-3 py-1.5 bg-red-500/20 hover:bg-red-500/30 text-red-500 text-sm font-medium rounded-lg transition-colors border border-red-500/30"
                                            >
                                                Desconectar
                                            </button>
                                        ) : (
                                            <button 
                                                onClick={() => handleOAuthConnect('github')}
                                                disabled={isLoading}
                                                className="px-3 py-1.5 bg-foreground hover:bg-white/10 text-primary text-sm font-medium rounded-lg transition-colors border border-white/5"
                                            >
                                                Conectar
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Password Action Sub-Modal */}
            {passwordModal && (
                <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
                    <div className="bg-background border border-foreground w-full max-w-md rounded-2xl shadow-2xl p-6 relative">
                        <button 
                            onClick={() => setPasswordModal(null)}
                            className="absolute top-4 right-4 p-1 text-secondary hover:text-primary transition-colors bg-foreground/50 rounded-full"
                        >
                            <XMarkIcon className="w-5 h-5" />
                        </button>
                        
                        <h3 className="text-xl font-bold text-primary mb-2">
                            {passwordModal === 'change' && 'Mudar Senha'}
                            {passwordModal === 'remove' && 'Remover Senha'}
                            {passwordModal === 'define' && 'Cadastrar Senha'}
                        </h3>
                        
                        <p className="text-sm text-secondary mb-6">
                            {passwordModal === 'change' && 'Insira sua senha atual e a nova senha.'}
                            {passwordModal === 'remove' && 'Tem certeza? Você só poderá entrar via provedores vinculados.'}
                            {passwordModal === 'define' && 'Crie uma senha para poder fazer login usando seu email.'}
                        </p>

                        <form onSubmit={passwordModal === 'remove' ? handleRemovePassword : handlePasswordUpdate} className="space-y-4">
                            {message.text && (
                                <div className={`p-3 rounded-lg text-sm ${message.type === 'error' ? 'bg-red-500/20 text-red-500 border border-red-500/30' : 'bg-green-500/20 text-green-500 border border-green-500/30'}`}>
                                    {message.text}
                                </div>
                            )}

                            {(passwordModal === 'change' || passwordModal === 'remove') && (
                                <div className="relative">
                                    <input 
                                        type={showPassword.current ? "text" : "password"} 
                                        placeholder="Senha atual"
                                        value={currentPassword}
                                        onChange={(e) => setCurrentPassword(e.target.value)}
                                        className="w-full p-3 pr-12 bg-foreground border border-foreground rounded-lg focus:border-accent focus:outline-none transition-colors text-primary"
                                    />
                                    <button 
                                        type="button"
                                        onClick={() => setShowPassword(prev => ({...prev, current: !prev.current}))}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-secondary hover:text-primary"
                                    >
                                        {showPassword.current ? <EyeSlashIcon className="w-5 h-5" /> : <EyeIcon className="w-5 h-5" />}
                                    </button>
                                </div>
                            )}

                            {(passwordModal === 'change' || passwordModal === 'define') && (
                                <>
                                    <div className="relative">
                                        <input 
                                            type={showPassword.new ? "text" : "password"} 
                                            placeholder="Nova senha"
                                            value={newPassword}
                                            onChange={(e) => setNewPassword(e.target.value)}
                                            className="w-full p-3 pr-12 bg-foreground border border-foreground rounded-lg focus:border-accent focus:outline-none transition-colors text-primary"
                                        />
                                        <button 
                                            type="button"
                                            onClick={() => setShowPassword(prev => ({...prev, new: !prev.new}))}
                                            className="absolute right-3 top-1/2 -translate-y-1/2 text-secondary hover:text-primary"
                                        >
                                            {showPassword.new ? <EyeSlashIcon className="w-5 h-5" /> : <EyeIcon className="w-5 h-5" />}
                                        </button>
                                    </div>
                                    <div className="relative">
                                        <input 
                                            type={showPassword.confirm ? "text" : "password"} 
                                            placeholder="Confirmar nova senha"
                                            value={confirmPassword}
                                            onChange={(e) => setConfirmPassword(e.target.value)}
                                            className="w-full p-3 pr-12 bg-foreground border border-foreground rounded-lg focus:border-accent focus:outline-none transition-colors text-primary"
                                        />
                                        <button 
                                            type="button"
                                            onClick={() => setShowPassword(prev => ({...prev, confirm: !prev.confirm}))}
                                            className="absolute right-3 top-1/2 -translate-y-1/2 text-secondary hover:text-primary"
                                        >
                                            {showPassword.confirm ? <EyeSlashIcon className="w-5 h-5" /> : <EyeIcon className="w-5 h-5" />}
                                        </button>
                                    </div>
                                </>
                            )}
                            
                            <div className="flex gap-3 pt-2">
                                <button 
                                    type="submit" 
                                    disabled={isLoading}
                                    className={`flex-1 px-4 py-2 rounded-lg font-medium transition-colors disabled:opacity-50 ${passwordModal === 'remove' ? 'bg-red-500 text-white hover:bg-red-600' : 'bg-accent text-background hover:bg-accent/90'}`}
                                >
                                    {isLoading ? 'Aguarde...' : 'Confirmar'}
                                </button>
                                <button 
                                    type="button" 
                                    onClick={() => setPasswordModal(null)}
                                    disabled={isLoading}
                                    className="px-4 py-2 bg-foreground text-primary rounded-lg font-medium hover:bg-white/10 transition-colors disabled:opacity-50"
                                >
                                    Cancelar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
