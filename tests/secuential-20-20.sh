#!/bin/bash
# Test script for Saga pattern evaluation
BASE_URL="http://3.11.16.193:30000"


echo "Starting test ..."

echo "============== phase1: Production 30 and delivery 20"
#curl -s -X POST "$BASE_URL/production/productions?amount=30"
echo ""
sleep 30

echo "Produce and deliver simultaneously"
for i in $(seq 1 10); do
    echo "Iteration $i"
    curl -s -X POST "$BASE_URL/production/productions?amount=20" &
    curl -s -X POST "$BASE_URL/delivery/deliveries?amount=20" &
    wait
done

sleep 30

echo "Ending test ..."
