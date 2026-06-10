
## 1. Problema abordado

As enchentes urbanas são um problema recorrente em várias cidades brasileiras. Em períodos de chuva forte, alguns rios e córregos sobem rapidamente e podem causar alagamentos em avenidas, bairros e áreas próximas a fundos de vale.

Um dos pontos mais críticos é a dificuldade de acompanhar essas mudanças em tempo real. Muitas vezes, a população e os órgãos responsáveis só percebem a gravidade da situação quando o alagamento já aconteceu. Por isso, sistemas de monitoramento podem ajudar na identificação mais rápida de regiões de risco.

No contexto deste projeto, o problema escolhido foi o monitoramento de pontos sujeitos a enchentes, usando sensores simulados e uma API para registrar leituras de nível da água e precipitação.

---

## 2. Objetivo da solução

O **Flood Monitor** é uma API REST criada para simular o monitoramento de enchentes urbanas. A ideia é permitir que sensores instalados em pontos estratégicos enviem dados para o sistema, que então registra as leituras e gera alertas quando o nível da água indicar risco.

A aplicação permite:

* cadastrar sensores;
* consultar sensores cadastrados;
* registrar leituras de nível da água e precipitação;
* calcular automaticamente o nível de alerta;
* gerar alertas quando uma leitura indicar situação de risco;
* consultar o histórico de leituras e alertas.

O projeto se relaciona com o **ODS 9**, pois demonstra como uma infraestrutura digital pode ser usada para apoiar o monitoramento urbano e ajudar na organização de informações importantes para prevenção de desastres.

---

## 3. Diagrama da arquitetura

```text
┌────────────────────────────────────────────────────────────┐
│                    CLIENTES / SISTEMAS                     │
│          Swagger  |  Dashboard  |  Sensores simulados      │
└────────────────────────┬───────────────────────────────────┘
                         │ HTTP + X-API-KEY
                         ▼
┌────────────────────────────────────────────────────────────┐
│                  FLOOD MONITOR API                         │
│              Spring Boot 3 / Java 17                       │
│                                                            │
│  ┌────────────────┐   ┌────────────────────────────────┐   │
│  │  ApiKeyFilter  │   │  GlobalExceptionHandler         │   │
│  │  Segurança     │   │  Tratamento de erros            │   │
│  └───────┬────────┘   └────────────────────────────────┘   │
│          │                                                 │
│  ┌───────▼─────────────────────────────────────────────┐   │
│  │                 Controller Layer                    │   │
│  │  SensorController | LeituraController | AlertaCtrl  │   │
│  └───────┬─────────────────────────────────────────────┘   │
│          │                                                 │
│  ┌───────▼─────────────────────────────────────────────┐   │
│  │                  Service Layer                      │   │
│  │  SensorService | LeituraService | AlertaService     │   │
│  │  Regras de negócio e cálculo de alerta              │   │
│  └───────┬─────────────────────────────────────────────┘   │
│          │                                                 │
│  ┌───────▼─────────────────────────────────────────────┐   │
│  │                Repository Layer                     │   │
│  │  SensorRepository | LeituraRepository | AlertaRepo  │   │
│  │  Acesso ao banco com Spring Data JPA                 │   │
│  └───────┬─────────────────────────────────────────────┘   │
└──────────┼─────────────────────────────────────────────────┘
           │
           ▼
┌────────────────────────────────────────────────────────────┐
│                      Banco de Dados                        │
│                         H2                                 │
│             sensores | leituras | alertas                  │
└────────────────────────────────────────────────────────────┘
```

---

## 4. Explicação dos componentes

### 4.1 Model

A camada de model representa as entidades principais do sistema. Cada entidade corresponde a uma informação que precisa ser armazenada no banco.

| Entidade  | Principais atributos                                         | Função                                                   |
| --------- | ------------------------------------------------------------ | -------------------------------------------------------- |
| `Sensor`  | id, nome, localização, latitude, longitude, status, criadoEm | Representa um sensor instalado em um ponto da cidade.    |
| `Leitura` | id, sensor, nivelAgua, precipitacao, nivelAlerta, timestamp  | Representa uma medição enviada por um sensor.            |
| `Alerta`  | id, sensor, leitura, mensagem, nivel, timestamp, ativo       | Representa um alerta criado quando há risco de enchente. |

### 4.2 Níveis de alerta

O sistema classifica cada leitura de acordo com o nível da água informado.

| Nível da água                  | Status    |
| ------------------------------ | --------- |
| Menor que 50 cm                | `NORMAL`  |
| De 50 cm até menor que 100 cm  | `ATENCAO` |
| De 100 cm até menor que 150 cm | `ALERTA`  |
| A partir de 150 cm             | `CRITICO` |

Quando a leitura é classificada como `ALERTA` ou `CRITICO`, o sistema cria automaticamente um alerta relacionado ao sensor.

---

### 4.3 Repository

A camada repository faz a comunicação com o banco de dados. No projeto, ela utiliza o **Spring Data JPA**, o que evita a necessidade de escrever manualmente todas as consultas SQL.

Os repositories são responsáveis por operações como:

* salvar sensores, leituras e alertas;
* buscar registros por ID;
* listar dados cadastrados;
* consultar leituras e alertas relacionados a um sensor.

---

### 4.4 Service

A camada service concentra as principais regras de negócio do sistema.

No Flood Monitor, ela é responsável por:

* verificar se um sensor existe antes de cadastrar uma leitura;
* calcular o nível de alerta com base no nível da água;
* salvar a leitura no banco;
* criar um alerta automaticamente quando necessário;
* organizar as operações antes de devolver a resposta ao controller.

Essa separação evita que o controller fique com muita lógica e deixa o código mais organizado.

---

### 4.5 Controller

A camada controller recebe as requisições HTTP da API. Ela é responsável por disponibilizar os endpoints de sensores, leituras e alertas.

Os principais endpoints seguem a estrutura:

* `/api/v1/sensores`
* `/api/v1/leituras`
* `/api/v1/alertas`

Os controllers recebem os dados, aplicam as validações dos DTOs e chamam os services para executar as regras do sistema.

---

### 4.6 DTO

Os DTOs são usados para separar os dados recebidos ou enviados pela API das entidades do banco.

Isso ajuda a evitar que a estrutura interna das entidades seja exposta diretamente nas respostas da API. Também facilita futuras mudanças no formato das requisições e respostas sem precisar alterar toda a modelagem do banco.

---

### 4.7 Segurança — ApiKeyFilter

A segurança da API foi feita com um filtro que verifica o header `X-API-KEY`.

Todas as requisições para os endpoints da API precisam enviar a chave correta. Caso a chave esteja ausente ou inválida, o sistema retorna erro `401 Unauthorized`.

Para esta entrega, a API Key foi escolhida por ser uma forma simples de proteger os endpoints e facilitar os testes. Em um sistema real, seria necessário usar um controle mais completo, com usuários, permissões e tokens.

---

### 4.8 Tratamento de erros

O projeto usa um tratamento global de erros para padronizar as respostas da API.

Os principais casos tratados são:

* dados inválidos enviados na requisição;
* busca por recurso inexistente;
* erro interno inesperado.

Com isso, a API retorna mensagens mais organizadas e evita respostas diferentes para erros parecidos.

---

## 5. Fluxo básico da aplicação

```text
1. O cliente envia uma leitura para POST /api/v1/leituras
        │
2. O ApiKeyFilter valida o header X-API-KEY
        │
3. O LeituraController recebe os dados da requisição
        │
4. O LeituraService verifica se o sensor existe
        │
5. O sistema calcula o nível de alerta da leitura
        │
6. A leitura é salva no banco
        │
7. Se o nível for ALERTA ou CRITICO, um alerta é criado
        │
8. A API retorna a resposta para o cliente
```

Exemplo: se um sensor enviar uma leitura com nível da água de 175 cm, o sistema classifica essa leitura como `CRITICO` e gera um alerta ativo para aquele sensor.

---

## 6. Justificativa da arquitetura

A arquitetura em camadas foi escolhida porque atende bem ao tamanho e ao objetivo do projeto. Ela permite separar a entrada da API, as regras de negócio e o acesso ao banco de dados.

Essa divisão ajuda principalmente em três pontos:

* deixa o código mais organizado;
* facilita manutenção e futuras alterações;
* evita que toda a lógica fique concentrada nos controllers.

No projeto, os controllers cuidam das requisições HTTP, os services cuidam das regras de negócio e os repositories cuidam da comunicação com o banco. Essa separação torna o funcionamento da aplicação mais claro.

O uso do H2 foi escolhido para facilitar os testes locais, já que não exige instalação de um banco externo. Para uma versão mais completa, o banco poderia ser substituído por PostgreSQL ou Oracle, mantendo a mesma lógica principal da aplicação.

A API também não usa sessão, o que facilita uma possível execução em mais de uma instância futuramente. Mesmo assim, para esta entrega, o foco principal foi construir uma API funcional, organizada e documentada.

---

## 7. Tecnologias e justificativas

| Tecnologia        | Justificativa                                                     |
| ----------------- | ----------------------------------------------------------------- |
| Java 17           | Linguagem usada no projeto e compatível com o Spring Boot.        |
| Spring Boot 3     | Facilita a criação de APIs REST e a organização da aplicação.     |
| Spring Data JPA   | Simplifica o acesso ao banco de dados por meio de repositories.   |
| H2                | Banco em memória usado para testes e demonstração local.          |
| Lombok            | Reduz código repetitivo, como getters, setters e construtores.    |
| SpringDoc OpenAPI | Gera a documentação Swagger para testar os endpoints.             |
| API Key           | Proteção simples para os endpoints da API nesta versão acadêmica. |

---
