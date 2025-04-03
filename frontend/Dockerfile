# Etapa 1: build
FROM node:20-alpine AS builder

# Recebe o valor da API no build (ex: /api)
# O valor padrão é o mesmo que o da API interna
ARG NEXT_PUBLIC_API_URL  
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# IMPORTANTE: esse valor será embutido no momento do build
RUN npm run build

# Etapa 2: produção
FROM node:20-alpine AS runner

WORKDIR /app

# Copia só o necessário
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/node_modules ./node_modules

# Copia os arquivos de configuração necessários para o Tailwind
COPY --from=builder /app/postcss.config.mjs ./
COPY --from=builder /app/next.config.mjs ./
# Copia os estilos do projeto
COPY --from=builder /app/src/styles ./src/styles

# Adicione permissões adequadas para segurança
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs
RUN chown -R nextjs:nodejs /app/.next
USER nextjs

EXPOSE 3000
CMD ["npm", "run", "start"]