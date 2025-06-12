/** @type {import('next').NextConfig} */
const nextConfig = {
    output: 'standalone',
    
    images: {
        remotePatterns: [
            {
                protocol: 'https',
                hostname: 'image.tmdb.org',
                port: '',
                pathname: '/**',
            },
        ],
        unoptimized: false,
    },
    
    env: {
        NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
    },
    
    compress: true,
    poweredByHeader: false,
    serverExternalPackages: [],
    
    experimental: {
        optimizePackageImports: ['@heroicons/react', 'lucide-react'],
        turbo: {
            memoryLimit: 512,
            rules: {
                '*.svg': {
                    loaders: ['@svgr/webpack'],
                    as: '*.js',
                },
            },
            resolveAlias: {
                '@': './src',
            },
        },
    },
};

export default nextConfig;