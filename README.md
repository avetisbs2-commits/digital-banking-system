# Digital Banking System

A Java backend project built with **Spring Boot**, **PostgreSQL**, and **Docker**. The application provides account management, deposits, withdrawals, transfers, transaction history, analytics, currency conversion, and an AI-powered banking assistant through the OpenAI API.

This repository demonstrates practical backend engineering concepts: layered architecture, REST endpoints, relational persistence with Spring Data JPA, transactional business operations, external API integration, containerization, and CI/CD with GitHub Actions.

> **Project context:** Personal learning and portfolio project. It is not a production banking system and must not be used for real financial transactions.

## Features

- Create bank accounts with a generated account number.
- Deposit and withdraw funds.
- Transfer money between accounts.
- Convert between AMD, USD, and EUR using configured exchange rates.
- View an account's transaction statement.
- Display basic banking analytics, including total balances by currency and transfer counts.
- Ask banking-related questions through the AI Banking Assistant.
- Persist accounts and transactions in PostgreSQL.
- Use a redesigned, responsive banking dashboard with dedicated panels for account and transaction operations.

## Engineering Highlights

The project brings together several backend engineering concepts in one application:

- **Layered design:** separates HTTP handling, business logic, persistence, and presentation.
- **Transactional operations:** uses `@Transactional` for banking operations that update balances and transaction records.
- **Data access:** uses Spring Data JPA repositories and Hibernate entity mapping with PostgreSQL.
- **Query-oriented indexing:** defines composite indexes for sender/receiver account numbers and transaction timestamps to support account-history lookups ordered by time.
- **External service integration:** calls the OpenAI Responses API from the backend using Spring's `RestClient`.
- **Containerized environment:** provides Docker and Docker Compose configuration for the application and database.
- **Automated workflow:** uses GitHub Actions to verify the Maven build, build a Docker image, and trigger deployment to Render.

These are implementation details of the current project, not claims of production-grade banking security or scalability.

## Architecture

The application uses a **layered monolithic architecture**. The web interface and REST endpoint are served by one Spring Boot application.

```text
                 Browser
                    |
                    v
        Thymeleaf UI (index.html)
                    |
                    v
             WebController
                    |
                    v
               BankService
                    |
          +---------+---------+
          |                   |
          v                   v
   AccountRepository   TransactionRepository
          |                   |
          +---------+---------+
                    |
                    v
                PostgreSQL


       AI Assistant (separate request flow)

 Browser JavaScript
        |
        | POST /api/ai/ask
        v
    AiController
        |
        v
     AiService
        |
        | HTTPS / OpenAI Responses API
        v
     OpenAI API
```

### Main layers

| Layer | Responsibility |
|---|---|
| Controller | Handles incoming web form submissions and REST requests. |
| Service | Contains banking rules, validation, transfers, currency conversion, analytics, and AI API integration. |
| Repository | Uses Spring Data JPA to query and persist entities. |
| Model | Defines the account, transaction, currency, and transaction-type domain objects. |
| View | Renders the banking interface with Thymeleaf. |

Banking operations are coordinated by `BankService`. Methods that modify account balances and transaction records use Spring's `@Transactional` annotation so the related database changes participate in one transaction.

## Technology Stack

### Backend

- **Java 17**
- **Spring Boot 3.2.5**
- Spring Web / Spring MVC
- Spring Data JPA
- Hibernate ORM (provided by the Spring Data JPA starter)
- Thymeleaf
- Spring Framework `RestClient` for outbound HTTP requests
- Jackson for parsing the AI API response
- dotenv-java for loading local environment variables

### Database

- **PostgreSQL**
- JPA entities are mapped to the `accounts` and `transactions` tables.
- Hibernate schema management is configured with `spring.jpa.hibernate.ddl-auto=update`.

### Frontend

- HTML, CSS, and JavaScript
- Thymeleaf server-side templates
- Responsive dashboard layout with mobile breakpoints

### Build, Testing, and DevOps

- **Apache Maven** for dependency management and builds
- Spring Boot Maven Plugin
- Spring Boot Starter Test (JUnit 5, Mockito, and Spring test support)
- **Docker** for containerizing the application
- **Docker Compose** for running the application and PostgreSQL together
- **GitHub Actions** for CI/CD
- **Render** deployment triggered through a deploy hook
- **Adminer** as an optional database administration tool in Docker Compose

### AI Integration

- OpenAI Responses API
- Spring Framework `RestClient`
- API key supplied through the `OPENAI_API_KEY` environment variable or a local `.env` file

The AI integration is implemented in `AiController` and `AiService`. The browser sends a question to the application's backend; the backend makes the external API request and returns the generated text.

## Project Structure

```text
digital-banking-system/
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── src/
│   ├── main/
│   │   ├── java/com/bank/
│   │   │   ├── controller/
│   │   │   │   ├── AiController.java
│   │   │   │   └── WebController.java
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   │   ├── AccountEntity.java
│   │   │   │   ├── TransactionEntity.java
│   │   │   │   ├── Currency.java
│   │   │   │   └── TransactionType.java
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AiService.java
│   │   │   │   └── BankService.java
│   │   │   └── DigitalBankingApplication.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   └── index.html
│   │       └── application.properties
│   └── test/
├── compose.yaml
├── Dockerfile
└── pom.xml
```

## Main Application Routes

| Method | Route | Description |
|---|---|---|
| GET | `/` | Displays the banking interface. |
| POST | `/account/create` | Creates a bank account. |
| POST | `/account/deposit` | Deposits funds into an account. |
| POST | `/account/withdraw` | Withdraws funds after PIN validation. |
| POST | `/account/transfer` | Transfers funds between accounts. |
| GET | `/account/statement?accountNumber=...` | Displays an account's transaction history. |
| GET | `/analytics?currency=AMD` | Displays total balance for a currency and the transfer count. |
| POST | `/api/ai/ask` | Sends a question to the AI Banking Assistant. |

### AI request example

```http
POST /api/ai/ask
Content-Type: application/json

{
  "question": "What is the difference between AMD and USD?"
}
```

## Database Design and Indexing

The `transactions` table defines two composite indexes through JPA's `@Table(indexes = ...)` mapping:

| Index | Columns | Intended query pattern |
|---|---|---|
| `idx_transactions_sender_timestamp` | `sender_account_number, timestamp` | Find transactions sent by an account and retrieve recent records first. |
| `idx_transactions_receiver_timestamp` | `receiver_account_number, timestamp` | Find transactions received by an account and retrieve recent records first. |

The transaction-history repository query filters by sender or receiver account number and sorts by timestamp descending. The indexes are intended to help PostgreSQL locate the relevant account records and support the time ordering. The actual execution plan depends on table size, data distribution, and PostgreSQL's cost estimates; index creation alone does not guarantee a faster query in every environment.

## Database Model

### Account

The `AccountEntity` class maps to the `accounts` table and stores:

- Database ID
- Account number
- Owner name
- PIN code
- Balance
- Currency

### Transaction

The `TransactionEntity` class maps to the `transactions` table and stores:

- Transaction ID
- Sender account number
- Receiver account number
- Amount
- Timestamp
- Transaction type (`TRANSFER`, `DEPOSIT`, or `WITHDRAW`)

Repositories extend `JpaRepository`. Custom repository methods provide account lookup, account existence checks, balance aggregation, transaction history, and transaction counts.

## Getting Started

### Prerequisites

- Java 17
- Maven 3.9+
- PostgreSQL 17 (for a local, non-Docker run)

### 1. Clone the repository

```bash
git clone https://github.com/avetisbs2-commits/digital-banking-system.git
cd digital-banking-system
```

### 2. Configure environment variables

Create a local `.env` file in the project root. Do not commit this file or share real API keys or database passwords.

```dotenv
LOCAL_DB_PASSWORD=your_local_postgres_password
OPENAI_API_KEY=your_openai_api_key
```

For Docker Compose, also provide:

```dotenv
DB_PASSWORD=your_postgres_password
```

The default local database configuration expects PostgreSQL at `localhost:5432`, with a database named `digital_bank_db` and username `postgres`. Create the database before starting the application, or adjust the datasource environment variables.

### 3. Run the application

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080).

### Run with Docker Compose

Docker Compose starts the Spring Boot application and PostgreSQL:

```bash
docker compose up --build
```

To start the optional Adminer database UI as well:

```bash
docker compose --profile tools up --build
```

When enabled, Adminer is available at [http://localhost:8091](http://localhost:8091).

## CI/CD

The GitHub Actions workflow at `.github/workflows/ci-cd.yml` runs on pushes and pull requests targeting `main`.

1. **Build and Test** — checks out the repository, configures Temurin Java 17, and runs `mvn clean verify`.
2. **Docker Build** — builds the Docker image after the build-and-test job succeeds.
3. **Deploy** — on pushes to `main`, triggers a Render deployment using the `RENDER_DEPLOY_HOOK_URL` GitHub Actions secret.

The workflow builds the Docker image but does not publish it to a container registry.

## Configuration

The main application configuration is in `src/main/resources/application.properties`.

| Variable | Purpose |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password. |
| `LOCAL_DB_PASSWORD` | Local database password fallback. |
| `PORT` | HTTP port; defaults to `8080`. |
| `OPENAI_API_KEY` | API key used by the AI Banking Assistant. |
| `DB_PASSWORD` | PostgreSQL password used by Docker Compose. |

## Current Limitations and Security Notes

This repository is a learning project and should not be used to store real customer data or process real money.

- PIN codes are currently stored directly in the account entity; production systems must store appropriately salted password hashes instead.
- The application does not currently include a full authentication and authorization framework such as Spring Security.
- Currency exchange rates are currently defined in application code and are not fetched from a live exchange-rate provider.
- The application does not implement the full security, compliance, auditing, and operational controls required of a real banking system.

## License

No license file is currently included in this repository. All rights are reserved by default unless a license is added.
