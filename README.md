# Automated Testing Pipeline Demo

A Java Spring Boot application designed to demonstrate a CI/CD pipeline with automated testing stages, containerization, and deployment orchestration. The project includes Maven-based tests, Docker Compose support, and a Jenkins pipeline that exercises build, functional, integration, regression, security, staging, and acceptance checks.

## Overview

This repository shows how a small Spring Boot REST application can be validated through multiple stages in a real delivery workflow. The application exposes a simple product catalog API backed by PostgreSQL and is meant to model a realistic test pipeline rather than a large enterprise system.

## Tech Stack

- Java 21
- Spring Boot 3.5.15
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- Docker
- Jenkins
- JUnit 5
- REST Assured
- Trivy

## Project Structure

```text
.
├── .github/
│   └── copilot-instructions.md
├── src/
│   ├── main/
│   │   └── java/com/example/demo/
│   │       ├── DemoApplication.java
│   │       ├── controller/
│   │       │   └── ProductController.java
│   │       ├── model/
│   │       │   └── Product.java
│   │       ├── repository/
│   │       │   └── ProductRepository.java
│   │       └── service/
│   │           └── ProductService.java
│   └── test/java/com/example/demo/
│       ├── ProductFunctionalTest.java
│       ├── ProductIntegrationTest.java
│       ├── ProductRegressionTest.java
│       └── ProductAcceptanceTest.java
├── Dockerfile
├── Jenkinsfile
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── README.md
```

## Application Architecture

The app follows a simple layered design:

- `ProductController` handles HTTP requests under `/api/products`
- `ProductService` contains the business logic
- `ProductRepository` extends `JpaRepository<Product, Long>` for persistence
- `Product` is the JPA entity representing a product entry

The API currently supports:

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `DELETE /api/products/{id}`

## Prerequisites

Before running this project locally, make sure you have:

- Java 21+
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL (or use the provided Docker Compose setup)

## Local Setup

### 1. Clone the repository

```bash
git clone git@github.com:JanithaDissanayaka/automated-testing-pipeline.git
cd automated-testing-pipeline
```

### 2. Start PostgreSQL with Docker Compose

```bash
docker compose up -d postgres-db
```

This starts a PostgreSQL container with:

- database: `automated_testing`
- username: `appuser`
- password: `app123`

### 3. Run the application

```bash
mvn spring-boot:run
```

The application will start on port `8081` by default.

## Build and Test Commands

### Build without tests

```bash
mvn clean package -DskipTests
```

### Run the full test suite

```bash
mvn test
```

### Run a single test class

```bash
mvn -Dtest=ProductRegressionTest test
```

### Run a single test method

```bash
mvn -Dtest=ProductRegressionTest#productNameShouldNotBeNull test
```

### Maven profile-based suites

The project includes profile-based test selection in `pom.xml`:

```bash
mvn test -Pintegration-test
mvn test -Pregression-test
mvn test -Pacceptance-test -DbaseUrl=http://localhost:8081
```

## Docker Setup

The application can be containerized with the provided Dockerfile and Compose configuration.

### Build the Docker image

```bash
docker build -t demo-app:latest .
```

### Run the full stack using Docker Compose

```bash
docker compose up --build
```

This starts:

- PostgreSQL (`postgres-db`)
- the Spring Boot app (`demo-app`)

The app is exposed on:

- `http://localhost:8081`

## Jenkins CI/CD Pipeline

The repository includes a `Jenkinsfile` that models a multi-stage delivery pipeline:

1. Checkout source code
2. Build the Java application
3. Run functional tests
4. Run integration tests
5. Run regression tests
6. Run Trivy security scanning
7. Build Docker image
8. Deploy to staging
9. Run acceptance testing
10. Run load testing

The pipeline expects PostgreSQL environment variables for the target database and uses the product API as the application under test.

## Testing Strategy

This repo demonstrates test separation by responsibility:

- `ProductFunctionalTest` - application context and basic functional validation
- `ProductIntegrationTest` - persistence and repository interaction
- `ProductRegressionTest` - regression checks for model behavior
- `ProductAcceptanceTest` - API availability using REST Assured

This pattern is useful for showing how a CI pipeline can gate releases using different test scopes.

## Security and Quality Checks

The Jenkins pipeline includes a Trivy scan using:

```bash
trivy fs --severity HIGH,CRITICAL --exit-code 1 .
```

This helps surface security issues during the pipeline lifecycle.

## Running the API Manually

After the app starts, you can test it with curl:

```bash
curl http://localhost:8081/api/products
```

Create a product:

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":1500}'
```

## Known Repository Behavior

- This project is intentionally small and demonstration-focused.
- The architecture is intentionally straightforward: no complex multi-module structure.
- The Jenkins pipeline and Maven profiles are part of the learning value of the repo.

## Git and Version Control

This repository is configured with a Git remote:

```bash
git remote -v
```

## License

This project is a demo repository for CI/CD testing and does not include a formal enterprise license unless added later.

## Contributing

This is a learning/demo project. Contributions should remain aligned with the repository’s purpose: demonstrating a CI/CD pipeline with practical testing stages and simple Spring Boot application behavior.
