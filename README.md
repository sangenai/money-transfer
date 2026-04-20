# Money Transfer REST API

A simple REST API for transferring money between accounts, built with Spring Boot.

## Tech Stack
- Java 17
- Spring Boot 3.2
- In-memory storage (ConcurrentHashMap)
- JUnit 5 + MockMvc for testing

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /accounts | List all accounts |
| GET | /accounts/{id} | Get account by ID |
| POST | /accounts | Create new account |
| POST | /transfers | Transfer money between accounts |

## How to Run

**Option 1 — Run executable JAR (Java 17+ required, nothing else needed):**
```bash
java -jar money-transfer-1.0.jar
```

**Option 2 — Build and run from source (Maven required):**

Step 1 — Build the JAR:
```bash
mvn package
```
This will:
- Download dependencies
- Compile the code
- Run the tests
- Create the JAR file

Wait for:
```
BUILD SUCCESS
```

Step 2 — Run the JAR:
```bash
java -jar target/money-transfer-1.0.jar
```

## Sample Requests

**Create Account:**
```json
POST /accounts
{"owner": "Rahul", "balance": 1000}
```

**Transfer Money:**
```json
POST /transfers
{"fromAccountId": 1, "toAccountId": 2, "amount": 200}
```

## Design Decisions
- `ConcurrentHashMap` for thread-safe in-memory storage
- `BigDecimal` for precise monetary calculations
- Accounts locked in ID order during transfer to prevent deadlock
- Embedded Tomcat via Spring Boot — no external server required
