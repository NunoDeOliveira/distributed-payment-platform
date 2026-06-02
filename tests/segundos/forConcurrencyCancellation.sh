#!/bin/bash
#forConcurrencyCancellation.sh
BASE_URL="http://localhost:8080"

sleep 2
DELIVERY_ID=$(curl -s "$BASE_URL/delivery/deliveries" | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')
curl -s -X DELETE "$BASE_URL/delivery/deliveries/$DELIVERY_ID"
