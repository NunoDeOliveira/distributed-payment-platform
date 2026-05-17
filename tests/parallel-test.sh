#!/bin/bash
# Test script for Saga pattern evaluation
BASE_URL="http://3.11.189.104:30000"


echo "Starting test ..."

echo "Creating production ..."
curl -s -X POST "$BASE_URL/production/productions?amount=40"
echo ""
sleep 40

echo "Produce and deliver simultaneously"
for i in $(seq 1 20); do
    echo "Iteration $i"
    curl -s -X POST "$BASE_URL/production/productions?amount=30" &
    curl -s -X POST "$BASE_URL/delivery/deliveries?amount=20" &
    wait
    sleep 2
done

echo "Ending test ..."
