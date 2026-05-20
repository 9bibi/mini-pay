# MiniPay

MiniPay is a simple Java Spring Boot digital wallet API. It lets you create users,
deposit money, transfer money between wallets, check balances, and view transaction
history.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Maven

## Run

```bash
mvn spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

H2 console:

```text
http://localhost:8080/h2-console
```

Use JDBC URL:

```text
jdbc:h2:mem:minipay
```

## Example Requests

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Amina","email":"amina@example.com"}'
```

Deposit money:

```bash
curl -X POST http://localhost:8080/api/wallets/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00}'
```

Transfer money:

```bash
curl -X POST http://localhost:8080/api/wallets/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromUserId":1,"toUserId":2,"amount":25.50}'
```

Check balance:

```bash
curl http://localhost:8080/api/wallets/1/balance
```

View transaction history:

```bash
curl http://localhost:8080/api/transactions/users/1
```
