#!/bin/bash
set -e

echo "Logging in..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@freshpress.com","password":"password123"}' | grep -o '"token":"[^"]*' | grep -o '[^"]*$')

echo "Fetching services..."
curl -s -X GET http://localhost:8080/api/v1/services -H "Authorization: Bearer $TOKEN" | head -c 200
echo "..."

echo "Fetching customers..."
curl -s -X GET "http://localhost:8080/api/v1/customers?page=0&size=2" -H "Authorization: Bearer $TOKEN" | head -c 200
echo "..."

echo "Fetching orders..."
curl -s -X GET "http://localhost:8080/api/v1/orders" -H "Authorization: Bearer $TOKEN" | head -c 200
echo "..."

echo "Done."
