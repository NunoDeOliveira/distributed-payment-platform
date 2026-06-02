#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=5
REQUESTS=6
echo "========== test random production and cancel each 5 productions"
echo ""
for i in $(seq 1 "$REQUESTS"); do
    echo "=== Ronda $i ==="
    TMP_FILE=$(mktemp)
    for j in $(seq 1 "$USERS"); do
    (
        RESPONSE=$(curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))")
        ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
        if [ -n "$ID" ]; then
            echo "$ID" >> "$TMP_FILE"
        fi
    ) &
    done
    wait
    LAST_ID=$(sort -n "$TMP_FILE" | tail -1)
    rm "$TMP_FILE"
    if [ -n "$LAST_ID" ]; then
        echo "Cancelando production id=$LAST_ID"
        # Buscar el deliveryId asociado a esa producción
        DELIVERY_ID=$(curl -s "$BASE_URL/delivery/deliveries" | python3 -c "
import sys, json
try:
    deliveries = json.load(sys.stdin)
    matches = [d for d in deliveries if str(d.get('productionId')) == '$LAST_ID']
    if matches:
        print(matches[0]['id'])
except:
    pass
")
        if [ -n "$DELIVERY_ID" ]; then
            echo "Cancelando delivery id=$DELIVERY_ID asociado a production id=$LAST_ID"
            curl -s -X DELETE "$BASE_URL/delivery/deliveries/$DELIVERY_ID"
        else
            echo "No hay delivery asociado a production id=$LAST_ID todavia"
        fi
        echo ""
    fi
done
echo "Ending test ..."


