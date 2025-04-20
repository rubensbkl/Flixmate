# 🎥 Flixmate

O **Flixmate** é uma plataforma inteligente de recomendação de filmes, que utiliza **Inteligência Artificial** para sugerir títulos personalizados com base nas preferências e avaliações dos usuários. A aplicação combina um backend robusto em **Java**, um frontend dinâmico em **Next.js** e uma infraestrutura moderna baseada em **Docker**, garantindo fácil instalação e execução tanto em ambientes de desenvolvimento quanto em produção.

## 👥 Alunos integrantes da equipe

- Bernardo Vieira Rocha
- Carlos Eduardo de Melo Sabino
- Felipe Costa Unsonst
- Rubens Dias Bicalho

## 🎓 Professores responsáveis

- Walisson Ferreira de Carvalho
- Wladmir Cardoso Brandao

---

## 🛠️ Tecnologias utilizadas

- **Java 17** (Backend)
- **Next.js (React)** (Frontend)
- **Docker** e **Docker Compose**
- **Nginx** (produção)
- **Certbot** (produção para SSL)

---

## ✅ Requisitos

Antes de iniciar, verifique se você tem instalado:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)

---

## 📐 Arquitetura do Projeto

````bash
flixmate/
├── backend/
│ └── Dockerfile
│ └── src/
├── frontend/
│ └── Dockerfile
│ └── src/
├── docker-compose.dev.yml
├── docker-compose.yml
├── .env (personalizado) 
└── README.md
````

---

## 📦 Como rodar o projeto

### 1️⃣ Clonar o repositório

````bash
git clone https://github.com/seu-usuario/flixmate.git
cd flixmate
````

### 2️⃣ Configurar variáveis de ambiente

Personalize as environment keys no arquivo docker-compose.dev.yml ou docker-compose.yml conforme necessário.
Exemplo:

````bash
ENV=dev || production
PORT=6789
DB_HOST=your_db_host_here
DB_NAME=your_db_name_here
DB_PORT=5432
DB_USER=your_db_user_here
DB_PASSWORD=your_db_password_here
JWT_SECRET=your_jwt_secret_here
TMDB_API_KEY=your_tmdb_api_key_here
AZURE_OPENAI_ENDPOINT=your_azure_openai_endpoint_here
AZURE_OPENAI_API_KEY=your_azure_openai_api_key_here
AZURE_OPENAI_DEPLOYMENT_NAME=your_azure_openai_deployment_name_here
````

### 3️⃣ Rodar no ambiente de desenvolvimento

Renomeie o arquivo `docker-compose-example.dev.yml` para `docker-compose.dev.yml`

````bash
mv docker-compose-example.dev.yml docker-compose.dev.yml
````

Execute o Docker Compose:

````bash
docker compose -f docker-compose.dev.yml up --build
````

A aplicação será disponibilizada localmente:

- Frontend: <http://localhost:3000>
- Backend: <http://localhost:6789>

### 4️⃣ Rodar no ambiente de produção

Renomeie o arquivo `docker-compose-example.yml` para `docker-compose.yml`

````bash
mv docker-compose-example.yml docker-compose.yml
````

Execute o Docker Compose:

````bash
docker compose -f docker-compose.yml up --build
````

A aplicação será disponibilizada:

- Frontend: <https://flixmate.com.br>
- Backend: <https://flixmate.com/api>
- Com **Nginx** configurado como proxy reverso e **Certbot** gerando certificados SSL automaticamente.

## 📌 Notas

- Certifique-se de que as portas utilizadas (3000, 6789, 5432, 80, 443) estejam liberadas.
- Para usar o ambiente de produção, é necessário configurar corretamente o domínio e acesso root para o Certbot emitir certificados.

## 📃 Licença

Este projeto é acadêmico e faz parte do Trabalho Interdisciplinar do curso de Ciência da Computação da **PUC Minas**.

## 📞 Contato

Para dúvidas, sugestões ou contribuições:

- [Bernardo Vieira Rocha](https://github.com/bernardovieirarocha)
- [Carlos Eduardo de Melo Sabino](https://github.com/cadumeloo)
- [Felipe Costa Unsonst](https://github.com/felipeunsonst)
- [Rubens Dias Bicalho](https://github.com/rubensbkl)