// filepath: /Users/berd/git/CineMatch/CineMatch/frontend/src/components/ErrorModal.jsx
import { ExclamationTriangleIcon, XCircleIcon } from "@heroicons/react/24/outline";

export default function ErrorModal({ isOpen, onClose, message }) {
    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
            onClick={onClose} // Close on backdrop click
        >
            <div
                className="bg-white rounded-lg shadow-xl p-6 w-full max-w-md relative"
                onClick={(e) => e.stopPropagation()} // Prevent closing when clicking inside modal
            >
                <button
                    onClick={onClose}
                    className="absolute top-2 right-2 text-gray-400 hover:text-gray-600"
                    aria-label="Fechar"
                >
                    <XCircleIcon className="w-6 h-6" />
                </button>
                <div className="flex items-center mb-4">
                    <ExclamationTriangleIcon className="w-8 h-8 text-red-500 mr-3" />
                    <h2 className="text-xl font-semibold text-gray-800">Atenção</h2>
                </div>
                <p className="text-gray-600">
                    {message || "Ocorreu um erro inesperado."}
                </p>
                <div className="mt-6 flex justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                    >
                        Fechar
                    </button>
                </div>
            </div>
        </div>
    );
}