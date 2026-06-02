#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=5
REQUESTS=6


echo "========== test cancel every 5 productions"
NEXT_ID=1

for i in $(seq 1 $REQUESTS); do
    echo "=== Ronda $i ==="
    # Bucle 1: 4 producciones normales
    for j in $(seq 1 4); do
        curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))" &
    done
    # Bucle 2: 1 produccion + cancel en paralelo
    curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))" &
    sleep 0.2 && curl -s -X DELETE "$BASE_URL/production/productions/$((NEXT_ID + 4))" &
    
    NEXT_ID=$((NEXT_ID + USERS))
    wait
    sleep 1
done



echo "Ending test ..."

