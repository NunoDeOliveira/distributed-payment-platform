#!/bin/bash
# Test script for Saga pattern evaluation
BASE_URL="http://13.134.165.98:30000"


echo "============== recived a production with random amount"
#curl -s -X POST "$BASE_URL/production/productions?amount=30"
echo ""
sleep 20

echo "Produce and deliver simultaneously"
for i in $(seq 1 50); do
    echo "Iteration $i"
    curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))" 
    wait
done

sleep 20

echo "Ending test ...


