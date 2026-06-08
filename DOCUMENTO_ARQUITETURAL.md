# Documento Arquitetural — Flood Monitor

**Disciplina:** Arquitetura Orientada a Serviço (SOA)  
**Curso:** 3ESPY — FIAP 2026  
**Professora:** Damiana Costa  
**Tema:** ODS 9 — Indústria, Inovação e Infraestrutura  

---

## 1. Problema Abordado

Enchentes urbanas representam um dos maiores desafios das cidades brasileiras. Eventos de chuva intensa causam alagamentos repentinos que resultam em perdas humanas, materiais e econômicas. A falta de monitoramento em tempo real impede respostas rápidas por parte da defesa civil e da população.

Cidades como São Paulo enfrentam esse problema ciclicamente, especialmente em regiões de fundos de vale onde rios e córregos transbordam com rapidez, sem que haja sistemas adequados de alerta precoce para comunidades vulneráveis.

---

## 2. Objetivo da Solução

O **Flood Monitor** é uma plataforma de monitoramento de enchentes baseada em APIs REST. Ela digitaliza e centraliza as informações coletadas por sensores físicos instalados em pontos estratégicos, oferecendo:

- Cadastro e gerenciamento de sensores de campo.
- Registro contínuo de leituras (nível de água e precipitação).
- Geração automática de alertas quando os limites críticos são ultrapassados.
- Interface de consulta para sistemas externos (aplicativos, painéis da defesa civil, etc.).

A solução atende ao ODS 9 ao criar uma **infraestrutura digital inovadora** que conecta sensores físicos a uma plataforma inteligente de gestão de riscos.

---

## 3. Diagrama da Arquitetura

```
┌──────────────────────────────────────────────────────────────────┐
│                        CLIENTES / SISTEMAS                       │
│         App Defesa Civil  |  Dashboard  |  IoT Sensor Gateway    │
└────────────────────────┬─────────────────────────────────────────┘
                         │ HTTPS + X-API-KEY
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                    FLOOD MONITOR API                             │
│                   (Spring Boot 3 / Java 17)                      │
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────────┐  │
│  │  ApiKeyFilter  │  │  Swagger UI    │  │  GlobalException   │  │
│  │  (Segurança)   │  │  /swagger-ui   │  │  Handler           │  │
│  └───────┬────────┘  └────────────────┘  └────────────────────┘  │
│          │                                                        │
│  ┌───────▼──────────────────────────────────────────────────┐    │
│  │                  CONTROLLER LAYER                        │    │
│  │   SensorController   LeituraController   AlertaController│    │
│  │   /api/v1/sensores   /api/v1/leituras    /api/v1/alertas │    │
│  └───────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│  ┌───────▼──────────────────────────────────────────────────┐    │
│  │                   SERVICE LAYER                          │    │
│  │   SensorService    LeituraService      AlertaService     │    │
│  │   (CRUD + regras  (CRUD + cálculo de  (CRUD + alerta    │    │
│  │    de validação)   nível automático)   automático)       │    │
│  └───────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│  ┌───────▼──────────────────────────────────────────────────┐    │
│  │                 REPOSITORY LAYER                         │    │
│  │  SensorRepository  LeituraRepository  AlertaRepository   │    │
│  │              (Spring Data JPA)                           │    │
│  └───────┬──────────────────────────────────────────────────┘    │
│          │                                                        │
│  ┌───────▼──────────────────────────────────────────────────┐    │
│  │                  MODEL LAYER                             │    │
│  │         Sensor       Leitura        Alerta               │    │
│  │         (Entity)     (Entity)       (Entity)             │    │
│  └───────┬──────────────────────────────────────────────────┘    │
└──────────┼───────────────────────────────────────────────────────┘
           │
┌──────────▼───────────────────────────────────────────────────────┐
│                     BANCO DE DADOS                               │
│              H2 (dev) → PostgreSQL / Oracle (prod)               │
│         sensores | leituras | alertas                            │
└──────────────────────────────────────────────────────────────────┘
```

---

## 4. Explicação dos Componentes

### 4.1 Model (Entidades)

| Entidade   | Atributos principais                                           | Função                                      |
|------------|----------------------------------------------------------------|---------------------------------------------|
| `Sensor`   | id, nome, localizacao, latitude, longitude, status, criadoEm  | Representa o dispositivo físico de campo    |
| `Leitura`  | id, sensor, nivelAgua, precipitacao, nivelAlerta, timestamp    | Medição registrada pelo sensor              |
| `Alerta`   | id, sensor, leitura, mensagem, nivel, timestamp, ativo         | Notificação gerada quando há risco de cheia |

**Enum NivelAlerta:**
- `NORMAL` — nível de água < 50 cm
- `ATENCAO` — 50 a 100 cm
- `ALERTA` — 100 a 150 cm
- `CRITICO` — acima de 150 cm

### 4.2 Repository

Interfaces Spring Data JPA. Herdam operações CRUD básicas e possuem métodos de consulta customizados como `findBySensorIdOrderByTimestampDesc` e `findByAtivoTrue`.

### 4.3 Service

Camada de regras de negócio. Responsável por:
- Validar existência de entidades relacionadas antes de operações.
- Calcular automaticamente o `NivelAlerta` da leitura com base no nível de água.
- Gerar alertas automáticos quando `nivelAlerta` for `ALERTA` ou `CRITICO`.

### 4.4 Controller

Recebe requisições HTTP, aplica validação Bean Validation (`@Valid`) e retorna respostas padronizadas em JSON via `ApiResponse<T>`. Segue semântica REST completa com GET, POST, PUT, PATCH e DELETE.

### 4.5 DTO (Data Transfer Object)

Classes internas `Request` e `Response` em cada DTO. Isolam o modelo de domínio da camada de apresentação, facilitando evoluções sem quebrar contratos.

### 4.6 Segurança — ApiKeyFilter

Filtro Servlet que intercepta todas as requisições e valida o header `X-API-KEY`. Endpoints públicos (Swagger, H2 Console) são liberados automaticamente. Retorna HTTP 401 para chaves ausentes ou inválidas.

### 4.7 Tratamento de Erros — GlobalExceptionHandler

`@RestControllerAdvice` que captura exceções e retorna mensagens padronizadas:
- `400` — dados inválidos ou parâmetros incorretos
- `404` — recurso não encontrado
- `500` — erros não esperados do servidor

---

## 5. Fluxo Básico da Aplicação

```
1. Gateway IoT envia leitura via POST /api/v1/leituras (com X-API-KEY)
         │
2. ApiKeyFilter valida o header
         │
3. LeituraController recebe e valida o DTO (Bean Validation)
         │
4. LeituraService calcula NivelAlerta com base no nivelAgua
         │
5. Leitura é persistida no banco
         │
6. Se nivel == ALERTA ou CRITICO:
         └─→ AlertaRepository.save(novoAlerta) [automático]
         │
7. Response 201 Created retornado ao cliente com dados da leitura

8. Dashboard consulta GET /api/v1/alertas?apenasAtivos=true
         └─→ Exibe alertas críticos para a defesa civil
```

---

## 6. Justificativa da Arquitetura

A arquitetura em camadas foi escolhida por ser o padrão mais maduro para APIs REST corporativas, com separação clara de responsabilidades:

- **Baixo acoplamento:** cada camada depende apenas da inferior.
- **Alta coesão:** cada classe tem uma responsabilidade única.
- **Testabilidade:** services podem ser testados unitariamente com mocks dos repositories.
- **Escalabilidade horizontal:** a API é stateless (sem sessão), pronta para escalar com múltiplas instâncias atrás de um load balancer.
- **Evolutibilidade:** o banco H2 pode ser substituído por PostgreSQL ou Oracle apenas alterando o `application.properties`, sem mudança no código.

---

## 7. Tecnologias e Justificativas

| Tecnologia        | Justificativa                                                                  |
|-------------------|--------------------------------------------------------------------------------|
| Java 17 + Spring  | Ecossistema maduro, amplamente utilizado em sistemas corporativos brasileiros  |
| Spring Data JPA   | Abstração de repositório que elimina SQL repetitivo                            |
| H2 (dev)          | Banco em memória sem configuração, ideal para demos e testes                   |
| Lombok            | Reduz boilerplate sem comprometer legibilidade                                 |
| SpringDoc OpenAPI | Documentação interativa automática, facilita integração por terceiros          |
| API Key (header)  | Autenticação simples e eficiente para comunicação máquina-a-máquina (IoT)      |
