# Complete Testing Guide - Split Group Microservices

## Table of Contents
1. [Testing Tools](#testing-tools)
2. [Service Health Checks](#service-health-checks)
3. [Individual Service Testing](#individual-service-testing)
4. [Integration Testing](#integration-testing)
5. [End-to-End Scenarios](#end-to-end-scenarios)
6. [Database Testing](#database-testing)
7. [Performance Testing](#performance-testing)
8. [Error Testing](#error-testing)
9. [Security Testing](#security-testing)
10. [Troubleshooting Guide](#troubleshooting-guide)

---

## Architecture Overview

### Microservices Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Eureka Server │    │   API Gateway   │    │   Auth Service  │
│     :8761       │    │     :8080       │    │     :8081       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
        │  User Service   │ │Transaction Svc  │ │ Balance Service │
        │     :8082       │ │     :8083       │ │     :8084       │
        └─────────────────┘ └─────────────────┘ └─────────────────┘
```

### Service Responsibilities
- **Eureka Server**: Service discovery and registration
- **API Gateway**: Single entry point, request routing, load balancing
- **Auth Service**: User authentication, JWT token management
- **User Service**: Extended user profile management
- **Transaction Service**: Expense tracking and bill splitting
- **Balance Service**: Balance calculations and settlements

---

## Prerequisites

### Environment Setup
- **Java 17** installed
- **MySQL** running on port 3306
- **Maven** for building services
- **IntelliJ IDEA** or similar IDE
- **Postman** or **Swagger UI** for API testing

### Required Databases
```sql
CREATE DATABASE IF NOT EXISTS split_auth_db;
CREATE DATABASE IF NOT EXISTS split_user_db;
CREATE DATABASE IF NOT EXISTS split_transaction_db;
CREATE DATABASE IF NOT EXISTS split_balance_db;
```

### Service Start Order
```bash
1. eureka-server (port 8761)
2. api-gateway (port 8080)
3. auth-service (port 8081)
4. user-service (port 8082)
5. transaction-service (port 8083)
6. balance-service (port 8084)
```

---

## Testing Tools

### 1. Swagger UI (Recommended)
- **Auth Service**: http://localhost:8081/swagger-ui.html
- **User Service**: http://localhost:8082/swagger-ui.html
- **Transaction Service**: http://localhost:8083/swagger-ui.html
- **Balance Service**: http://localhost:8084/swagger-ui.html

### 2. Postman
- Import API collections
- Environment variables for tokens
- Automated testing scripts

### 3. curl Commands
- Quick endpoint testing
- CI/CD integration
- Scripted testing

### 4. Browser
- Direct endpoint access
- Eureka dashboard monitoring
- Health check verification

---

## Service Health Checks

### Quick Health Verification
```bash
# Check all services are running
curl http://localhost:8761/  # Eureka Dashboard
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/health  # Auth Service
curl http://localhost:8082/health  # User Service
curl http://localhost:8083/health  # Transaction Service
curl http://localhost:8084/health  # Balance Service
```

### Eureka Registration Check
1. Open http://localhost:8761
2. Verify all services are registered:
    - API-GATEWAY
    - AUTH-SERVICE
    - USER-SERVICE
    - TRANSACTION-SERVICE
    - BALANCE-SERVICE

### Expected Health Responses
```json
// API Gateway
{"status":"UP"}

// Auth Service
"Auth Service is healthy! Total users: X"

// User Service
"User Service is healthy! Total profiles: X"

// Transaction Service
"Transaction Service is healthy! Total transactions: X"

// Balance Service
"Balance Service is healthy! Active balances: X"
```

---

## Individual Service Testing

### Auth Service Testing

#### 1. User Registration
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "karthii.k@test.com",
  "password": "password123",
  "name": "karthii k"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "karthii.k@test.com",
  "name": "karthii k",
  "userId": 1,
  "message": "Authentication successful"
}
```

#### 2. User Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "karthii.k@test.com",
  "password": "password123"
}
```

#### 3. Token Validation
```bash
POST http://localhost:8080/api/auth/validate
Content-Type: application/json

"YOUR_JWT_TOKEN_HERE"
```

#### 4. Get User Information
```bash
GET http://localhost:8080/api/auth/users/1
```

### User Service Testing

#### 1. Create User Profile
```bash
POST http://localhost:8080/api/users/profiles
Content-Type: application/json

{
  "userId": 1,
  "email": "karthii.k@test.com",
  "name": "karthii k"
}
```

#### 2. Get User Profile
```bash
GET http://localhost:8080/api/users/profiles/1
```

#### 3. Update User Profile
```bash
PUT http://localhost:8080/api/users/profiles/1
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "phone": "+1-555-0123",
  "bio": "Love splitting bills with friends!",
  "location": "New York, NY"
}
```

#### 4. Search Users
```bash
GET http://localhost:8080/api/users/search/email?q=karthii
GET http://localhost:8080/api/users/search/name?q=doe
```

### Transaction Service Testing

#### 1. Create Equal Split Transaction
```bash
POST http://localhost:8080/api/transactions/
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "paidBy": 1,
  "totalAmount": 60.00,
  "description": "Dinner at Italian Restaurant",
  "category": "Food",
  "splitType": "EQUAL",
  "participants": [
    {"userId": 1},
    {"userId": 2}
  ]
}
```

#### 2. Create Exact Split Transaction
```bash
POST http://localhost:8080/api/transactions/
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "paidBy": 2,
  "totalAmount": 100.00,
  "description": "Grocery Shopping",
  "category": "Groceries",
  "splitType": "EXACT",
  "participants": [
    {"userId": 1, "amount": 40.00},
    {"userId": 2, "amount": 60.00}
  ]
}
```

#### 3. Get User Transactions
```bash
GET http://localhost:8080/api/transactions/user/1
```

#### 4. Get Transactions Between Users
```bash
GET http://localhost:8080/api/transactions/between/1/2
```

#### 5. Search Transactions
```bash
GET http://localhost:8080/api/transactions/search?q=dinner
GET http://localhost:8080/api/transactions/category/Food
```

### Balance Service Testing

#### 1. Get Balance Between Users
```bash
GET http://localhost:8080/api/balances/1/2
```

#### 2. Get User's All Balances
```bash
GET http://localhost:8080/api/balances/user/1
```

#### 3. Create Settlement
```bash
POST http://localhost:8080/api/balances/settle
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "payerId": 1,
  "payeeId": 2,
  "amount": 10.00,
  "description": "Settling dinner expenses",
  "method": "VENMO"
}
```

#### 4. Get Balance Summary
```bash
GET http://localhost:8080/api/balances/summary/1
```

#### 5. Optimize Group Balances
```bash
POST http://localhost:8080/api/balances/optimize
Content-Type: application/json

{
  "userIds": [1, 2, 3]
}
```

---

## Integration Testing

### Cross-Service Communication Tests

#### 1. Transaction → Balance Integration
**Test Flow:**
1. Create transaction in Transaction Service
2. Verify balance automatically updated in Balance Service
3. Check balance calculations are correct

```bash
# Step 1: Create transaction
POST http://localhost:8080/api/transactions/
{
  "paidBy": 1,
  "totalAmount": 50.00,
  "description": "Lunch",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}]
}

# Step 2: Check balance (should be ₹25 owed)
GET http://localhost:8080/api/balances/1/2
```

#### 2. Auth → User Integration
**Test Flow:**
1. Register user in Auth Service
2. Create corresponding profile in User Service
3. Verify user information consistency

#### 3. Settlement → Balance Integration
**Test Flow:**
1. Create settlement in Balance Service
2. Verify balance reduced correctly
3. Check settlement history recorded

### Service Discovery Testing

#### 1. Service Registration
```bash
# Check all services registered with Eureka
curl http://localhost:8761/eureka/apps
```

#### 2. Load Balancing (if multiple instances)
```bash
# Test requests distributed across instances
for i in {1..10}; do
  curl http://localhost:8080/api/auth/health
done
```

#### 3. Service Failure Scenarios
```bash
# Stop one service and verify others continue working
# Test graceful degradation
```

---

## End-to-End Scenarios

### Scenario 1: Complete User Journey

#### Step 1: User Registration and Setup
```bash
# Register User 1
POST http://localhost:8080/api/auth/register
{
  "email": "karthii@k.com",
  "password": "password123",
  "name": "k karthii"
}

# Register User 2
POST http://localhost:8080/api/auth/register
{
  "email": "pranay@k.com",
  "password": "password123",
  "name": "pranay k"
}

# Create profiles
POST http://localhost:8080/api/users/profiles
{
  "userId": 1,
  "email": "karthii@test.com",
  "name": "k karthiison"
}
```

#### Step 2: Create Expenses
```bash
# karthii pays for dinner
POST http://localhost:8080/api/transactions/
Authorization: Bearer karthii_TOKEN
{
  "paidBy": 1,
  "totalAmount": 80.00,
  "description": "Dinner at Sushi Place",
  "category": "Food",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}]
}

# pranay pays for groceries
POST http://localhost:8080/api/transactions/
Authorization: Bearer pranay_TOKEN
{
  "paidBy": 2,
  "totalAmount": 120.00,
  "description": "Weekly Groceries",
  "category": "Groceries",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}]
}
```

#### Step 3: Check Balances
```bash
# Check who owes whom
GET http://localhost:8080/api/balances/1/2
# Expected: pranay owes karthii ₹20 (karthii paid ₹80, owes ₹40; pranay paid ₹120, owes ₹40; Net: pranay owes ₹20)

# Get summary for karthii
GET http://localhost:8080/api/balances/summary/1
```

#### Step 4: Settle Debts
```bash
# pranay pays karthii ₹20
POST http://localhost:8080/api/balances/settle
Authorization: Bearer pranay_TOKEN
{
  "payerId": 2,
  "payeeId": 1,
  "amount": 20.00,
  "description": "Settling our expenses",
  "method": "VENMO"
}
```

#### Step 5: Verify Settlement
```bash
# Check balance is now zero
GET http://localhost:8080/api/balances/1/2
# Expected: isSettled = true, amount = 0.00

# Check settlement history
GET http://localhost:8080/api/balances/settlements/1/2
```

### Scenario 2: Group Trip Expenses

#### Setup: 3 Users for a Trip
```bash
# Register User 3
POST http://localhost:8080/api/auth/register
{
  "email": "adi@k.com",
  "password": "password123",
  "name": "adi k"
}
```

#### Create Group Expenses
```bash
# Hotel booking (karthii pays)
POST http://localhost:8080/api/transactions/
{
  "paidBy": 1,
  "totalAmount": 300.00,
  "description": "Hotel for 3 nights",
  "category": "Accommodation",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}, {"userId": 3}]
}

# Car rental (pranay pays)
POST http://localhost:8080/api/transactions/
{
  "paidBy": 2,
  "totalAmount": 150.00,
  "description": "Car rental",
  "category": "Transportation",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}, {"userId": 3}]
}

# Dinner (adi pays)
POST http://localhost:8080/api/transactions/
{
  "paidBy": 3,
  "totalAmount": 90.00,
  "description": "Group dinner",
  "category": "Food",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}, {"userId": 3}]
}
```

#### Optimize Payments
```bash
# Get optimized payment suggestions
POST http://localhost:8080/api/balances/optimize
{
  "userIds": [1, 2, 3]
}
```

**Expected Optimization Result:**
- Instead of multiple payments, suggests minimal transactions
- Shows who should pay whom to settle all debts efficiently

---

## Database Testing

### Data Verification Queries

#### Auth Database
```sql
USE split_auth_db;
SELECT id, email, name, created_at FROM users ORDER BY created_at DESC;
```

#### User Database
```sql
USE split_user_db;
SELECT user_id, email, name, phone, bio, profile_completed FROM user_profiles;
```

#### Transaction Database
```sql
USE split_transaction_db;
SELECT id, paid_by, owed_by, amount, description, category, status, created_at 
FROM transactions ORDER BY created_at DESC;
```

#### Balance Database
```sql
USE split_balance_db;
SELECT balance_id, user1, user2, amount, transaction_count, last_updated 
FROM balances WHERE ABS(amount) > 0.01;

SELECT id, payer_id, payee_id, amount, description, method, status 
FROM settlements ORDER BY settlement_date DESC;
```

### Data Consistency Checks

#### 1. User Consistency
```sql
-- Check users exist in both auth and user services
SELECT a.id, a.email, a.name, 
       CASE WHEN u.user_id IS NOT NULL THEN 'YES' ELSE 'NO' END as has_profile
FROM split_auth_db.users a
LEFT JOIN split_user_db.user_profiles u ON a.id = u.user_id;
```

#### 2. Transaction-Balance Consistency
```sql
-- Verify balance calculations match transaction history
-- This is a complex query that should be run as a data integrity check
```

#### 3. Settlement-Balance Consistency
```sql
-- Check settlements properly reduced balances
SELECT s.id, s.payer_id, s.payee_id, s.amount,
       b.balance_id, b.amount as current_balance
FROM split_balance_db.settlements s
JOIN split_balance_db.balances b ON s.balance_id = b.balance_id
WHERE s.status = 'COMPLETED';
```

---

## Performance Testing

### Load Testing Scenarios

#### 1. Concurrent User Registration
```bash
# Test multiple simultaneous registrations
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"user${i}@test.com\",\"password\":\"password123\",\"name\":\"User ${i}\"}" &
done
wait
```

#### 2. Transaction Volume Testing
```bash
# Create multiple transactions simultaneously
# Test database performance under load
# Monitor response times
```

#### 3. Balance Calculation Performance
```bash
# Test complex balance optimizations with many users
# Measure optimization algorithm performance
# Test with groups of 10, 20, 50 users
```

### Performance Metrics to Monitor

#### Response Times
- **Auth Service**: < 500ms for login/register
- **Transaction Service**: < 1s for transaction creation
- **Balance Service**: < 2s for optimization (complex calculations)

#### Database Performance
- **Connection pool usage**
- **Query execution times**
- **Index effectiveness**

#### Memory Usage
- **JVM heap usage per service**
- **Garbage collection frequency**
- **Memory leaks detection**

---

## Error Testing

### Input Validation Tests

#### 1. Invalid User Registration
```bash
# Missing required fields
POST http://localhost:8080/api/auth/register
{
  "email": "",
  "password": "123",
  "name": ""
}
# Expected: 400 Bad Request with validation errors

# Invalid email format
POST http://localhost:8080/api/auth/register
{
  "email": "invalid-email",
  "password": "password123",
  "name": "Test User"
}
# Expected: 400 Bad Request
```

#### 2. Invalid Transaction Data
```bash
# Negative amount
POST http://localhost:8080/api/transactions/
{
  "paidBy": 1,
  "totalAmount": -50.00,
  "description": "Invalid transaction"
}
# Expected: 400 Bad Request

# Empty participants
POST http://localhost:8080/api/transactions/
{
  "paidBy": 1,
  "totalAmount": 50.00,
  "description": "No participants",
  "participants": []
}
# Expected: 400 Bad Request
```

#### 3. Invalid Settlement Data
```bash
# Settlement amount exceeds balance
POST http://localhost:8080/api/balances/settle
{
  "payerId": 1,
  "payeeId": 2,
  "amount": 1000.00,
  "description": "Overpayment"
}
# Expected: 400 Bad Request
```

### Authentication/Authorization Tests

#### 1. Missing JWT Token
```bash
PUT http://localhost:8080/api/users/profiles/1
Content-Type: application/json
# No Authorization header
{
  "name": "Updated Name"
}
# Expected: 401 Unauthorized
```

#### 2. Invalid JWT Token
```bash
PUT http://localhost:8080/api/users/profiles/1
Authorization: Bearer invalid-token
# Expected: 401 Unauthorized
```

#### 3. Expired JWT Token
```bash
# Use token that's older than 24 hours
# Expected: 401 Unauthorized
```

### Service Failure Scenarios

#### 1. Database Connection Failures
```bash
# Stop MySQL and test graceful error handling
# Expected: 500 Internal Server Error with appropriate message
```

#### 2. Service Unavailability
```bash
# Stop Auth Service and test other services
# Expected: Services should handle Auth Service unavailability gracefully
```

#### 3. Network Timeouts
```bash
# Simulate slow network conditions
# Test timeout handling and retry mechanisms
```

---

## Security Testing

### Authentication Security

#### 1. JWT Token Security
```bash
# Test token manipulation
# Verify token signature validation
# Test token expiration handling
```

#### 2. Password Security
```bash
# Verify passwords are encrypted in database
SELECT id, email, password FROM split_auth_db.users;
# Password field should be hashed, not plain text
```

#### 3. SQL Injection Prevention
```bash
# Test with malicious input
GET http://localhost:8080/api/users/search/email?q='; DROP TABLE users; --
# Expected: Query should be safely parameterized
```

### Authorization Security

#### 1. User Data Access Control
```bash
# Test user can only access their own data
GET http://localhost:8080/api/users/profiles/999
Authorization: Bearer USER_1_TOKEN
# Expected: Should not access other user's data
```

#### 2. Transaction Security
```bash
# Test user can only modify their own transactions
DELETE http://localhost:8080/api/transactions/1
Authorization: Bearer WRONG_USER_TOKEN
# Expected: 403 Forbidden
```

### Data Privacy

#### 1. Sensitive Data Exposure
```bash
# Verify passwords not returned in API responses
GET http://localhost:8080/api/auth/users/1
# Response should not contain password field
```

#### 2. Error Message Information Disclosure
```bash
# Verify error messages don't leak sensitive information
# Test with various invalid inputs
```

---

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Service Won't Start

**Symptoms:**
- Service fails to start
- Port already in use errors
- Database connection failures

**Solutions:**
```bash
# Check port availability
netstat -an | grep :808[1-4]

# Check database connection
mysql -u root -p -e "SHOW DATABASES;"

# Check Java version
java -version

# Clear Maven cache
rm -rf ~/.m2/repository
```

#### 2. Service Not Registering with Eureka

**Symptoms:**
- Service starts but not visible in Eureka dashboard
- Service discovery failures

**Solutions:**
```bash
# Check Eureka server is running
curl http://localhost:8761

# Verify eureka client configuration in application.yml
# Check network connectivity
# Restart services in correct order
```

#### 3. JWT Authentication Failures

**Symptoms:**
- 401 Unauthorized errors
- Token validation failures

**Solutions:**
```bash
# Verify token format (must include "Bearer " prefix)
# Check token hasn't expired (24 hour limit)
# Verify JWT secret consistency across services
# Check system clock synchronization
```

#### 4. Database Connection Issues

**Symptoms:**
- Connection refused errors
- Table not found errors
- Data not persisting

**Solutions:**
```bash
# Check MySQL is running
sudo systemctl status mysql

# Verify database exists
mysql -u root -p -e "SHOW DATABASES;"

# Check connection parameters in application.yml
# Verify user permissions
GRANT ALL PRIVILEGES ON split_*_db.* TO 'root'@'localhost';
```

#### 5. API Gateway Routing Issues

**Symptoms:**
- 404 errors when accessing via gateway
- Requests not reaching target services

**Solutions:**
```bash
# Check service registration in Eureka
# Verify gateway routing configuration
# Test direct service access first
# Check load balancer configuration
```

### Debug Commands

#### Service Logs
```bash
# Check service logs for errors
tail -f logs/spring.log

# Enable debug logging
# Add to application.yml:
logging:
  level:
    root: DEBUG
```

#### Network Testing
```bash
# Test service connectivity
curl -v http://localhost:8081/health
curl -v http://localhost:8080/api/auth/health

# Check service discovery
curl http://localhost:8761/eureka/apps
```

#### Database Debugging
```sql
-- Check table structures
DESCRIBE split_auth_db.users;
DESCRIBE split_transaction_db.transactions;
DESCRIBE split_balance_db.balances;

-- Check data consistency
SELECT COUNT(*) FROM split_auth_db.users;
SELECT COUNT(*) FROM split_user_db.user_profiles;
```

### Performance Debugging

#### Memory Issues
```bash
# Check JVM memory usage
jmap -histo PID

# Generate heap dump
jmap -dump:format=b,file=heapdump.hprof PID
```

#### Database Performance
```sql
-- Check slow queries
SHOW PROCESSLIST;

-- Analyze query performance
EXPLAIN SELECT * FROM transactions WHERE user_id = 1;
```

---

## Testing Checklist

### Pre-Testing Setup
- [ ] All databases created and accessible
- [ ] All services start without errors
- [ ] All services register with Eureka
- [ ] Swagger UI accessible for all services
- [ ] Test data prepared

### Core Functionality Testing
- [ ] User registration and login
- [ ] JWT token generation and validation
- [ ] User profile creation and updates
- [ ] Transaction creation (all split types)
- [ ] Balance calculations
- [ ] Settlement processing
- [ ] Balance optimization

### Integration Testing
- [ ] Cross-service communication working
- [ ] Data consistency across services
- [ ] Service discovery functioning
- [ ] API Gateway routing correctly

### Error Handling Testing
- [ ] Input validation working
- [ ] Authentication errors handled properly
- [ ] Database errors handled gracefully
- [ ] Service unavailability handled

### Performance Testing
- [ ] Response times within acceptable limits
- [ ] Concurrent user handling
- [ ] Database performance under load
- [ ] Memory usage stable

### Security Testing
- [ ] Authentication required for protected endpoints
- [ ] Authorization preventing unauthorized access
- [ ] Sensitive data protected
- [ ] SQL injection prevented

---

## Conclusion

This comprehensive testing guide covers all aspects of testing your Split Group microservices architecture. Regular testing using these scenarios will ensure:

- **Reliability**: All services work correctly individually and together
- **Security**: User data and operations are properly protected
- **Performance**: System handles expected load efficiently
- **Maintainability**: Issues can be quickly identified and resolved

Remember to run these tests regularly, especially after making changes to any service. Automated testing pipelines can help ensure continuous quality assurance.

For any issues not covered in this guide, check the troubleshooting section or refer to the individual service documentation and logs.