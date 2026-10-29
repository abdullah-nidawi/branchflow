# BranchFlow

BranchFlow is internal software for a company with multiple retail branches. This portfolio project is a backend for managing those branches and their day-to-day operations.

## Technology

Java 21, Spring Boot, PostgreSQL, Spring Data JPA, and Flyway.

## Current state

The project is under active development. The current implementation includes company and branch APIs, request validation, persistence configuration, and a shared not-found error response. Product catalog, inventory, tasks, scheduling, security, automated coverage, deployment, and complete API documentation are being added next.

## Run locally

1. Copy `.env.example` to `.env` and set a unique local `POSTGRES_PASSWORD`.
2. Start PostgreSQL with `docker compose up -d postgres`.
3. Load the local variables and run the API:

   ```sh
   set -a
   source .env
   set +a
   ./mvnw spring-boot:run
   ```

4. Open `http://localhost:8080/api/hello`.

`.env` is ignored by Git. Never reuse local database credentials outside your development machine.
