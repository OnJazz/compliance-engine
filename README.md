# Compliance Engine

A backend application for evaluating financial transactions against configurable compliance rules.

The project is designed as a personal Java/Spring Boot backend project, with a focus on clean architecture, domain-driven design principles, REST APIs, persistence, security, automated testing and CI/CD.

## Overview

Compliance Engine evaluates financial transactions and determines whether they should be:

* **APPROVED**
* **MANUAL_REVIEW**
* **BLOCKED**

The evaluation is based on multiple compliance rules. Each rule can generate a violation with a severity level, which contributes to the transaction's overall risk score.

The project is intentionally designed around a backend architecture similar to what can be found in enterprise financial applications.

## Main Features

### Transaction management

* Create transactions
* Retrieve a transaction by ID
* List transactions with:

    * pagination
    * filtering by customer
    * filtering by status
    * filtering by transaction type
    * sorting
* Validation of listing parameters

### Compliance evaluation

Transactions are evaluated against multiple rules:

* Restricted country detection
* High-risk customer detection
* High transaction amount detection
* Transaction velocity detection

Each violation has:

* a rule code
* a severity
* a description

The resulting risk score is calculated from all violations.

### Compliance results

Compliance evaluations are persisted and can be retrieved through the REST API.

A transaction can therefore be evaluated once and its previously calculated compliance result retrieved later.

### Authentication and authorization

The API is secured using:

* JWT authentication
* RSA-signed tokens
* Role-based authorization
* `USER` and `ADMIN` roles

Protected endpoints require a valid JWT.

### Persistence

MongoDB is used as the persistence layer.

The project separates:

* domain models
* application ports
* MongoDB persistence models
* Spring Data repositories

This keeps the domain and application layers independent from the persistence implementation.

### API documentation

The REST API is documented using OpenAPI / Swagger.

The Swagger UI provides an interactive way to explore and test the API.

### Testing

The project includes several levels of automated tests:

* Unit tests
* Controller tests
* Repository integration tests
* REST API integration tests
* MongoDB integration tests with Testcontainers
* Security integration tests

MongoDB integration tests use Testcontainers to run against a real MongoDB instance rather than an in-memory replacement.

### Docker

The application can be run using Docker together with MongoDB.

### CI/CD

The project uses GitHub Actions to automatically:

* build the application
* run the test suite
* generate code coverage
* perform code quality analysis
* build the Docker image

## Architecture

The application follows a layered architecture inspired by Clean Architecture / Hexagonal Architecture principles.

```text
                         ┌──────────────────────┐
                         │      REST API        │
                         │ Controllers / DTOs   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     Application      │
                         │    Use Cases / Ports │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │        Domain        │
                         │ Rules / Models /     │
                         │ Compliance Engine    │
                         └──────────────────────┘
                                    ▲
                                    │
                         ┌──────────┴───────────┐
                         │   Infrastructure     │
                         │ MongoDB / Security / │
                         │ Configuration        │
                         └──────────────────────┘
```

The main application layers are:

```text
api/
application/
compliance/
customer/
rule/
transaction/
user/
infrastructure/
```

### Domain

The domain contains the business logic and domain models.

Examples include:

* `Transaction`
* `Customer`
* `ComplianceResult`
* `RiskScore`
* `ComplianceRule`
* `RuleViolation`
* `ComplianceEngine`

The compliance rules are implemented independently so that new rules can be added without modifying the core evaluation flow.

### Application

The application layer contains use cases and repository ports.

Examples:

* `EvaluateTransactionUseCase`
* `GetComplianceResultUseCase`
* `CreateTransactionUseCase`
* `GetTransactionUseCase`
* `GetTransactionsUseCase`

Repository interfaces are defined as application ports, while their MongoDB implementations live in the infrastructure layer.

### Infrastructure

The infrastructure layer contains technical implementations such as:

* MongoDB persistence
* Spring Data repositories
* JWT security
* RSA key configuration
* application configuration

## Technology Stack

| Technology          | Purpose                          |
| ------------------- | -------------------------------- |
| Java                | Backend language                 |
| Spring Boot         | Application framework            |
| Spring Web          | REST API                         |
| Spring Security     | Authentication and authorization |
| JWT                 | API authentication               |
| Spring Data MongoDB | MongoDB persistence              |
| MongoDB             | Database                         |
| Maven               | Build and dependency management  |
| JUnit 5             | Testing                          |
| Mockito             | Unit testing                     |
| Testcontainers      | Integration testing              |
| Docker              | Containerization                 |
| OpenAPI / Swagger   | API documentation                |
| GitHub Actions      | CI/CD                            |
| JaCoCo              | Code coverage                    |
| SonarCloud          | Code quality                     |

## API

### Authentication

```http
POST /api/auth/login
```

Returns a JWT that can be used to access protected endpoints.

### Transactions

Create a transaction:

```http
POST /api/transactions
```

Retrieve a transaction:

```http
GET /api/transactions/{transactionId}
```

List transactions:

```http
GET /api/transactions
```

Supported parameters include:

```text
customerId
status
type
page
size
sortBy
direction
```

Example:

```http
GET /api/transactions?customerId=customer-1&status=PENDING&page=0&size=20&sortBy=createdAt&direction=desc
```

### Compliance

Evaluate a transaction:

```http
POST /api/compliance/transactions/{transactionId}/evaluate
```

Retrieve a previously calculated compliance result:

```http
GET /api/compliance/transactions/{transactionId}
```

Example response:

```json
{
  "transactionId": "transaction-1",
  "status": "MANUAL_REVIEW",
  "riskScore": 50,
  "violations": [
    {
      "ruleCode": "HIGH_AMOUNT",
      "severity": "HIGH",
      "message": "Transaction amount exceeds the configured threshold"
    }
  ]
}
```

## Getting Started

### Prerequisites

Make sure the following tools are installed:

* Java
* Docker
* Maven, or use the Maven Wrapper included in the project

### Run with Docker

Start the application and MongoDB:

```bash
docker compose up --build
```

### Run locally

Start MongoDB and run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

## Running Tests

Run the complete test suite with:

```bash
./mvnw clean verify
```

This executes the unit and integration tests and generates the JaCoCo coverage report.

Some integration tests use Testcontainers and therefore require Docker to be available.

## API Documentation

Once the application is running, Swagger UI is available at:

```text
/swagger-ui/index.html
```

The OpenAPI specification is available at:

```text
/v3/api-docs
```

## Project Goals

This project was created to deepen and demonstrate backend development skills with Java and Spring Boot.

The main goals are:

* Strengthen Java and Spring Boot expertise
* Apply clean architecture principles
* Design a domain-oriented compliance engine
* Build a production-style REST API
* Implement secure authentication and authorization
* Work with MongoDB
* Write meaningful unit and integration tests
* Use Testcontainers for realistic integration testing
* Containerize the application
* Implement automated CI/CD and code quality checks

## Future Improvements

Potential future improvements include:

* Additional compliance rules
* More advanced transaction history analysis
* Asynchronous compliance evaluation
* Event-driven processing
* Improved observability and metrics
* Additional API capabilities
* Performance and load testing
* Deployment to a cloud environment

## License

This project is intended as a personal portfolio project.
