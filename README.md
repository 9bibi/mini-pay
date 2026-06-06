# MiniPay

MiniPay is a simple Java Spring Boot digital wallet API. It lets you create users,
deposit money, transfer money between wallets, check balances, and view transaction
history.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway database migrations
- Docker Compose
- Maven

## Run

Start the API and PostgreSQL together:

```bash
docker compose up -d
```

The API starts at:

```text
http://localhost:8081
```

Swagger API docs:

```text
http://localhost:8081/swagger-ui/index.html
```

PostgreSQL is exposed locally on:

```text
localhost:15432
```

Connect to PostgreSQL in Docker:

```bash
docker exec -it minipay-postgres psql -U minipay -d minipay
```

Stop the app and database:

```bash
docker compose down
```

Run the API locally without Dockerizing the Java app:

```bash
docker compose up -d postgres
mvn spring-boot:run
```

Useful SQL inside `psql`:

```sql
SELECT * FROM users;
SELECT * FROM wallets;
SELECT * FROM transactions;
```

## Example Requests

Create a user:

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"New","email":"new@example.com"}'
```

Deposit money:

```bash
curl -X POST http://localhost:8081/api/wallets/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00}'
```

Transfer money:

```bash
curl -X POST http://localhost:8081/api/wallets/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromUserId":1,"toUserId":2,"amount":25.50}'
```

Check balance:

```bash
curl http://localhost:8081/api/wallets/1/balance
```

View transaction history:

```bash
curl http://localhost:8081/api/transactions/users/1
```
