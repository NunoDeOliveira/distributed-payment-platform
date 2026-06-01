#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=5
REQUESTS_PER_USER=5
 

echo "========== Concurrent test - $USERS users ========="

for ((i=1; i<=USERS; i++)); do

    for ((j=1; j<=REQUESTS_PER_USER; j++)); do
        curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))"
    done
    DELIVERY_ID=$(curl -s "$BASE_URL/delivery/deliveries" | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')
    curl -s -X DELETE "$BASE_URL/delivery/deliveries/$DELIVERY_ID"
    
done



wait
sleep 10
echo "=== Results ==="
ssh -i ~/.ssh/infra-k3s-aws.pem ubuntu@13.43.203.132 \
"sudo kubectl exec deploy/postgres-production -- psql -U postgres -d productiondb \
-c 'SELECT state, COUNT(*) FROM productions GROUP BY state;'"
ssh -i ~/.ssh/infra-k3s-aws.pem ubuntu@13.43.203.132 \
"sudo kubectl exec deploy/postgres-delivery -- psql -U postgres -d deliverydb \
-c 'SELECT state, COUNT(*) FROM deliveries GROUP BY state;'"
