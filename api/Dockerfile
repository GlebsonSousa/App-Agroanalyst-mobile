# Imagem base com versão fixada
FROM node:22-alpine

WORKDIR /app

# Dependências primeiro, para aproveitar o cache das camadas
COPY package*.json ./
RUN npm ci --omit=dev

# Código da API
COPY . .

ENV NODE_ENV=production
EXPOSE 3000

# Usuário sem privilégios, que já existe na imagem
USER node

CMD ["node", "server.js"]
