#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=5
REQUESTS=5

echo "========== test cancel every 5 productions"

NEXT_ID=1
for i in $(seq 1 $REQUESTS); do
    echo "=== Ronda $i ==="
    for j in $(seq 1 $USERS); do
        curl -s -X POST "$BASE_URL/production/productions?amount=$((10 + RANDOM % 20))" &
    done
    wait
    CANCEL_ID=$((NEXT_ID + USERS - 1))
    echo "Cancelando production id=$CANCEL_ID"
    curl -s -X DELETE "$BASE_URL/production/productions/$CANCEL_ID"
    NEXT_ID=$((NEXT_ID + USERS))
done

echo "Ending test ..."


