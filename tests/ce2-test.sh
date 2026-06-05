#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=$1
REQUESTS=$2
#AMOUNT=10

Sleep 0.7
echo "USERS=$USERS REQUESTS=$REQUESTS"
START=$(date +%s)
for i in $(seq 1 "$REQUESTS"); do
    for j in $(seq 1 "$USERS"); do
    (
        curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))"
    ) &
    done
    wait
done
END=$(date +%s)
DURATION=$((END - START))
echo "Duración envío peticiones: $DURATION segundos"
echo "Peticiones enviadas: $((USERS * REQUESTS))"
