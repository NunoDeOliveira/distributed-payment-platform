
## PostgreSQl Queries

**1. Payment Service**: shows the payment lifecycle:

```bash
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d paymentdb -c "SELECT id, amount, correlation_id AS \"correlationId\", register FROM payments ORDER BY id ASC;"
```

**2. Commission Service**: shows the commission calculated:

```bash
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d commissiondb -c "SELECT id, amount, total_amount AS \"totalAmount\", correlation_id AS \"correlationId\", register FROM commissions ORDER BY id ASC;"
```

**3. Account Service**: shows the account balance:

```bash
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d accountdb -c "SELECT id, balance_account AS \"balanceAccount\", correlation_id AS \"correlationId\", register FROM balances ORDER BY id ASC;"
```

**4. Ledger Service**: shows the record movements:

```bash
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d movementdb -c "SELECT id, amount, correlation_id AS \"correlationId\", register FROM movements ORDER BY id DESC LIMIT 20;"
```

**5. Query for monitoring the states in real time.**

```bash
watch -n 2 '
echo "=== PAYMENT SERVICE ===" &&
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d paymentdb -c "SELECT id, amount, correlation_id AS \"correlationId\", register FROM payments ORDER BY id ASC;" &&
echo "=== COMMISSION SERVICE ===" &&
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d commissiondb -c "SELECT id, amount, total_amount AS \"totalAmount\", correlation_id AS \"correlationId\", register FROM commissions ORDER BY id ASC;" &&
echo "=== ACCOUNT SERVICE ===" &&
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d accountdb -c "SELECT id, balance_account AS \"balanceAccount\", correlation_id AS \"correlationId\", register FROM balances ORDER BY id ASC;" &&
echo "=== LEDGER SERVICE ===" &&
docker exec -e PGPASSWORD=postgres postgres-tfg psql -U postgres -d movementdb -c "SELECT id, amount, correlation_id AS \"correlationId\", register FROM movements ORDER BY id ASC;"
'
```

> **Note:**: The refresh interval can be adjusted by changing the value of the `-n` parameter. The default time is 2 seconds.

Stop watch with Ctrl+C.






### Reference

PostgreSQL psql documentation: https://www.postgresql.org/docs/current/app-psql.html
