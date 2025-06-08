FROM python:3.11-slim

# Instalar dependências do sistema
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Definir diretório de trabalho
WORKDIR /app

# Copiar requirements primeiro para cache de layers
COPY requirements.txt .

# Instalar dependências Python
RUN pip install --no-cache-dir -r requirements.txt

# Copiar apenas arquivos necessários
COPY ./model_data ./model_data
COPY ./inference.py ./
COPY ./recommender.py ./
COPY ./3pretrain.py ./

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# Expor porta
EXPOSE 5005

# Comando para iniciar a aplicação
CMD ["uvicorn", "inference:app", "--host", "0.0.0.0", "--port", "5005", "--workers", "1"]