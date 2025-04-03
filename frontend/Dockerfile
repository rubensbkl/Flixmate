# Etapa 1: build
FROM node:20-alpine AS builder

# Recebe o valor da API no build (ex: /api)
# O valor padrão é o mesmo que o da API interna
ARG NEXT_PUBLIC_API_URL  
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

# Recebe o valor da API interna no build (ex: http://localhost:8080)
ARG INTERNAL_API_URL
ENV INTERNAL_API_URL=$INTERNAL_API_URL

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# IMPORTANTE: esse valor será embutido no momento do build
RUN npm run build

# Etapa 2: produção
FROM node:20-alpine AS runner

WORKDIR /app
ENV NODE_ENV=production

# Copia só o necessário
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/node_modules ./node_modules

EXPOSE 3000
CMD ["npm", "run", "start"]