#!/bin/bash
BASE_URL="http://localhost:8080"

echo "Lanzando producción..."
curl -s -X POST "$BASE_URL/production/productions?amount=20"
echo ""

echo "Esperando 4 segundos para que delivery se cree..."
sleep 4

echo "Estado de deliveries:"
curl -s "$BASE_URL/delivery/deliveries" | python3 -m json.tool

echo "Cancelando delivery id=1..."
curl -s -X DELETE "$BASE_URL/delivery/deliveries/1"
echo ""

sleep 3

echo "Estado final:"
curl -s "$BASE_URL/delivery/deliveries" | python3 -m json.tool
