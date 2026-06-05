#!/bin/bash
HOST="localhost"
USER="postgres"
PGPASSWORD="postgres"
export PGPASSWORD
ID=${1:-1}

for i in {1..60}; do
  echo "==================== $(date '+%H:%M:%S') ===================="
  echo "--- Production ---"
  psql -h "$HOST" -p 5433 -U "$USER" -d productiondb -c "SELECT TO_CHAR(NOW(), 'HH24:MI:SS') AS snapshot, id, amount, state, TO_CHAR(start_time, 'HH24:MI:SS') AS start, TO_CHAR(end_time, 'HH24:MI:SS') AS end FROM productions WHERE id=$ID;"
  echo "--- Delivery ---"
  psql -h "$HOST" -p 5434 -U "$USER" -d deliverydb -c "SELECT TO_CHAR(NOW(), 'HH24:MI:SS') AS snapshot, id, production_id, amount, state, TO_CHAR(start_time, 'HH24:MI:SS') AS start, TO_CHAR(end_time, 'HH24:MI:SS') AS end FROM deliveries WHERE production_id=$ID;"
  echo "--- Inventory reservations ---"
  psql -h "$HOST" -p 5435 -U "$USER" -d inventorydb -c "SELECT TO_CHAR(NOW(), 'HH24:MI:SS') AS snapshot, id, reservation_id, reservation_amount FROM stock_reservations ORDER BY id DESC LIMIT 3;"
done
