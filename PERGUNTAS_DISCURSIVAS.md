
## Pergunta 1

### Quais seriam os principais desafios caso o sistema precisasse atender milhares de usuários simultaneamente?

Se o Flood Monitor precisasse atender milhares de usuários e vários sensores enviando dados ao mesmo tempo, o primeiro desafio seria garantir que a API continuasse respondendo bem sem perder leituras importantes.

No projeto atual, o banco H2 foi usado porque facilita os testes locais e a demonstração da aplicação. Porém, em um cenário real, ele não seria suficiente. O ideal seria usar um banco como PostgreSQL ou Oracle, com uma configuração melhor para lidar com muitas conexões ao mesmo tempo. Também seria importante separar bem as consultas das gravações, porque o sistema teria muitas leituras sendo registradas e, ao mesmo tempo, usuários consultando alertas e sensores.

Outro ponto importante seria o recebimento das leituras dos sensores. Hoje, a API recebe as informações por requisições HTTP. Para um volume maior, isso poderia gerar gargalo, principalmente em momentos de chuva forte, quando vários sensores enviariam dados ao mesmo tempo. Uma melhoria seria usar uma fila ou mensageria para receber essas leituras e processá-las aos poucos, evitando perda de dados.

A API também precisaria rodar em mais de uma instância. Como o sistema não depende de sessão do usuário, seria possível colocar várias instâncias atrás de um balanceador de carga. Assim, as requisições seriam distribuídas entre elas. Mesmo assim, seria necessário cuidar para que a geração de alertas não criasse registros duplicados quando muitas leituras fossem processadas ao mesmo tempo.

Além disso, consultas muito repetidas, como a listagem de alertas ativos, poderiam sobrecarregar o banco. Nesse caso, uma camada simples de cache ajudaria a diminuir a quantidade de consultas diretas.

Por fim, a autenticação por API Key atende bem ao objetivo acadêmico do projeto, mas para muitos usuários ou organizações diferentes seria necessário um controle mais completo, com login, permissões e tokens. Isso permitiria separar, por exemplo, quem pode cadastrar sensores, quem pode enviar leituras e quem pode apenas consultar alertas.

---

## Pergunta 2

### Quais pontos da arquitetura poderiam ser melhorados futuramente?

A arquitetura atual atende bem ao objetivo do projeto, porque separa controller, service, repository, DTOs, entidades e segurança. Mesmo assim, alguns pontos poderiam ser melhorados se o Flood Monitor fosse evoluir para um sistema mais próximo de produção.

O primeiro ponto seria trocar o banco H2 por um banco persistente, como PostgreSQL ou Oracle. O H2 é prático para desenvolvimento, mas os dados ficam em memória e são perdidos quando a aplicação reinicia. Com um banco real, seria possível manter o histórico de sensores, leituras e alertas.

Outro ponto seria melhorar as listagens. Hoje, os endpoints retornam os dados de forma simples. Com muitos registros, isso ficaria pesado. O ideal seria implementar paginação e filtros por período, principalmente nas leituras, já que esse tipo de dado tende a crescer muito rápido.

A autenticação também poderia ser evoluída. A API Key foi uma solução simples para proteger os endpoints nesta entrega, mas em um sistema real seria melhor ter usuários, papéis e permissões. Por exemplo, um sensor poderia ter permissão apenas para enviar leituras, enquanto um administrador poderia cadastrar sensores e gerenciar alertas.

Também seria interessante melhorar o processo de geração de alertas. Hoje, quando uma leitura é registrada, o próprio sistema calcula o nível de risco e cria um alerta se necessário. Para uma versão maior, essa parte poderia ser separada em um processamento assíncrono, usando uma fila. Isso deixaria a API mais leve e ajudaria em momentos de maior volume de dados.

Outro ponto de melhoria seria adicionar mais testes automatizados. O projeto já possui uma estrutura organizada, então seria possível criar testes nos services, validando regras como o cálculo do nível de alerta, e testes nos controllers, verificando as respostas da API.

Por fim, a aplicação poderia ser preparada para deploy com Docker. Isso facilitaria a execução em outros ambientes e permitiria subir a API junto com um banco de dados real, deixando o projeto mais próximo de um cenário profissional.

---

## Pergunta 3

### Como o sistema poderia evoluir para uma arquitetura distribuída?

O Flood Monitor poderia evoluir para uma arquitetura distribuída de forma gradual. Não seria necessário transformar tudo em microsserviços de uma vez, porque isso aumentaria bastante a complexidade. O ideal seria começar separando as partes que têm responsabilidades mais claras.

Uma possível divisão seria ter um serviço para sensores, um serviço para leituras e outro para alertas. O serviço de sensores cuidaria do cadastro e atualização dos sensores instalados. O serviço de leituras ficaria responsável por receber os dados enviados pelos sensores. Já o serviço de alertas analisaria as leituras e criaria alertas quando o nível da água indicasse risco.

A comunicação entre esses serviços poderia usar eventos. Por exemplo, quando uma leitura fosse registrada, o serviço de leituras enviaria um evento informando os dados recebidos. O serviço de alertas consumiria esse evento e decidiria se precisa gerar um alerta. Essa abordagem ajuda porque um serviço não fica totalmente dependente do outro estar disponível no mesmo momento.

Também seria possível criar um serviço separado para notificações. Assim, quando um alerta crítico fosse gerado, esse serviço poderia enviar e-mail, SMS ou notificação para os responsáveis. Isso deixaria a regra de alerta separada da parte de comunicação com usuários ou órgãos públicos.

Para os clientes externos, como um dashboard ou aplicativo, poderia existir uma entrada única para a API. Essa camada ficaria responsável por direcionar as requisições para o serviço correto e também poderia centralizar autenticação e controle de acesso.

Em uma estrutura distribuída, cada serviço poderia ter seu próprio banco ou pelo menos sua própria responsabilidade bem definida sobre os dados. Isso evita que todos dependam diretamente das mesmas tabelas e facilita a manutenção.

Por fim, os serviços poderiam ser executados em containers, facilitando a implantação e a escalabilidade. Em períodos de chuva intensa, por exemplo, o serviço de leituras poderia receber mais instâncias para aguentar o aumento no volume de dados enviados pelos sensores.

Essa evolução deixaria o Flood Monitor mais preparado para crescer, mas também exigiria mais cuidado com monitoramento, logs, testes e tratamento de falhas. Por isso, para o momento atual, a arquitetura em camadas é suficiente para o MVP, enquanto a arquitetura distribuída seria uma evolução para uma versão maior do sistema.
