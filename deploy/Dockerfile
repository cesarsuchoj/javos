# ============================================================
# Javos - Dockerfile Multi-Stage Build
# ============================================================
# Stage 1: Build do Frontend
# Stage 2: Build do Backend
# Stage 3: Imagem final de produção
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

# ---- Stage 3: Imagem final ----
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

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
