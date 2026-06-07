# MiniPay

MiniPay is a Java Spring Boot digital wallet API. It supports user wallets,
deposits, transfers, balances, and transaction history.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Docker Compose
- Maven
- Swagger/OpenAPI
- GitHub Actions
- JUnit, MockMvc, H2 test profile

## Features

- Create users with wallets
- Deposit money
- Transfer money between wallets
- View balances and transaction history
- Validate request DTOs
- Handle API errors with custom exceptions
- Protect API endpoints with HTTP Basic authentication
- Run API and PostgreSQL with Docker Compose
- Manage database schema with Flyway migrations
- Protect payment data with database constraints
- Prevent duplicate payment retries with idempotency keys
- Expose health status with Spring Boot Actuator

## Run

Start the API and PostgreSQL:

```bash
docker compose up -d
```

API:

```text
http://localhost:8081
```

Swagger:

```text
http://localhost:8081/swagger-ui/index.html
```

Health check:

```text
http://localhost:8081/actuator/health
```

Default API credentials:

```text
username: minipay
password: minipay
```

They can be changed with `MINIPAY_SECURITY_USERNAME` and `MINIPAY_SECURITY_PASSWORD`.

Stop containers:

```bash
docker compose down
```

## Database

PostgreSQL is exposed on:

```text
localhost:15432
```

Connect with `psql`:

```bash
docker exec -it minipay-postgres psql -U minipay -d minipay
```

Example queries:

```sql
SELECT * FROM users;
SELECT * FROM wallets;
SELECT * FROM transactions;
```

## Tests

Local tests use the H2 test profile for fast feedback:

```bash
mvn test
```

GitHub Actions runs the same tests against PostgreSQL.

## Example Requests

Create a user:

```bash
curl -X POST http://localhost:8081/api/users \
  -u minipay:minipay \
  -H "Content-Type: application/json" \
  -d '{"name":"Raven","email":"raven@example.com"}'
```

Deposit money:

```bash
curl -X POST http://localhost:8081/api/wallets/1/deposit \
  -u minipay:minipay \
  -H "Idempotency-Key: deposit-demo-1" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00}'
```

Transfer money:

```bash
curl -X POST http://localhost:8081/api/wallets/transfer \
  -u minipay:minipay \
  -H "Idempotency-Key: transfer-demo-1" \
  -H "Content-Type: application/json" \
  -d '{"fromUserId":1,"toUserId":2,"amount":25.50}'
```

Check balance:

```bash
curl -u minipay:minipay http://localhost:8081/api/wallets/1/balance
```
