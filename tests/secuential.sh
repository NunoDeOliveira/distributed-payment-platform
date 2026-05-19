#!/bin/bash
# Test script for Saga pattern evaluation
BASE_URL="http://16.60.108.50:30000"


echo "Starting test ..."

echo "============== phase1: Production 30 and delivery 20"
#curl -s -X POST "$BASE_URL/production/productions?amount=30"
echo ""
sleep 20

echo "Produce and deliver simultaneously"
for i in $(seq 1 5); do
    echo "Iteration $i"
    curl -s -X POST "$BASE_URL/production/productions?amount=30" &
    curl -s -X POST "$BASE_URL/delivery/deliveries?amount=20" &
    wait
    sleep 2
done

echo "================== Phase2: Production 20 and delivery 25"
for i in $(seq 1 5); do
    echo "Iteration $i"
    curl -s -X POST "$BASE_URL/production/productions?amount=20" &
    curl -s -X POST "$BASE_URL/delivery/deliveries?amount=30" &
    wait
    sleep 2
done
sleep 30

echo "Ending test ..."
