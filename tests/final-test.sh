#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=5
REQUESTS=20

echo "========== test cancel every 5 productions"

NEXT_ID=1
for i in $(seq 1 $REQUESTS); do
    echo "=== Ronda $i ==="
    for j in $(seq 1 $USERS); do
        curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))" &
    done
wait
done

echo "Ending test ..."


