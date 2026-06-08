# 🌊 Flood Monitor API

> **FIAP — 3ESPY 2026 | Global Solution | ODS 9 — Indústria, Inovação e Infraestrutura**

Sistema de Monitoramento de Enchentes baseado em APIs REST com arquitetura em camadas, desenvolvido em **Java 17 + Spring Boot 3**.

---

## 📌 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Como Executar](#como-executar)
- [Autenticação](#autenticação)
- [Endpoints da API](#endpoints-da-api)
- [Exemplos de Requisição](#exemplos-de-requisição)
- [Documentação Swagger](#documentação-swagger)
- [Banco de Dados (H2)](#banco-de-dados-h2)

---

## 📖 Sobre o Projeto

O **Flood Monitor** é uma plataforma inteligente de monitoramento de enchentes urbanas. Ela permite:

- Cadastrar sensores instalados em pontos estratégicos da cidade.
- Registrar leituras periódicas de **nível de água** e **precipitação**.
- Gerar **alertas automáticos** quando os dados ultrapassam limites críticos.
- Consultar o histórico de leituras e alertas por sensor.

### Relevância — ODS 9

A solução se alinha ao Objetivo de Desenvolvimento Sustentável 9 ao utilizar **infraestrutura digital conectada** para prevenir desastres causados por enchentes, protegendo vidas e propriedades por meio de tecnologia acessível e escalável.

---

## 🛠 Tecnologias

| Camada          | Tecnologia                   |
|-----------------|------------------------------|
| Linguagem       | Java 17                      |
| Framework       | Spring Boot 3.2.5            |
| Persistência    | Spring Data JPA + H2 (dev)   |
| Segurança       | Spring Security + API Key    |
| Documentação    | SpringDoc OpenAPI (Swagger)  |
| Build           | Maven                        |
| Boilerplate     | Lombok                       |

---

## 🏗 Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                     CLIENT (HTTP)                       │
└─────────────────────┬───────────────────────────────────┘
                      │ X-API-KEY header
          ┌───────────▼────────────┐
          │    ApiKeyFilter        │  ← Segurança
          └───────────┬────────────┘
          ┌───────────▼────────────┐
          │    Controller Layer    │  ← SensorController
          │                        │     LeituraController
          │   /api/v1/sensores     │     AlertaController
          │   /api/v1/leituras     │
          │   /api/v1/alertas      │
          └───────────┬────────────┘
          ┌───────────▼────────────┐
          │    Service Layer       │  ← Regras de negócio
          │  SensorService         │     (alerta automático,
          │  LeituraService        │      cálculo de nível)
          │  AlertaService         │
          └───────────┬────────────┘
          ┌───────────▼────────────┐
          │   Repository Layer     │  ← Spring Data JPA
          │  SensorRepository      │
          │  LeituraRepository     │
          │  AlertaRepository      │
          └───────────┬────────────┘
          ┌───────────▼────────────┐
          │    Database (H2)       │  ← Em memória (dev)
          │  sensores / leituras   │     Substituível por
          │  alertas               │     PostgreSQL / Oracle
          └────────────────────────┘
```

### Camadas

| Camada         | Responsabilidade                                      |
|----------------|-------------------------------------------------------|
| **Controller** | Recebe requisições HTTP, valida entrada, retorna JSON |
| **Service**    | Lógica de negócio, regras de alerta automático        |
| **Repository** | Acesso ao banco via JPA                               |
| **Model**      | Entidades JPA (Sensor, Leitura, Alerta)               |
| **DTO**        | Objetos de transferência de dados (Request/Response)  |
| **Exception**  | Tratamento global de erros + wrapper ApiResponse      |
| **Security**   | Filtro de API Key via header X-API-KEY                |

### Lógica de Níveis de Alerta

| Nível da Água | Status     |
|---------------|------------|
| < 50 cm       | `NORMAL`   |
| 50 – 100 cm   | `ATENCAO`  |
| 100 – 150 cm  | `ALERTA`   |
| > 150 cm      | `CRITICO`  |

> ⚠️ Quando uma leitura com nível `ALERTA` ou `CRITICO` é registrada, o sistema **gera um alerta automaticamente**.

---

## ▶️ Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/flood-monitor.git
cd flood-monitor

# 2. Compile o projeto
mvn clean install

# 3. Execute
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

---

## 🔐 Autenticação

Todas as rotas da API exigem o header **`X-API-KEY`**.

```
X-API-KEY: FIAP-GS-2026-FLOOD-KEY
```

Sem esse header, a API retorna **401 Unauthorized**.

---

## 📡 Endpoints da API

### Sensores — `/api/v1/sensores`

| Método   | Endpoint                   | Descrição                         |
|----------|----------------------------|-----------------------------------|
| `GET`    | `/api/v1/sensores`         | Lista todos os sensores           |
| `GET`    | `/api/v1/sensores?status=` | Filtra por status (ATIVO etc.)    |
| `GET`    | `/api/v1/sensores/{id}`    | Busca sensor por ID               |
| `POST`   | `/api/v1/sensores`         | Cadastra novo sensor              |
| `PUT`    | `/api/v1/sensores/{id}`    | Atualiza sensor                   |
| `DELETE` | `/api/v1/sensores/{id}`    | Remove sensor                     |

### Leituras — `/api/v1/leituras`

| Método   | Endpoint                      | Descrição                         |
|----------|-------------------------------|-----------------------------------|
| `GET`    | `/api/v1/leituras`            | Lista todas as leituras           |
| `GET`    | `/api/v1/leituras?sensorId=`  | Filtra leituras por sensor        |
| `GET`    | `/api/v1/leituras/{id}`       | Busca leitura por ID              |
| `POST`   | `/api/v1/leituras`            | Registra nova leitura ⚡           |
| `PUT`    | `/api/v1/leituras/{id}`       | Atualiza leitura                  |
| `DELETE` | `/api/v1/leituras/{id}`       | Remove leitura                    |

### Alertas — `/api/v1/alertas`

| Método    | Endpoint                          | Descrição                          |
|-----------|-----------------------------------|------------------------------------|
| `GET`     | `/api/v1/alertas`                 | Lista todos os alertas             |
| `GET`     | `/api/v1/alertas?apenasAtivos=true` | Filtra alertas ativos            |
| `GET`     | `/api/v1/alertas?sensorId=`       | Filtra alertas por sensor          |
| `GET`     | `/api/v1/alertas/{id}`            | Busca alerta por ID                |
| `POST`    | `/api/v1/alertas`                 | Cria alerta manual                 |
| `PUT`     | `/api/v1/alertas/{id}`            | Atualiza alerta                    |
| `PATCH`   | `/api/v1/alertas/{id}/desativar`  | Encerra/desativa alerta            |
| `DELETE`  | `/api/v1/alertas/{id}`            | Remove alerta                      |

---

## 🔄 Exemplos de Requisição

### Criar sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensores \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: FIAP-GS-2026-FLOOD-KEY" \
  -d '{
    "nome": "Sensor Rio Tamanduateí - Mooca",
    "descricao": "Sensor de nível instalado na margem do rio",
    "localizacao": "Av. Paes de Barros, 1200 - Mooca, São Paulo",
    "latitude": -23.5505,
    "longitude": -46.6033,
    "status": "ATIVO"
  }'
```

**Resposta:**
```json
{
  "success": true,
  "message": "Sensor cadastrado com sucesso.",
  "data": {
    "id": 1,
    "nome": "Sensor Rio Tamanduateí - Mooca",
    "localizacao": "Av. Paes de Barros, 1200 - Mooca, São Paulo",
    "latitude": -23.5505,
    "longitude": -46.6033,
    "status": "ATIVO",
    "criadoEm": "2026-05-28T10:00:00"
  },
  "timestamp": "2026-05-28T10:00:00"
}
```

### Registrar leitura crítica (gera alerta automaticamente)

```bash
curl -X POST http://localhost:8080/api/v1/leituras \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: FIAP-GS-2026-FLOOD-KEY" \
  -d '{
    "sensorId": 1,
    "nivelAgua": 175.5,
    "precipitacao": 42.0
  }'
```

---

## 📚 Documentação Swagger

Acesse em: `http://localhost:8080/swagger-ui.html`

> O Swagger já inclui o campo de API Key. Clique em **Authorize** e informe `FIAP-GS-2026-FLOOD-KEY`.

---

## 🗄 Banco de Dados (H2)

Para desenvolvimento, o projeto usa H2 em memória.

Console: `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:floodmonitordb`
- **Username:** `sa`
- **Password:** *(vazio)*

> Para produção, basta alterar o `application.properties` com as credenciais do PostgreSQL ou Oracle.

---

## 📋 Respostas Padronizadas

Todas as respostas seguem o padrão:

```json
{
  "success": true | false,
  "message": "Descrição da operação",
  "data": { ... },
  "timestamp": "2026-05-28T10:00:00"
}
```

### Códigos HTTP utilizados

| Código | Situação                        |
|--------|---------------------------------|
| `200`  | Sucesso                         |
| `201`  | Recurso criado                  |
| `400`  | Dados inválidos / Bad Request   |
| `401`  | API Key ausente ou inválida     |
| `404`  | Recurso não encontrado          |
| `500`  | Erro interno do servidor        |
