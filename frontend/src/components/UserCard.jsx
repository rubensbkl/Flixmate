"use client";

import { UserIcon } from "@heroicons/react/24/solid";
import { useRouter } from "next/navigation";

export default function UserCard({ user }) {
    const router = useRouter();

    const handleClick = () => {
        router.push(`/profile/${user.id}`);
    };

    return (
        <div
            onClick={handleClick}
            className="bg-foreground rounded-xl p-4 flex items-center gap-4 cursor-pointer transition-all hover:bg-accent/10 hover:shadow-lg"
        >
            <div className="h-12 w-12 rounded-full bg-foreground flex items-center justify-center border-2 border-accent/20 overflow-hidden">
                                    <UserIcon className="h-6 w-6 text-secondary" />
                                </div>
            <div className="overflow-hidden">
                <h3 className="font-medium text-primary truncate">
                    {user.first_name} {user.last_name}
                </h3>
                <p className="text-sm text-secondary truncate">
                    {user.email}
                </p>
            </div>
        </div>
    );
}
