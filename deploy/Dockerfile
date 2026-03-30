# ============================================================
# Javos - Dockerfile Multi-Stage Build
# ============================================================
# Stage 1: Build do Frontend (Node.js)
# Stage 2: Build do Backend com frontend embarcado (Maven)
# Stage 3: Imagem final de produção (backend + frontend no JAR)
# Stage 4: nginx com frontend estático (produção multi-container)
# ============================================================

# ---- Stage 1: Build do Frontend ----
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
# Build estático vai para dist/ no WORKDIR
RUN npm run build -- --outDir=dist

# ---- Stage 2: Build do Backend ----
FROM eclipse-temurin:21-jdk-alpine AS backend-builder

WORKDIR /app/backend
COPY backend/pom.xml ./
COPY backend/src ./src

# Copia o build estático do frontend para resources/static
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN apk add --no-cache maven && \
    mvn package -DskipTests --no-transfer-progress

# ---- Stage 3: Imagem final (backend + frontend no JAR) ----
FROM eclipse-temurin:21-jre-alpine AS production

LABEL maintainer="Javos Contributors"
LABEL description="Javos - Sistema de gestão open-source"
LABEL license="GPL-3.0"

WORKDIR /app

# Cria diretório para o banco SQLite
RUN mkdir -p /app/data

# Copia o JAR gerado
COPY --from=backend-builder /app/backend/target/javos-*.jar app.jar

EXPOSE 8080

ENV SERVER_PORT=8080
ENV JWT_SECRET=change-this-in-production-to-a-secure-random-secret

HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# ---- Stage 4: nginx com frontend estático (produção multi-container) ----
FROM nginx:1.27-alpine AS nginx-frontend

LABEL maintainer="Javos Contributors"
LABEL description="Javos - nginx reverse proxy + frontend estático"
LABEL license="GPL-3.0"

# Copia os arquivos estáticos do frontend
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html

# Copia as configurações do nginx
COPY deploy/nginx/nginx.conf /etc/nginx/nginx.conf
COPY deploy/nginx/conf.d/javos.conf /etc/nginx/conf.d/default.conf

# Cria diretório para ACME challenge (Let's Encrypt)
RUN mkdir -p /var/www/certbot

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1
