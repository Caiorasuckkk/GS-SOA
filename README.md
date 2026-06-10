# 🌊 Flood Monitor API

> **FIAP — 3ESPY 2026 | Global Solution | ODS 9 — Indústria, Inovação e Infraestrutura**

API REST para monitoramento de enchentes urbanas, desenvolvida em **Java 17 + Spring Boot 3**, com arquitetura em camadas, autenticação por API Key, persistência com H2 em ambiente de desenvolvimento e documentação via Swagger.

---

## 📌 Índice

* [Sobre o Projeto](#sobre-o-projeto)
* [Tecnologias](#tecnologias)
* [Arquitetura](#arquitetura)
* [Lógica de Alerta](#lógica-de-alerta)
* [Como Executar](#como-executar)
* [Autenticação](#autenticação)
* [Endpoints da API](#endpoints-da-api)
* [Exemplos de Requisição](#exemplos-de-requisição)
* [Documentação Swagger](#documentação-swagger)
* [Banco de Dados H2](#banco-de-dados-h2)
* [Respostas Padronizadas](#respostas-padronizadas)

---

## 📖 Sobre o Projeto

O **Flood Monitor** é uma API para apoiar o monitoramento de enchentes em áreas urbanas. A proposta do projeto é simular uma infraestrutura digital capaz de registrar sensores, armazenar leituras ambientais e gerar alertas quando os dados indicarem risco de alagamento.

A aplicação permite:

* Cadastrar sensores instalados em pontos estratégicos da cidade.
* Registrar leituras de **nível da água** e **precipitação**.
* Classificar automaticamente o risco da leitura.
* Gerar alertas automáticos em situações de risco.
* Consultar sensores, leituras e alertas por meio de endpoints REST.

### Relevância — ODS 9

A solução se relaciona ao **Objetivo de Desenvolvimento Sustentável 9**, pois utiliza tecnologia e infraestrutura digital para apoiar cidades mais resilientes. O projeto demonstra como APIs, sensores e sistemas integrados podem ajudar no acompanhamento de eventos climáticos, principalmente em regiões sujeitas a enchentes.

---

## 🛠 Tecnologias

| Camada                 | Tecnologia                  |
| ---------------------- | --------------------------- |
| Linguagem              | Java 17                     |
| Framework              | Spring Boot 3.2.5           |
| Persistência           | Spring Data JPA + H2        |
| Segurança              | Spring Security + API Key   |
| Documentação           | SpringDoc OpenAPI / Swagger |
| Build                  | Maven                       |
| Redução de boilerplate | Lombok                      |

---

## 🏗 Arquitetura

O projeto utiliza uma arquitetura em camadas para separar responsabilidades e facilitar manutenção, testes e evolução futura.

```text
┌─────────────────────────────────────────────────────────┐
│                     CLIENTE HTTP                        │
└─────────────────────┬───────────────────────────────────┘
                      │ X-API-KEY
          ┌───────────▼────────────┐
          │     ApiKeyFilter       │  ← Segurança
          └───────────┬────────────┘
                      │
          ┌───────────▼────────────┐
          │   Controller Layer     │  ← Endpoints REST
          │                        │
          │   SensorController     │
          │   LeituraController    │
          │   AlertaController     │
          └───────────┬────────────┘
                      │
          ┌───────────▼────────────┐
          │     Service Layer      │  ← Regras de negócio
          │                        │
          │   SensorService        │
          │   LeituraService       │
          │   AlertaService        │
          └───────────┬────────────┘
                      │
          ┌───────────▼────────────┐
          │   Repository Layer     │  ← Spring Data JPA
          │                        │
          │   SensorRepository     │
          │   LeituraRepository    │
          │   AlertaRepository     │
          └───────────┬────────────┘
                      │
          ┌───────────▼────────────┐
          │      Database H2       │  ← Banco em memória
          │                        │
          │   sensores             │
          │   leituras             │
          │   alertas              │
          └────────────────────────┘
```

### Responsabilidade das camadas

| Camada         | Responsabilidade                                                                     |
| -------------- | ------------------------------------------------------------------------------------ |
| **Controller** | Recebe as requisições HTTP, valida os dados de entrada e retorna respostas JSON.     |
| **Service**    | Concentra as regras de negócio, como cálculo do nível de risco e geração de alertas. |
| **Repository** | Realiza o acesso ao banco de dados usando Spring Data JPA.                           |
| **Model**      | Representa as entidades persistidas no banco.                                        |
| **DTO**        | Define os objetos usados para entrada e saída de dados da API.                       |
| **Exception**  | Centraliza o tratamento de erros da aplicação.                                       |
| **Security**   | Valida a API Key enviada no header `X-API-KEY`.                                      |

---

## 🚨 Lógica de Alerta

Cada leitura registrada recebe uma classificação de risco com base no nível da água informado.

| Nível da água                  | Status    |
| ------------------------------ | --------- |
| Menor que 50 cm                | `NORMAL`  |
| De 50 cm até menor que 100 cm  | `ATENCAO` |
| De 100 cm até menor que 150 cm | `ALERTA`  |
| A partir de 150 cm             | `CRITICO` |

Quando uma nova leitura é registrada com status `ALERTA` ou `CRITICO`, o sistema gera automaticamente um alerta relacionado ao sensor daquela leitura.

No MVP, a geração automática acontece no cadastro de novas leituras, simulando a chegada de dados em tempo real enviados por sensores.

---

## ▶️ Como Executar

### Pré-requisitos

Antes de iniciar, é necessário ter instalado:

* Java 17 ou superior
* Maven 3.8 ou superior
* Git

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/Caiorasuckkk/GS-SOA.git

# 2. Acesse a pasta do projeto
cd GS-SOA

# 3. Compile o projeto
mvn clean install

# 4. Execute a aplicação
mvn spring-boot:run
```

Após iniciar a aplicação, a API estará disponível em:

```text
http://localhost:8080
```

---

## 🔐 Autenticação

Todas as rotas da API exigem o envio do header `X-API-KEY`.

```text
X-API-KEY: FIAP-GS-2026-FLOOD-KEY
```

Sem esse header, ou com uma chave inválida, a API retorna:

```text
401 Unauthorized
```

A chave utilizada neste projeto é uma chave padrão para ambiente acadêmico e de desenvolvimento. Em um ambiente real de produção, essa informação deveria ser armazenada em variável de ambiente ou serviço seguro de secrets.

---

## 📡 Endpoints da API

### Sensores — `/api/v1/sensores`

| Método   | Endpoint                   | Descrição                       |
| -------- | -------------------------- | ------------------------------- |
| `GET`    | `/api/v1/sensores`         | Lista todos os sensores.        |
| `GET`    | `/api/v1/sensores?status=` | Filtra sensores por status.     |
| `GET`    | `/api/v1/sensores/{id}`    | Busca um sensor pelo ID.        |
| `POST`   | `/api/v1/sensores`         | Cadastra um novo sensor.        |
| `PUT`    | `/api/v1/sensores/{id}`    | Atualiza os dados de um sensor. |
| `DELETE` | `/api/v1/sensores/{id}`    | Remove um sensor.               |

### Leituras — `/api/v1/leituras`

| Método   | Endpoint                     | Descrição                       |
| -------- | ---------------------------- | ------------------------------- |
| `GET`    | `/api/v1/leituras`           | Lista todas as leituras.        |
| `GET`    | `/api/v1/leituras?sensorId=` | Filtra leituras por sensor.     |
| `GET`    | `/api/v1/leituras/{id}`      | Busca uma leitura pelo ID.      |
| `POST`   | `/api/v1/leituras`           | Registra uma nova leitura.      |
| `PUT`    | `/api/v1/leituras/{id}`      | Atualiza uma leitura existente. |
| `DELETE` | `/api/v1/leituras/{id}`      | Remove uma leitura.             |

### Alertas — `/api/v1/alertas`

| Método   | Endpoint                            | Descrição                      |
| -------- | ----------------------------------- | ------------------------------ |
| `GET`    | `/api/v1/alertas`                   | Lista todos os alertas.        |
| `GET`    | `/api/v1/alertas?apenasAtivos=true` | Lista apenas alertas ativos.   |
| `GET`    | `/api/v1/alertas?sensorId=`         | Filtra alertas por sensor.     |
| `GET`    | `/api/v1/alertas/{id}`              | Busca um alerta pelo ID.       |
| `POST`   | `/api/v1/alertas`                   | Cria um alerta manual.         |
| `PUT`    | `/api/v1/alertas/{id}`              | Atualiza um alerta.            |
| `PATCH`  | `/api/v1/alertas/{id}/desativar`    | Desativa ou encerra um alerta. |
| `DELETE` | `/api/v1/alertas/{id}`              | Remove um alerta.              |

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

Exemplo de resposta:

```json
{
  "success": true,
  "message": "Sensor cadastrado com sucesso.",
  "data": {
    "id": 1,
    "nome": "Sensor Rio Tamanduateí - Mooca",
    "descricao": "Sensor de nível instalado na margem do rio",
    "localizacao": "Av. Paes de Barros, 1200 - Mooca, São Paulo",
    "latitude": -23.5505,
    "longitude": -46.6033,
    "status": "ATIVO",
    "criadoEm": "2026-05-28T10:00:00"
  },
  "timestamp": "2026-05-28T10:00:00"
}
```

---

### Registrar leitura crítica

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

Como o nível da água está acima de 150 cm, a leitura será classificada como `CRITICO` e um alerta será gerado automaticamente.

---

### Listar alertas ativos

```bash
curl -X GET "http://localhost:8080/api/v1/alertas?apenasAtivos=true" \
  -H "X-API-KEY: FIAP-GS-2026-FLOOD-KEY"
```

---

## 📚 Documentação Swagger

A documentação interativa da API pode ser acessada em:

```text
http://localhost:8080/swagger-ui.html
```

Também pode funcionar pelo endereço:

```text
http://localhost:8080/swagger-ui/index.html
```

No Swagger, clique em **Authorize** e informe a chave:

```text
FIAP-GS-2026-FLOOD-KEY
```

Depois disso, os endpoints poderão ser testados diretamente pela interface.

---

## 🗄 Banco de Dados H2

Para desenvolvimento e testes locais, o projeto utiliza o banco H2 em memória.

Console do H2:

```text
http://localhost:8080/h2-console
```

Credenciais:

| Campo    | Valor                        |
| -------- | ---------------------------- |
| JDBC URL | `jdbc:h2:mem:floodmonitordb` |
| Username | `sa`                         |
| Password | vazio                        |

O H2 Console está habilitado apenas para facilitar a demonstração acadêmica e os testes locais. Em produção, ele deve ser desabilitado, e o banco poderia ser substituído por PostgreSQL, Oracle ou outro banco relacional.

---

## 📋 Respostas Padronizadas

Todas as respostas da API seguem um formato padronizado:

```json
{
  "success": true,
  "message": "Descrição da operação",
  "data": {},
  "timestamp": "2026-05-28T10:00:00"
}
```

Exemplo de resposta de erro:

```json
{
  "success": false,
  "message": "Sensor não encontrado.",
  "data": null,
  "timestamp": "2026-05-28T10:00:00"
}
```

### Códigos HTTP utilizados

| Código | Situação                                  |
| ------ | ----------------------------------------- |
| `200`  | Requisição realizada com sucesso.         |
| `201`  | Recurso criado com sucesso.               |
| `400`  | Dados inválidos ou requisição malformada. |
| `401`  | API Key ausente ou inválida.              |
| `404`  | Recurso não encontrado.                   |
| `500`  | Erro interno do servidor.                 |
