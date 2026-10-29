# BranchFlow

BranchFlow is an operations API for a company with multiple retail branches. It tracks a shared product catalog, branch-level stock and immutable stock movements, tasks, employees, and shifts.

## Stack

- Java 21 and Spring Boot 4.1
- PostgreSQL, Spring Data JPA, and Flyway
- Bean Validation and Spring Security HTTP Basic
- React, TypeScript, and Vite operations dashboard
- OpenAPI / Swagger UI and Docker Compose

## Start the full application

1. Copy `.env.example` to `.env`.
2. Set a unique PostgreSQL password and three distinct passwords of at least 12 characters for the demo roles. `.env` is ignored by Git.
3. Build and start both services:

   ```sh
   docker compose up --build
   ```

4. Open the dashboard at `http://localhost:3000`. Check API health at `http://localhost:8080/actuator/health`; the API docs are at `http://localhost:8080/swagger-ui.html` (also available through the dashboard proxy at `http://localhost:3000/swagger-ui.html`).

Sign in with one of the configured demo accounts from `.env`. The dashboard keeps the HTTP Basic credential in memory for the current browser session and clears it when you sign out or reload.

Flyway applies migrations on startup. PostgreSQL data persists in the `branchflow_pgdata` volume. To stop the services, run `docker compose down`. `docker compose down -v` also deletes the local database volume.

For local IDE development, start only PostgreSQL with `docker compose up -d postgres`, export the variables from `.env`, and run `./mvnw spring-boot:run`.

## Roles

The first boot creates database-backed demo accounts from environment variables. Passwords are stored as BCrypt hashes. Existing accounts are not overwritten on restart; change a demo password by recreating the local database or updating its account hash.

| Role | Access |
| --- | --- |
| `ADMIN` | Read and write all resources; create companies |
| `MANAGER` | Read and write operational resources |
| `EMPLOYEE` | Read-only access |

HTTP Basic is intended for this backend portfolio demo. Do not expose these demo credentials to the public internet.

## API walkthrough

All operational endpoints require HTTP Basic authentication. The hello endpoint and API documentation are public.

Create a company (admin):

```sh
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -H 'Content-Type: application/json' \
  -d '{"name":"Northstar Retail"}' http://localhost:8080/api/companies
```

Create a branch with the returned `companyId`:

```sh
curl -u "$ADMIN_USERNAME:$ADMIN_PASSWORD" -H 'Content-Type: application/json' \
  -d '{"companyId":1,"name":"Dortmund Center","city":"Dortmund"}' \
  http://localhost:8080/api/branches
```

Add a product using the same company ID, then create a zero-quantity stock record for a branch. Stock is changed only through movements; deliveries increase it, sales and damage decrease it, and corrections accept a signed change. The API rejects movements that would make stock negative. A transfer writes paired outbound and inbound ledger rows in one transaction.

```sh
curl -u "$MANAGER_USERNAME:$MANAGER_PASSWORD" -H 'Content-Type: application/json' \
  -d '{"companyId":1,"sku":"COLA-1L","name":"Cola 1 L"}' \
  http://localhost:8080/api/products

curl -u "$MANAGER_USERNAME:$MANAGER_PASSWORD" -H 'Content-Type: application/json' \
  -d '{"productId":1,"minimumQuantity":5}' \
  http://localhost:8080/api/branches/1/stock

curl -u "$MANAGER_USERNAME:$MANAGER_PASSWORD" -H 'Content-Type: application/json' \
  -d '{"type":"DELIVERY","quantityChange":30,"reason":"Opening delivery"}' \
  http://localhost:8080/api/stock/1/movements
```

## Main endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST`, `GET` | `/api/companies` | Create/list companies |
| `POST`, `GET` | `/api/branches` | Create/list branches; filter with `?companyId=` |
| `GET` | `/api/branches/{id}` | Read a branch |
| `POST`, `GET` | `/api/products` | Create/list products by company |
| `POST`, `GET` | `/api/branches/{id}/stock` | Add/list branch stock records |
| `GET` | `/api/branches/{id}/stock/low` | Find items at or below minimum |
| `POST`, `GET` | `/api/stock/{id}/movements` | Record a movement/read its audit history |
| `POST` | `/api/stock/transfers` | Transfer inventory between branches |
| `POST`, `GET` | `/api/branches/{id}/tasks` | Create/list tasks; filter status/priority and paginate |
| `PATCH` | `/api/tasks/{id}/status` | Change task status |
| `POST`, `GET` | `/api/branches/{id}/employees` | Create/list branch employees |
| `POST`, `GET` | `/api/branches/{id}/shifts` | Schedule/list shifts |
| `PATCH` | `/api/shifts/{id}/status` | Complete or cancel a shift |

API errors use a consistent JSON shape with an HTTP status, message, timestamp, and optional field-level validation details. Invalid input returns `400`, missing resources return `404`, business conflicts return `409`, and access failures return `401` or `403`.

## Design notes

- Controllers handle HTTP and validation; services enforce business rules and transaction boundaries; repositories handle persistence.
- API responses use DTOs instead of serializing JPA entities.
- Flyway migrations are append-only. Never edit a migration already applied to a shared database.
- Stock movements form an audit ledger; current quantity is updated in the same transaction as each movement.
- Inventory rows use pessimistic locks for concurrent updates and an optimistic version column as a second safeguard.
- Shift overlap uses the interval rule `newStart < existingEnd && newEnd > existingStart`; cancelled shifts do not block scheduling.
- Local sample data credentials are read from ignored `.env`. Use managed secrets and stronger deployment controls for any hosted environment.

## Build and tests

```sh
./mvnw verify
```

The frontend can be built independently with `cd frontend && npm ci && npm run lint && npm run build`. GitHub Actions runs both verification pipelines on pushes and pull requests.
