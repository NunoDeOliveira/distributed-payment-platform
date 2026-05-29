#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=100
REQUESTS_PER_USER=10

echo "========== Concurrent test - $USERS users ========="

for i in $(seq 1 $USERS); do
    (
        counter=0
        for j in $(seq 1 $REQUESTS_PER_USER); do
            curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))" > /dev/null
            counter=$((counter + 1))
            
            if [ $((counter % 5)) -eq 0 ]; then
                sleep 3
		DELIVERY_ID=$(curl -s "$BASE_URL/delivery/deliveries" | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')
		curl -s -X DELETE "$BASE_URL/delivery/deliveries/$DELIVERY_ID"
            fi
        done
    ) &
done

wait
sleep 30
echo "=== Results ==="
ssh -i ~/.ssh/infra-k3s-aws.pem ubuntu@13.135.243.150 \
"sudo kubectl exec deploy/postgres-production -- psql -U postgres -d productiondb \
-c 'SELECT state, COUNT(*) FROM productions GROUP BY state;'"
ssh -i ~/.ssh/infra-k3s-aws.pem ubuntu@13.135.243.150 \
"sudo kubectl exec deploy/postgres-delivery -- psql -U postgres -d deliverydb \
-c 'SELECT state, COUNT(*) FROM deliveries GROUP BY state;'"
