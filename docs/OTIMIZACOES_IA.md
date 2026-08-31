# Otimizações de Inteligência Artificial (FastAPI / Sklearn)

Este documento registra as melhorias de performance, concorrência e uso de memória aplicadas no microsserviço de Inteligência Artificial (`ai/inference.py` e `ai/recommender.py`) do Flixmate.

## 1. Concorrência e Retreinamento Assíncrono (`inference.py`)
Antes, os retreinos automáticos e induzidos da IA bloqueavam as respostas das rotas, prejudicando a UX (causando travamentos de *swipe* e *timeouts* no frontend).
- **BackgroundTasks:** Refatoramos a chamada `/train` para aceitar a carga de dados, devolver imediatamente um **HTTP 202 Accepted**, e enfileirar o treinamento pesado no *BackgroundTasks* nativo do FastAPI.
- A rota `/recommend` também foi adaptada para que, caso um retreino automático seja engatilhado (`should_retrain() == True`), ele seja despachado de modo invisível para o usuário, garantindo uma recomendação instantânea.

## 2. Otimização de Memória e Construção de Matrizes Esparsas (`recommender.py`)
Duas grandes falhas de vazamento de performance foram consertadas na classe base do modelo colaborativo e de conteúdo:

- **Matriz CSR Nativa (O(1) Memory Overhead):**
A construção da matriz usuário-item (necessária para o `TruncatedSVD`) era feita usando uma tabela pivô do Pandas `.pivot().fillna(0)`. Isso instanciava os valores zerados diretamente na memória RAM, o que causaria um crash fatal na máquina (Out of Memory) assim que a base passasse de algumas centenas de usuários.
  - **A Solução:** Convertemos os dados diretamente usando colunas categóricas (`astype("category")`) e geramos uma Matriz Esparsa (CSR) no formato nativo `csr_matrix((ratings, (user_indices, movie_indices)))`. O uso de RAM agora é irrisório, apenas mapeando as avaliações que de fato aconteceram.

- **Falso Cache de TF-IDF Resolvido:**
O código original tinha um *handler* para recuperar a matriz TF-IDF do Redis, mas continha apenas um comando `pass`, recalculando e descartando a vetorização em tempo real toda vez.
  - **A Solução:** O sistema agora salva efetivamente as matrizes esparsas serializadas no Redis (`data`, `indices`, `indptr`, `shape`) e consegue instanciar instantaneamente a matriz a partir do cache sem acionar a CPU.

---

## 🏗️ Como Rodar e Testar o Sistema

O repositório foi construído para utilizar containers. A diferença principal entre DEV e PROD é o gerenciamento de volumes e variáveis.

### 💻 Ambiente de Desenvolvimento (DEV)
Ideal para modificar o código e testar com *Live Reloading* ativo (seu código reinicia o server sozinho a cada modificação):
```bash
# Sobe o banco Postgres, o Redis, o Backend Spring Boot e a IA em modo dev
docker-compose -f docker-compose.dev.yml up -d --build
```
- Você pode acompanhar os logs do Spring Boot ou da IA usando:
  `docker-compose -f docker-compose.dev.yml logs -f backend` ou `ai`

### 🚀 Ambiente de Produção (PROD)
Ideal para a apresentação e implantação no servidor. Aqui, os arquivos são cacheados e não possuem "live reloading" habilitado, poupando CPU.
```bash
# Sobe o sistema com as configurações de produção
docker-compose -f docker-compose.yml up -d --build
```

### Dicas de Teste e Monitoramento
1. **Teste a IA:**
   No seu navegador, acesse o painel da IA em `http://localhost:8000/docs`. Você poderá simular chamadas e ver os logs assíncronos no terminal sem travar a interface.
2. **Monitoramento do Backend:**
   Seu backend estará escutando no Spring Boot em `http://localhost:8080/api`. Tente mandar uma request pro `/api/ping` ou logar para verificar a conexão unificada via HikariCP!
