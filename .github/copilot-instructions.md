# Copilot instructions for Demo-Project-Java

## Project overview

This repository is a demonstration Spring Boot application for a CI/CD pipeline. It exposes a small product catalog API and is designed to exercise multiple testing stages in a pipeline, including functional, integration, regression, acceptance, Docker build, and deployment checks.

The application structure is intentionally simple:
- `src/main/java/com/example/demo/DemoApplication.java` boots the Spring Boot application.
- `src/main/java/com/example/demo/controller/ProductController.java` defines the `/api/products` REST endpoints.
- `src/main/java/com/example/demo/service/ProductService.java` contains the business logic.
- `src/main/java/com/example/demo/repository/ProductRepository.java` defines the JPA repository layer.
- `src/main/java/com/example/demo/model/Product.java` is the persisted JPA entity.

This is a classic Spring MVC + JPA layout with a single domain entity and a thin repository abstraction. Most changes should respect that layered structure.

## Build, test, and lint commands

Run everything from the repository root:

- Build the project without tests:
  `mvn clean package -DskipTests`
- Run the full Maven test suite:
  `mvn test`
- Run one specific test class:
  `mvn -Dtest=ProductRegressionTest test`
- Run one specific test method:
  `mvn -Dtest=ProductRegressionTest#productNameShouldNotBeNull test`

The project defines profile-based suites in `pom.xml`:
- Integration tests: `mvn test -Pintegration-test`
- Regression tests: `mvn test -Pregression-test`
- Acceptance tests: `mvn test -Pacceptance-test -DbaseUrl=http://localhost:8081`

There is no dedicated lint command in this repository; Maven build/test execution is the primary validation path.

## CI pipeline architecture

The Jenkins pipeline in `Jenkinsfile` is the main source of truth for the project’s deployment flow. It includes stages for:
- checkout
- build
- functional testing
- integration testing
- regression testing
- security scanning via Trivy
- Docker image build
- staging deployment
- acceptance testing
- load testing

The pipeline assumes a PostgreSQL dependency and environment variables such as database host, name, user, and password. The application is expected to run with a PostgreSQL-backed datasource and is exposed via Docker on port 8081.

## Local runtime and Docker conventions

- `docker-compose.yml` starts a PostgreSQL service named `postgres-db` and a `demo-app` service built from the root `Dockerfile`.
- The Docker image exposes port `8081`.
- The acceptance tests default to `http://localhost:8081` unless `-DbaseUrl` is supplied.
- The project uses Spring Boot 3.5 with Java 21 and PostgreSQL as the persistence layer.

## Test conventions to preserve

- Test classes live under `src/test/java/com/example/demo` and use JUnit 5.
- The naming conventions matter because Maven profiles select tests by filename suffix:
  - `*IntegrationTest.java`
  - `*RegressionTest.java`
  - `*AcceptanceTest.java`
- When adding new tests, follow the same naming pattern so they integrate with the CI pipeline automatically.
- Use the repository’s current style of direct `assert*` checks rather than introducing alternative testing frameworks or patterns.

## Code conventions specific to this repo

- Keep the application layers separated: controllers should delegate to services; services should use repositories; repositories should stay thin and JPA-based.
- Preserve the current Product model structure (`id`, `name`, `price`) and entity conventions when changing persistence-related code.
- Prefer minimal, direct Spring Boot changes over introducing new infrastructure unless the repo clearly needs it.
- Treat this as a demo repo focused on pipeline validation, not a large production service. Keep changes aligned with that scope.

## Practical editing guidance

- If you are changing the API, inspect both the controller and service together.
- If you are changing persistence behavior, inspect the entity and repository together.
- If a new test is needed, make sure it matches the existing profile-based naming conventions used by the Jenkins pipeline.
- When debugging pipeline issues, read `Jenkinsfile` and `pom.xml` together; they define the actual validation flow.
