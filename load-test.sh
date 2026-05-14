#!/bin/bash
# Load test script for Saga pattern evaluation

# Reset databases
echo "Resetting databases..."
docker exec postgres-tfg psql -U postgres -d productiondb -c "TRUNCATE productions RESTART IDENTITY;"
docker exec postgres-tfg psql -U postgres -d deliverydb -c "TRUNCATE deliveries RESTART IDENTITY;"
docker exec postgres-tfg psql -U postgres -d inventorydb -c "TRUNCATE stock_entries, stock_reservations RESTART IDENTITY;"
echo "Databases reset"

CONCURRENT=10
echo "Launching $CONCURRENT concurrent productions..."

for i in $(seq 1 $CONCURRENT); do
    curl -s -X POST "http://localhost:8080/production/productions?amount=30" &
done
wait
echo "All productions launched"

sleep 5

echo "Launching $CONCURRENT concurrent deliveries..."
for i in $(seq 1 $CONCURRENT); do
    curl -s -X POST "http://localhost:8080/delivery/deliveries?amount=20" &
done
wait
echo "All deliveries launched"
