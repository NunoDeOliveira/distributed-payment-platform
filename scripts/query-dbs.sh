#!/usr/bin/env bash
HOST="${1:-localhost}"

echo "=== PAYMENT SERVICE ===" &&
PGPASSWORD=postgres psql -h "$HOST" -p 30432 -U postgres -d paymentdb -c "SELECT id, amount, correlation_id AS \"correlationId\", register FROM payments ORDER BY id ASC;" &&
echo "=== COMMISSION SERVICE ===" &&
PGPASSWORD=postgres psql -h "$HOST" -p 30433 -U postgres -d commissiondb -c "SELECT id, amount, total_amount AS \"totalAmount\", correlation_id AS \"correlationId\", register FROM commissions ORDER BY id ASC;" &&
echo "=== ACCOUNT SERVICE ===" &&
PGPASSWORD=postgres psql -h "$HOST" -p 30435 -U postgres -d accountdb -c "SELECT id, balance_account AS \"balanceAccount\", correlation_id AS \"correlationId\", register FROM balances ORDER BY id ASC;" &&
echo "=== LEDGER SERVICE ===" &&
PGPASSWORD=postgres psql -h "$HOST" -p 30434 -U postgres -d movementdb -c "SELECT id, amount, correlation_id AS \"correlationId\", register FROM movements ORDER BY id ASC;"
