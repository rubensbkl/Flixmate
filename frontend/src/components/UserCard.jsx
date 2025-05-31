// components/UserCard.jsx

"use client";

import { UserIcon } from "@heroicons/react/24/solid";
import { useRouter } from "next/navigation";
import Image from "next/image";

export default function UserCard({ user }) {
    const router = useRouter();

    const handleClick = () => {
        router.push(`/profile/${user.id}`);
    };

    return (
        <div
            onClick={handleClick}
            className="bg-foreground rounded-xl p-4 flex items-center gap-4 cursor-pointer hover:bg-background transition"
        >
            <div className="w-12 h-12 bg-primary rounded-full flex items-center justify-center overflow-hidden">
                {user.profileImage ? (
                    <Image
                        src={user.profileImage}
                        alt={`${user.firstName} ${user.last_name}`}
                        width={48}
                        height={48}
                        className="rounded-full object-cover"
                    />
                ) : (
                    <UserIcon className="w-6 h-6 text-background" />
                )}
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
