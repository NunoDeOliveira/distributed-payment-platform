## Local Execution Guide

This document contains the detailed commands for running and testing theplatform locally. Run the commands from the repository root unless anotherlocation is specified.

### Prerequisites

- Docker Engine

- Docker Compose

- curl

### Start the Platform

```bash
docker compose up -d
```

Check that the containers are running:

```bash
docker compose ps
```

**1. Add an Initial Balance**

```bash
curl -X POST \
  "http://localhost:8080/balances/add?balanceAccount=500.00"
```

**2. Create a Payment**

```bash
curl -X POST "http://localhost:8080/payments" \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00,"method":"INTERNATIONAL_TRANSFER"}'
```

The response contains the payment ID.

**3. List Payments**

```bash
curl "http://localhost:8080/payments"
```

**4. Cancel a Payment**

Replace <PAYMENT_ID> with the identifier returned by the platform:

```bash
curl -X DELETE "http://localhost:8080/payments/<PAYMENT_ID>"
```

**5. Run the Current Demonstration Script**

```bash
./test-cancel-payment.sh
```

This script demonstrates the flow, but it is not yet an automated E2E testwith assertions.

**6. Inspect Logs**

```bash
docker compose logs --tail=200
```

Follow the logs in real time:

```bash
docker compose logs -f
```

**7. Stop the Platform**

```bash
docker compose down
```

## Reference

Docker Compose documentation: https://docs.docker.com/compose/
