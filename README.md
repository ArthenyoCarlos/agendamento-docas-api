# Agendamento de Docas

API REST para organizar o recebimento de mercadorias. Permite cadastrar docas e fornecedores, reservar horários de descarga, consultar a agenda por doca e acompanhar o recebimento do check-in à finalização.

**Tecnologias** · Java 25 · Spring Boot · Spring Data JPA · PostgreSQL 18 · Flyway · Maven

## Sumário

- [Execução local](#execução-local)
- [Primeiro agendamento](#primeiro-agendamento)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Respostas de erro](#respostas-de-erro)
- [Testes](#testes)
- [Banco de dados e migrações](#banco-de-dados-e-migrações)

## Execução local

Você precisa do **JDK 25** e do **Docker com Docker Compose**. O projeto inclui o Maven Wrapper.

Na raiz do repositório, configure o banco e inicie a aplicação.

**Windows (PowerShell)**

```powershell
$env:POSTGRES_DB = "agendamento"
$env:POSTGRES_USER = "postgres"
$env:POSTGRES_PASSWORD = "postgres"

docker compose up -d --wait
.\mvnw.cmd spring-boot:run
```

**Linux e macOS**

```bash
export POSTGRES_DB=agendamento
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres

docker compose up -d --wait
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080` e o PostgreSQL usa a porta local `5432`. O Flyway aplica as migrações ao iniciar a aplicação. Se você já usa um banco com dados, veja [Banco de dados e migrações](#banco-de-dados-e-migrações).

## Primeiro agendamento

Use o arquivo [AgendamentoDocas.http](AgendamentoDocas.http) no cliente HTTP da IDE ou envie as requisições com seu cliente HTTP de preferência.

1. Cadastre uma doca com `POST /docas` e o corpo `{"tipo":"SECA"}`. Guarde o `id` retornado.
2. Cadastre um fornecedor com `POST /fornecedores` e o corpo `{"cnpj":"24.292.765/0001-00","razaoSocial":"Fornecedor Exemplo"}`. Guarde o `id` retornado.
3. Envie o corpo abaixo em `POST /agendamentos`, com `Content-Type: application/json`. Substitua os IDs pelos valores retornados e ajuste `inicio` para um horário válido com pelo menos 24 horas de antecedência.

```json
{
  "fornecedorId": 1,
  "docaId": 1,
  "placaVeiculo": "QWE1R23",
  "inicio": "2026-11-23T10:00:00",
  "quantidadePaletes": 6,
  "tipoCarga": "SECA"
}
```

O horário é interpretado no fuso `America/Sao_Paulo`. Com 6 paletes, a descarga dura 60 minutos. A criação retorna **201 Created**, com o agendamento no corpo e sua URL no cabeçalho `Location`.

## Endpoints

| Método | Rota | Operação | Sucesso |
| --- | --- | --- | --- |
| GET | `/docas` | Lista as docas ativas | 200 |
| POST | `/docas` | Cadastra uma doca | 201 |
| POST | `/fornecedores` | Cadastra um fornecedor | 201 |
| POST | `/agendamentos` | Cria um agendamento | 201 |
| GET | `/agendamentos?docaId={id}&data={AAAA-MM-DD}` | Consulta a agenda diária da doca | 200 |
| GET | `/agendamentos/{id}` | Busca um agendamento | 200 |
| PATCH | `/agendamentos/{id}/check-in` | Registra a chegada | 204 |
| PATCH | `/agendamentos/{id}/finalizar` | Finaliza o recebimento | 204 |
| PATCH | `/agendamentos/{id}/cancelar` | Cancela o agendamento | 204 |

## Regras de negócio

### Reserva

| Regra | Comportamento |
| --- | --- |
| Duração | 30 minutos mais 5 minutos por palete. São permitidos de 1 a 26 paletes. |
| Antecedência | Pelo menos 24 horas antes do início. |
| Expediente | Segunda a sábado, das 06h às 18h. A descarga deve terminar até as 18h. |
| Horário de início | Minutos `00`, `15`, `30` ou `45`, com segundos e frações zerados. |
| Disponibilidade | Uma doca ativa pode receber agendamentos consecutivos. Se um termina às 10h, o próximo pode começar às 10h. Agendamentos cancelados liberam o horário. |
| Tipo de carga | Carga refrigerada exige doca refrigerada. Carga seca pode usar doca seca ou refrigerada. |

### Transições de status

| Ação | Status atual | Novo status | Condição |
| --- | --- | --- | --- |
| Check-in | `AGENDADO` | `CHEGOU` | De 60 minutos antes até 30 minutos depois do início, incluindo os limites. |
| Finalização | `CHEGOU` | `FINALIZADO` | O check-in deve estar registrado. |
| Cancelamento | `AGENDADO` | `CANCELADO` | Envie `{"motivo":"Entrega desmarcada pelo fornecedor"}` com 10 a 255 caracteres após remover os espaços das extremidades. |

## Respostas de erro

A API usa o formato `application/problem+json`. As respostas incluem o status HTTP, o título, os detalhes e o caminho da requisição em `instance`. Erros de validação do corpo incluem o campo `erros` com o nome do campo e a mensagem.

| Status | Quando ocorre |
| --- | --- |
| **400 Bad Request** | Entrada inválida, como a ausência de `inicio` ou quantidade de paletes fora do limite. |
| **404 Not Found** | Doca, fornecedor ou agendamento inexistente. |
| **409 Conflict** | Sobreposição de horários, CNPJ duplicado ou violação de integridade do banco. |
| **422 Unprocessable Content** | Violação de regra de negócio, como check-in fora da janela ou carga refrigerada em doca seca. |

## Testes

Execute `.\mvnw.cmd test` no Windows ou `./mvnw test` no Linux e macOS.

- **Unitários** usam JUnit 5 e Mockito. Os cenários de horário usam um relógio fixo.
- **HTTP** usam MockMvc para verificar a validação e o formato das respostas de erro.
- **Integração** usa Testcontainers para iniciar um PostgreSQL isolado, aplicar as migrações e testar a consulta de sobreposição. A suíte pula o teste de repositório quando o Docker está indisponível.

## Banco de dados e migrações

As migrações ficam em [`src/main/resources/db/migration`](src/main/resources/db/migration). A V3 adiciona o índice por doca e início, reforça as restrições de agendamento e normaliza CNPJs e placas.

Para atualizar um banco existente, corrija antes os agendamentos sem início, fim, placa ou data de criação, bem como os que tenham quantidade de paletes fora de 1 a 26. Concilie também os fornecedores cujo CNPJ fique duplicado após a normalização. Se esses problemas existirem, a migração falha e preserva os registros para correção.

A consulta de conflitos verifica os horários já gravados. Requisições concorrentes para a mesma doca ainda podem passar pela verificação antes da gravação.
