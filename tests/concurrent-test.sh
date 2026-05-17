#!/bin/bash
# Test script for Saga pattern evaluation
BASE_URL="http://IP_DNS:8080"


echo "Starting concurrent test ..."


echo "Creating initial stock..."
curl -s -X POST "$BASE_URL/production/productions?amount=20"
echo ""
sleep 40

echo "Starting test..."

# Variable for number of the request
CONCURRENT=5

# concucency for production
for i in $(seq 1 $CONCURRENT); do
    curl -s -X POST "$BASE_URL/production/productions?amount=20" &
    sleep 1
done

# concucency for delivery
for i in $(seq 1 $CONCURRENT); do
    curl -s -X POST "$BASE_URL/delivery/deliveries?amount=20" &
    sleep 1
done




echo "Ending test ..."
