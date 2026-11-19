# Agendamento de docas

API REST para cadastrar docas e fornecedores, consultar a agenda de recebimento e registrar check-in, finalização e cancelamento.

## Executar localmente

Pré-requisitos: JDK 25 e Docker com Docker Compose. Na raiz do projeto, execute no PowerShell:

```powershell
$env:POSTGRES_DB = "agendamento"
$env:POSTGRES_USER = "postgres"
$env:POSTGRES_PASSWORD = "postgres"
docker compose up -d --wait
.\mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080`. O Flyway aplica as migrações na inicialização. Em Linux e macOS, use `./mvnw` e exporte as mesmas variáveis de ambiente.

Se o banco já estiver em uso, revise os dados antes de aplicar a migração V3. Agendamentos com início, fim, placa ou data de criação ausentes, ou com quantidade de paletes fora de 1 a 26, precisam ser corrigidos. Fornecedores cujo CNPJ fique duplicado após a normalização precisam ser conciliados. A migração falha nesses casos e preserva os registros para correção.

## Usar a API

O arquivo [AgendamentoDocas.http](AgendamentoDocas.http) contém exemplos. Atualize os IDs e escolha um início futuro com pelo menos 24 horas de antecedência no fuso `America/Sao_Paulo`.

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/docas` | Lista as docas ativas |
| POST | `/docas` | Cadastra uma doca |
| POST | `/fornecedores` | Cadastra um fornecedor |
| POST | `/agendamentos` | Cria um agendamento |
| GET | `/agendamentos?docaId=1&data=2026-11-23` | Lista a agenda diária da doca |
| GET | `/agendamentos/{id}` | Busca um agendamento |
| PATCH | `/agendamentos/{id}/check-in` | Registra o check-in |
| PATCH | `/agendamentos/{id}/finalizar` | Finaliza após o check-in |
| PATCH | `/agendamentos/{id}/cancelar` | Cancela com motivo de 10 a 255 caracteres |

O expediente vai de segunda a sábado, das 06h às 18h. A descarga dura 30 minutos mais 5 por palete e deve terminar até o fechamento. O início deve cair em um intervalo de 15 minutos sem segundos ou frações. Carga refrigerada exige doca refrigerada e toda doca precisa estar ativa. O check-in é permitido de 60 minutos antes até 30 minutos depois do início.

Os erros usam `application/problem+json`. A API retorna 400 para dados de entrada inválidos, 404 para recursos ausentes, 409 para conflitos e 422 para violações de regras de negócio. Erros de validação incluem os detalhes no campo `erros`.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes unitários e HTTP rodam sem banco e usam um relógio fixo nos cenários de horário. Com Docker ativo, o Testcontainers sobe um PostgreSQL isolado, roda as migrações e verifica a consulta de sobreposição, incluindo o caso das 10h. Sem Docker, o teste de repositório é ignorado.
