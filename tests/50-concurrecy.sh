#!/bin/bash
# Test script for Saga pattern evaluation
BASE_URL="http://3.11.16.193:30000"

echo "Starting medium load test..."

for i in $(seq 1 5); do
    echo "Iteration $i"
    for j in $(seq 1 50); do
        curl -s -X POST "$BASE_URL/production/productions?amount=20" &
        curl -s -X POST "$BASE_URL/delivery/deliveries?amount=20" &
    done
    wait
done
sleep 30




echo "Ending test ..."
