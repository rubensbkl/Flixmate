FROM python:3.11-slim

# Instalar dependências do sistema
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root antes de copiar arquivos
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Definir diretório de trabalho
WORKDIR /app

# Copiar requirements primeiro para cache de layers
COPY requirements.txt .

# Instalar dependências Python
RUN pip install --no-cache-dir -r requirements.txt

# Copiar arquivos da aplicação
COPY ./model_data ./model_data
COPY ./inference.py ./
COPY ./recommender.py ./
COPY ./3pretrain.py ./

# Ajustar permissões para o diretório de modelos
RUN chown -R appuser:appuser /app/model_data

# Trocar para o usuário seguro
USER appuser

# Expor a porta
EXPOSE 5005

# Iniciar a aplicação
CMD ["uvicorn", "inference:app", "--host", "0.0.0.0", "--port", "5005", "--workers", "1"]