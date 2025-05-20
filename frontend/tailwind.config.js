/** @type {import('tailwindcss').Config} */
const colors = require('tailwindcss/colors');

module.exports = {
    content: [
      "./src/**/*.{js,ts,jsx,tsx}"
    ],
    theme: {
      extend: {
        colors: {
            primary: "#eaecef",      // exemplo: azul escuro
            secondary: "#848e9c",  // usando a cor oficial do Tailwind
            accent: "#fcd535",    // outro exemplo de cor Tailwind
            background: "#181a20",   // cinza claro
            foreground: "#2d2f37",   // quase preto
        },
        height: {
            fit: "fit-content",
        },
      },
    },
    plugins: [],
  };