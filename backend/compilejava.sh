#!/bin/bash

# Navega para o diretório backend (ajuste se necessário)
cd backend

# Limpa compilações anteriores
mvn clean

# Compila e empacota o JAR
mvn package -DskipTests

# Verifica se o JAR foi criado
echo "Verificando o JAR gerado:"
ls -l target/*.jar

# Verifica o manifesto do JAR
echo -e "\nConteúdo do MANIFEST.MF:"
unzip -p target/cinematch-0.0.1-SNAPSHOT.jar META-INF/MANIFEST.MF

# Tenta executar o JAR
echo -e "\nTentando executar o JAR:"
java -jar target/cinematch-0.0.1-SNAPSHOT.jar