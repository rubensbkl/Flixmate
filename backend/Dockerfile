# Usa a imagem oficial do Maven para compilar o projeto
FROM maven:3.8.8-eclipse-temurin-17 AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia os arquivos do projeto para o container
COPY . .

# Executa o build do projeto
RUN mvn clean package -DskipTests

# Usa a imagem oficial do OpenJDK 17 (ou a versão que você estiver usando)
FROM openjdk:11

# Define o diretório de trabalho dentro do container
WORKDIR /app

# # Copia o arquivo .env para dentro do container
# COPY .env .env

# Copia o JAR gerado pelo estágio anterior
COPY --from=builder /app/target/cinematch-0.0.1-SNAPSHOT.jar app.jar

# Copia a pasta public para dentro do container
COPY public /app/public

# Define o comando para executar a aplicação
CMD ["java", "-jar", "app.jar"]