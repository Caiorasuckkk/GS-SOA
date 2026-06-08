# Perguntas Discursivas — Flood Monitor

**Disciplina:** Arquitetura Orientada a Serviço (SOA)  
**Curso:** 3ESPY — FIAP 2026  
**Professora:** Damiana Costa

---

## Pergunta 1
### Quais seriam os principais desafios caso o sistema precisasse atender milhares de usuários simultaneamente?

O principal desafio ao escalar o Flood Monitor para milhares de usuários simultâneos está na **gestão de carga e consistência de dados**.

**Banco de dados:** O H2 em memória é adequado apenas para desenvolvimento. Em produção com alta concorrência, um banco relacional robusto como PostgreSQL precisaria ser configurado com **connection pooling** (ex.: HikariCP, já incluso no Spring Boot) e **réplicas de leitura**, separando as consultas das escritas. O gargalo de banco é geralmente o primeiro a aparecer em cenários de escala.

**Processamento de leituras:** Sensores IoT podem enviar centenas de leituras por segundo. Uma arquitetura baseada em chamadas HTTP síncronas se tornaria ineficiente. A solução seria introduzir um **message broker** (ex.: Apache Kafka ou RabbitMQ) para receber as leituras de forma assíncrona, desacoplando a ingestão do processamento e dos alertas. Isso garante que nenhuma leitura seja perdida mesmo em picos de carga.

**Instâncias da API:** A API é stateless (sem sessão), o que é um grande facilitador: basta subir múltiplas instâncias atrás de um **load balancer** (ex.: Nginx ou AWS ALB). Porém, seria necessário gerenciar a geração de alertas de forma idempotente para evitar duplicatas quando múltiplas instâncias processam a mesma leitura.

**Caching:** Endpoints de leitura frequentes (como listar alertas ativos) poderiam ser protegidos com **Redis**, reduzindo a carga no banco para consultas repetitivas feitas por dashboards e aplicativos.

**Autenticação:** A API Key simples não escala bem em ambientes multi-tenant com muitos clientes distintos. Seria necessário migrar para **JWT** com um servidor de autorização (ex.: Keycloak ou Auth0) para emitir tokens com tempo de expiração e controle granular de permissões.

---

## Pergunta 2
### Quais pontos da arquitetura poderiam ser melhorados futuramente?

Apesar da arquitetura atual ser funcional e bem organizada para um MVP, diversos aspectos podem evoluir:

**1. Banco de dados em produção**  
Substituir o H2 por **PostgreSQL** (open source) ou **Oracle** (já amplamente usado na FIAP e em grandes corporações). O Spring Data JPA facilita essa transição sem alterar código: basta modificar o `application.properties` e o driver no `pom.xml`. Para dados de séries temporais (leituras de sensores), bancos especializados como **TimescaleDB** (extensão do PostgreSQL) ou **InfluxDB** seriam mais eficientes.

**2. Autenticação e autorização granular**  
A API Key atual trata todos os clientes igualmente. Uma evolução natural seria implementar **OAuth 2.0 com JWT**, diferenciando papéis (ex.: `ROLE_SENSOR` para dispositivos IoT, `ROLE_ADMIN` para gestão, `ROLE_READER` para dashboards read-only), usando Spring Security com scopes.

**3. Paginação nas listagens**  
As listagens atuais retornam todos os registros. Com o crescimento dos dados, seria essencial implementar paginação (`Pageable` do Spring Data) e filtros por período de data nas leituras e alertas.

**4. Ingestão assíncrona**  
Integrar Apache Kafka para receber leituras de múltiplos sensores em paralelo, com consumers processando as mensagens em background. Isso desacopla a camada de ingestão da de processamento.

**5. Containerização e infraestrutura como código**  
Dockerizar a aplicação e criar um `docker-compose.yml` com a API + banco PostgreSQL + Redis facilita deploys reproduzíveis. Em seguida, evoluir para Kubernetes para orquestração em produção.

**6. Monitoramento e observabilidade**  
Adicionar Spring Boot Actuator com métricas exportadas para **Prometheus** e visualizadas no **Grafana**, incluindo alertas de SLO (tempo de resposta, taxa de erros). Logs estruturados em JSON enviados para **ELK Stack** (Elasticsearch + Logstash + Kibana).

**7. Testes automatizados**  
Ampliar a cobertura com testes unitários nos services (JUnit 5 + Mockito) e testes de integração nos controllers (MockMvc). Criar um pipeline CI/CD no GitHub Actions para rodar os testes automaticamente a cada pull request.

---

## Pergunta 3
### Como o sistema poderia evoluir para uma arquitetura distribuída?

A transição para uma **arquitetura de microsserviços distribuída** seria feita de forma gradual, seguindo princípios de Domain-Driven Design (DDD):

**Decomposição em microsserviços**  
A primeira etapa seria identificar os bounded contexts do domínio e separar a aplicação monolítica em serviços independentes:

- **sensor-service** — Cadastro e gerenciamento de sensores.
- **reading-service** — Ingestão e armazenamento de leituras.
- **alert-service** — Geração, consulta e gerenciamento de alertas.
- **notification-service** — Envio de notificações (SMS, push, e-mail) quando alertas críticos são gerados.

Cada serviço teria seu próprio banco de dados (Database per Service pattern), eliminando o acoplamento via banco compartilhado.

**Comunicação assíncrona com event-driven**  
Em vez de chamadas HTTP síncronas entre serviços (que criam acoplamento temporal), o `reading-service` publicaria eventos no **Apache Kafka** sempre que uma leitura fosse registrada. O `alert-service` consumiria esses eventos e decidiria se deve gerar um alerta. O `notification-service` consumiria eventos de alerta e enviaria as notificações. Isso garante resiliência: se o `alert-service` estiver offline, os eventos ficam na fila até ele se recuperar.

**API Gateway**  
Um **API Gateway** (ex.: Spring Cloud Gateway ou Kong) seria a entrada única para todos os clientes externos, responsável por roteamento, autenticação centralizada, rate limiting e SSL termination. Os microsserviços internos comunicariam entre si sem necessidade de autenticação pesada.

**Service Discovery**  
Com múltiplas instâncias de cada serviço, seria necessário um **Service Registry** (ex.: Eureka ou Consul) para que os serviços se encontrem dinamicamente, sem hardcodar IPs ou portas.

**Resiliência e Circuit Breaker**  
Implementar o padrão **Circuit Breaker** com Resilience4j para evitar falhas em cascata: se o `alert-service` demorar a responder, o `reading-service` não fica bloqueado esperando, retornando um fallback adequado.

**Orquestração com Kubernetes**  
Cada microsserviço seria containerizado com Docker e orquestrado com Kubernetes, permitindo auto-scaling baseado em métricas (ex.: escalar o `reading-service` automaticamente durante eventos de chuva intensa que aumentam o volume de leituras).

Essa evolução transformaria o Flood Monitor em uma plataforma de **infraestrutura digital distribuída**, altamente disponível, resiliente e capaz de atender cidades inteiras com milhares de sensores simultâneos.
