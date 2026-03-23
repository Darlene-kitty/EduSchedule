FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# ---- Runtime ----
FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app/dist/frontend-angular ./dist/frontend-angular
COPY --from=builder /app/node_modules ./node_modules

ENV PORT=4200
ENV NODE_ENV=production

EXPOSE 4200

CMD ["node", "dist/frontend-angular/server/server.mjs"]
