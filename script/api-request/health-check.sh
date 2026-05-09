#!/bin/bash

# Health Check API Test Script
# This script tests the health endpoint of the backend service

# Configuration
BASE_URL="http://localhost:8080"
HEALTH_ENDPOINT="/api/health"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=================================================="
echo "Testing Health Endpoint"
echo "=================================================="
echo ""

# Test Health Endpoint
echo -e "${YELLOW}GET ${BASE_URL}${HEALTH_ENDPOINT}${NC}"
echo ""

response=$(curl -s -w "\n%{http_code}" "${BASE_URL}${HEALTH_ENDPOINT}")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

echo "Response:"
echo "$body" | jq '.' 2>/dev/null || echo "$body"
echo ""
echo "Status Code: $http_code"

if [ "$http_code" -eq 200 ]; then
    echo -e "${GREEN}✓ Health check passed${NC}"
else
    echo -e "${RED}✗ Health check failed${NC}"
fi

echo ""
echo "=================================================="
