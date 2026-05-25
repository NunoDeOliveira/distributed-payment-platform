#!/bin/bash
BASE_URL="http://localhost:8080"

echo "============= Test to cancel a delivery by a client"
curl -s -X POST "$BASE_URL/production/productions?amount=20"
echo ""

# Wait delivery 
echo "Waiting for delivery to be created..."
DELIVERY_ID=""
for i in {1..15}; do
    sleep 1
    DELIVERY_ID=$(curl -s "$BASE_URL/delivery/deliveries" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
    if [ -n "$DELIVERY_ID" ]; then
        echo "Delivery found at second $i"
        break
    fi
done

echo "State of delivery:"
curl -s "$BASE_URL/delivery/deliveries"
echo ""

echo "Cancelling delivery id=$DELIVERY_ID..."
curl -s -X DELETE "$BASE_URL/delivery/deliveries/$DELIVERY_ID"
echo ""

sleep 4
echo "Final state deliveries:"
curl -s "$BASE_URL/delivery/deliveries"
echo ""
echo "Final state productions:"
curl -s "$BASE_URL/production/productions"
echo ""
