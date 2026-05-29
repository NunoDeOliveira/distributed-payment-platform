#!/bin/bash
BASE_URL="http://localhost:8080"
USERS=50
REQUESTS_PER_USER=10

echo "========== Concurrent test - $USERS users ========="

for i in $(seq 1 $USERS); do
    (
        for j in $(seq 1 $REQUESTS_PER_USER); do
            curl -s -X POST "$BASE_URL/production/productions?amount=$((5 + RANDOM % 15))" 
            
            # Binary random variable (0 or 1)
            if [ $((RANDOM % 2)) -eq 1 ]; then
                bash ~/UOC/microservices/tests/forConcurrencyCancellation.sh
            fi
        done
    ) &
done
wait

sleep 10
echo "Ending test ..."

# Final stock
ssh -i ~/.ssh/infra-k3s-aws.pem ubuntu@13.135.243.150 \
"sudo kubectl exec deploy/postgres-inventory -- psql -U postgres -d inventorydb \
-c 'SELECT SUM(amount) as stock_total FROM stock_entries;'"
