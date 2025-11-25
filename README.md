# ClusteredData Warehouse

Spring Boot 3.5.7 service that ingests FX deal data. The API validates and persists deals, prevents duplicates, and supports CSV imports without rolling back previously processed rows.

## Stack

- Java 17+ / Spring Boot 3.5.7
- PostgreSQL 16 + Flyway schema migrations
- Maven build with Testcontainers-backed integration tests
- Docker Compose for local deployment

## Features

- REST endpoint to create a single FX deal with bean validation
- CSV import endpoint that processes each row independently (no rollback)
- Duplicate deal detection enforced at DB level
- Structured error handling + logging
- Sample CSV file under `samples/deals-sample.csv`
- Makefile helpers and containerized runtime

## Getting Started

1. **Install prerequisites**: Java 17+, Maven 3.9+, Docker (optional but recommended).
2. **Configure Postgres (optional)**
3. **Build & test**:
   ```bash
   make build
   make test
   ```
4. **Run locally** (expects Postgres running at the configured URL):
   ```bash
   make run
   ```

### Docker Compose Deployment

```
make docker-up   # builds the app image and starts postgres (host port 5433) + service
make docker-down # stops and removes containers/volumes
```

The API will be available at `http://localhost:8080` once the stack is healthy.

## API

### Create Deal

```
POST /api/v1/deals
Content-Type: application/json
```

```json
{
  "dealUniqueId": "FX-2024-0001",
  "fromCurrencyIso": "USD",
  "toCurrencyIso": "EUR",
  "dealTimestamp": "2024-11-25T10:15:30Z",
  "dealAmount": 1250000.45
}
```

- Returns `201 Created` with persisted deal payload
- Returns `409 Conflict` when the `dealUniqueId` already exists

### Import CSV

```
POST /api/v1/deals/import
Content-Type: multipart/form-data (field name: file)
```

CSV headers must be exactly:
`deal_unique_id,from_currency_iso,to_currency_iso,deal_timestamp,deal_amount`

Response example:

```json
{
  "totalRows": 3,
  "successfulRows": 2,
  "failedRows": 1,
  "failures": [
    { "rowNumber": 3, "reason": "Deal with id 'FX-2024-0001' already exists" }
  ]
}
```

Each CSV row is validated independently; failures never roll back successfully saved deals.

### Manual API Smoke Tests

The following curl commands were executed against the Docker stack (`make docker-up`). They can be reused to verify the service quickly:

```bash
# Happy-path deal creation
curl -s -X POST http://localhost:8080/api/v1/deals \
  -H 'Content-Type: application/json' \
  -d '{
        "dealUniqueId":"FX-TEST-002",
        "fromCurrencyIso":"USD",
        "toCurrencyIso":"JPY",
        "dealTimestamp":"2024-11-25T09:00:00Z",
        "dealAmount":500.75
      }'
# => {"id":1,"dealUniqueId":"FX-TEST-002","fromCurrencyIso":"USD","toCurrencyIso":"JPY","dealTimestamp":"2024-11-25T09:00:00Z","dealAmount":500.75}

# Duplicate submission (expect 409 Conflict)
curl -s -o - -w '\n%{http_code}\n' -X POST http://localhost:8080/api/v1/deals \
  -H 'Content-Type: application/json' \
  -d '{
        "dealUniqueId":"FX-TEST-002",
        "fromCurrencyIso":"USD",
        "toCurrencyIso":"JPY",
        "dealTimestamp":"2024-11-25T09:00:00Z",
        "dealAmount":500.75
      }'
# => {"timestamp":"...","status":409,"error":"Conflict","message":"Deal with id 'FX-TEST-002' already exists",...}
# => 409

# CSV import using provided sample file
curl -s -X POST http://localhost:8080/api/v1/deals/import \
  -F file=@samples/deals-sample.csv
# => {"totalRows":3,"successfulRows":3,"failedRows":0,"failures":[]}
```

## Testing

- `DealServiceTest`: unit tests for service logic & failure tallying
- `DealControllerTest`: MockMvc slice tests for REST contracts
- `DealServiceIntegrationTest`: full-stack Testcontainers coverage with PostgreSQL

Run all tests via `make test`.

## Possible Future Enhancements

- Add authentication/authorization
- Stream imports via message broker
- Push metrics/logs to centralized observability stack
