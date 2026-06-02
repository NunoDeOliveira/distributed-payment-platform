#!/bin/bash
BASE_URL="http://localhost:8080"

# Predice el próximo ID
NEXT_ID=$(curl -s "$BASE_URL/production/productions" | python3 -c "import sys,json; data=json.load(sys.stdin); print(max([p['id'] for p in data])+1 if data else 1)")
echo "Próximo ID predicho: $NEXT_ID"

# Lanza POST y DELETE en paralelo
curl -s -X POST "$BASE_URL/production/productions?amount=10" &
sleep 0.15 && curl -s -X DELETE "$BASE_URL/production/productions/$NEXT_ID" &
wait

sleep 1
curl -s "$BASE_URL/production/productions/$NEXT_ID"
echo ""
echo "Ending test ..."

