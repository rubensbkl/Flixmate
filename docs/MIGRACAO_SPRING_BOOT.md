# Migração para Spring Boot e Otimizações de Backend

Este documento detalha o processo de refatoração e migração do backend do projeto Flixmate, antes escrito em Spark Java, para o **Spring Boot**, além das otimizações arquiteturais implementadas com o objetivo de preparar a base de código para um nível empresarial.

## 1. O Problema Inicial

A documentação original (`ARCHITECTURE.md`) e a base de código apresentavam as seguintes **dívidas técnicas** e limitações:

- **Monolito de Rotas:** O arquivo `Application.java` concentrava 26 rotas e mais de 1500 linhas de código sem divisão de responsabilidades.
- **Gerenciamento de Banco Ineficiente:** Cada DAO gerava sua própria conexão de banco usando `DriverManager`, criando contenção e vazamento de recursos, sem o uso de um *Connection Pool*.
- **Consultas SQL Inseguras:** Os métodos `.getAll()` de `UserDAO` e `GenreDAO` usavam `Statement`, abrindo brechas teóricas e falhando em práticas ideais (o correto é `PreparedStatement`).
- **Problema de UX no Feed:** A rota de geração de *feed* (`/api/feed`) buscava todos os IDs da base e sorteava 500 candidatos de forma aleatória. Isso significava que filmes que o usuário já havia avaliado tornavam a aparecer com grande frequência na tela de *swipe*.

## 2. A Solução (Spring Boot)

Optou-se por migrar o núcleo de rede para o **Spring Boot**, que é o padrão da indústria, por resolver de forma unificada problemas de injeção de dependência, criação de rotas, configuração e gerenciamento de banco de dados.

### 2.1 Refatoração em Controllers (Separação de Responsabilidades)
O código monolítico foi substituído por uma arquitetura elegante baseada em `@RestController`. O antigo `Application.java` foi particionado em 4 arquivos dedicados no pacote `app.controller`:

- `AuthController.java`: Gerencia `/api/login`, `/api/register` e `/api/verify`.
- `FeedController.java`: Responsável por gerar o feed (`/api/feed`) e postar interações de swipe (`/api/rate`).
- `ProfileController.java`: Cuida da busca de dados do usuário, *watchlist*, favoritos, recomendações salvas e pesquisa social.
- `RecommendationController.java`: Orquestra as requisições diretas de deleção e marcação de recomendados.
- `MovieController.java`: Responsável pela busca avançada de filmes com filtros cruzados.

> **Ganhos:** Código altamente testável, com classes de cerca de 150 linhas em média, garantindo a coesão.

### 2.2 Pool de Conexões de Alta Performance
Integramos o **HikariCP** como provedor de gerência de conexões JDBC no novo `application.properties`, removendo gargalos. 
As chamadas manuais para o `DriverManager` nas classes base de DAO foram substituídas para solicitar conexões ativas desse pool, mantendo as queries vivas e reaproveitadas.

### 2.3 PreparedStatement Mandatório
Varremos o código para substituir todos os usos de `Statement` inseguro. Classes como `UserDAO` e `GenreDAO` agora forçam consultas parametrizadas, seguindo os rigorosos padrões de segurança do OWASP para prevenção de *SQL Injection*.

### 2.4 Lógica de Paginação do Feed com Memória
A consulta de sorteio cego do backend foi substituída pelo método `getUnratedMovieIds`. 
Ele funciona com a seguinte estratégia:
`SELECT id FROM movies WHERE id NOT IN (SELECT movie_id FROM feedbacks WHERE user_id = ?) ORDER BY popularity DESC LIMIT ? OFFSET ?`

> **Ganhos:** O feed agora possui "memória". Ele exclui tudo o que o usuário já avaliou (`NOT IN feedbacks`) e pagina o restante pela métrica de popularidade, entregando recomendações fluidas na tela de swipe do usuário.

## Conclusão
O backend em Java evoluiu de um protótipo acadêmico (baseado em Spark) para uma API pronta para o mercado. Tais práticas valorizam o portfólio demonstrando controle sobre Arquitetura de Software, Segurança e Escalabilidade.
