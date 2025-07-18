# Split Group Microservices Architecture
## Complete System Documentation

### Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Design](#architecture-design)
3. [Service Details](#service-details)
4. [Data Flow Diagrams](#data-flow-diagrams)
5. [Inter-Service Communication](#inter-service-communication)
6. [API Gateway Routing](#api-gateway-routing)
7. [Authentication Flow](#authentication-flow)
8. [Business Logic Flows](#business-logic-flows)
9. [Database Design](#database-design)
10. [Deployment Architecture](#deployment-architecture)
11. [Technology Stack](#technology-stack)
12. [API Documentation](#api-documentation)

---

## System Overview

Split Group is a **microservices-based expense sharing application** similar to Splitwise. It allows users to track shared expenses, split bills among friends, and settle debts efficiently.

### Core Functionality
- **User Management**: Registration, authentication, and profile management
- **Expense Tracking**: Record transactions and split bills multiple ways
- **Balance Calculation**: Automatic calculation of who owes whom
- **Settlement Processing**: Record payments to settle debts
- **Balance Optimization**: Minimize transactions needed to settle all debts

### Key Benefits
- **Scalable Architecture**: Each service can be scaled independently
- **Fault Isolation**: Failure in one service doesn't affect others
- **Technology Diversity**: Different services can use different technologies
- **Team Independence**: Teams can work on different services independently
- **Maintainability**: Smaller, focused codebases are easier to maintain

---

## Architecture Design

### High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      Mobile     │    │       Web       │    │  External API   │
│       App       │    │     Browser     │    │   Consumers     │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │      API Gateway         │
                    │     (Port 8080)          │
                    │   - Request Routing      │
                    │   - Load Balancing       │
                    │   - CORS Handling        │
                    └────────────┬─────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Auth Service   │    │  User Service   │    │Transaction Svc  │
│   (Port 8081)   │    │   (Port 8082)   │    │   (Port 8083)   │
│                 │    │                 │    │                 │
│ - User Reg/Login│    │ - User Profiles │    │ - Expense Track │
│ - JWT Tokens    │    │ - Profile Mgmt  │    │ - Bill Splitting│
│ - Authentication│    │ - User Search   │    │ - Transaction   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│split_auth_db    │    │split_user_db    │    │split_trans_db   │
│   (MySQL)       │    │   (MySQL)       │    │   (MySQL)       │
└─────────────────┘    └─────────────────┘    └─────────────────┘

                    ┌─────────────────┐
                    │ Balance Service │
                    │   (Port 8084)   │
                    │                 │
                    │ - Balance Calc  │
                    │ - Settlements   │
                    │ - Optimization  │
                    └─────────┬───────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │split_balance_db │
                    │   (MySQL)       │
                    └─────────────────┘

                ┌─────────────────────────────┐
                │     Eureka Server           │
                │      (Port 8761)            │
                │                             │
                │    Service Discovery        │
                │  - Service Registration     │
                │  - Health Monitoring        │
                │  - Load Balancing Info      │
                └─────────────────────────────┘
```

### Service Communication Pattern

```
Client Request → API Gateway → Target Service → Database
                     ↓
                Service Discovery (Eureka)
                     ↓
            Auth Service (for JWT validation)
```

---

## Service Details

### 1. Eureka Server (Service Discovery)
**Port**: 8761  
**Purpose**: Central registry for all microservices

**Responsibilities**:
- Service registration and deregistration
- Health check monitoring
- Service discovery for inter-service communication
- Load balancing information

**Key Features**:
- Dashboard at `http://localhost:8761`
- Automatic service health monitoring
- Client-side load balancing support

### 2. API Gateway
**Port**: 8080  
**Purpose**: Single entry point for all client requests

**Responsibilities**:
- Request routing to appropriate services
- Cross-cutting concerns (CORS, rate limiting)
- Request/response transformation
- Load balancing across service instances

**Routing Rules**:
```yaml
/api/auth/**      → Auth Service (8081)
/api/users/**     → User Service (8082)
/api/transactions/** → Transaction Service (8083)
/api/balances/**  → Balance Service (8084)
```

### 3. Auth Service
**Port**: 8081  
**Database**: split_auth_db  
**Purpose**: User authentication and authorization

**Core Features**:
- User registration with email validation
- Password encryption using BCrypt
- JWT token generation and validation
- User login with credentials
- Token-based authentication

**Database Schema**:
```sql
users (
  id BIGINT PRIMARY KEY,
  email VARCHAR(255) UNIQUE,
  password VARCHAR(255), -- BCrypt hashed
  name VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)
```

**Key Endpoints**:
- `POST /register` - User registration
- `POST /login` - User authentication
- `POST /validate` - Token validation (for other services)
- `GET /users/{id}` - Get user information

### 4. User Service
**Port**: 8082  
**Database**: split_user_db  
**Purpose**: Extended user profile management

**Core Features**:
- Extended user profiles beyond basic auth
- Profile picture management
- User preferences and settings
- User search and discovery
- Privacy level management

**Database Schema**:
```sql
user_profiles (
  user_id BIGINT PRIMARY KEY, -- Same as auth service user ID
  email VARCHAR(255),
  name VARCHAR(255),
  phone VARCHAR(15),
  bio TEXT,
  location VARCHAR(100),
  profile_picture_url VARCHAR(500),
  date_of_birth DATETIME,
  notification_enabled BOOLEAN,
  email_notifications BOOLEAN,
  privacy_level ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE'),
  profile_completed BOOLEAN,
  last_active TIMESTAMP,
  created_at TIMESTAMP,
  updated_at TIMESTAMP
)
```

**Key Endpoints**:
- `POST /profiles` - Create user profile
- `GET /profiles/{userId}` - Get user profile
- `PUT /profiles/{userId}` - Update user profile
- `GET /search/email?q={email}` - Search users by email
- `GET /search/name?q={name}` - Search users by name

### 5. Transaction Service
**Port**: 8083  
**Database**: split_transaction_db  
**Purpose**: Expense tracking and bill splitting

**Core Features**:
- Record expenses between users
- Multiple split types (equal, exact, percentage)
- Transaction history and categorization
- Balance calculation integration
- Transaction status management

**Split Types**:
- **Equal**: Split amount equally among all participants
- **Exact**: Specify exact amount for each participant
- **Percentage**: Split based on percentages

**Database Schema**:
```sql
transactions (
  id BIGINT PRIMARY KEY,
  paid_by BIGINT, -- User who paid
  owed_by BIGINT, -- User who owes
  amount DECIMAL(10,2),
  description VARCHAR(500),
  category VARCHAR(100),
  total_amount DECIMAL(10,2),
  transaction_date DATETIME,
  created_by BIGINT,
  group_id VARCHAR(50),
  status ENUM('ACTIVE', 'CANCELLED', 'SETTLED'),
  split_type ENUM('EQUAL', 'EXACT', 'PERCENTAGE'),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  notes TEXT
)
```

**Key Endpoints**:
- `POST /` - Create new transaction
- `GET /{transactionId}` - Get transaction details
- `GET /user/{userId}` - Get user's transactions
- `GET /between/{userId1}/{userId2}` - Transactions between two users
- `GET /summary/{userId}` - Transaction summary

### 6. Balance Service
**Port**: 8084  
**Database**: split_balance_db  
**Purpose**: Balance calculation and settlement management

**Core Features**:
- Real-time balance calculations
- Settlement processing
- Balance optimization algorithms
- Debt tracking and history
- Group balance optimization

**Database Schema**:
```sql
balances (
  balance_id VARCHAR(50) PRIMARY KEY, -- Format: "userId1_userId2"
  user1 BIGINT,
  user2 BIGINT,
  amount DECIMAL(10,2), -- Positive: user1 owes user2
  last_updated TIMESTAMP,
  created_at TIMESTAMP,
  transaction_count BIGINT,
  last_transaction_id BIGINT
);

settlements (
  id BIGINT PRIMARY KEY,
  payer_id BIGINT,
  payee_id BIGINT,
  amount DECIMAL(10,2),
  description VARCHAR(500),
  balance_id VARCHAR(50),
  settlement_date DATETIME,
  created_by BIGINT,
  method ENUM('CASH', 'BANK_TRANSFER', 'Phonepay', 'PAYPAL', 'UPI', 'OTHER'),
  status ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED'),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  notes TEXT,
  reference_id VARCHAR(100)
)
```

**Key Endpoints**:
- `POST /update` - Update balance (called by Transaction Service)
- `GET /{userId1}/{userId2}` - Get balance between users
- `POST /settle` - Create settlement
- `GET /summary/{userId}` - Balance summary
- `POST /optimize` - Optimize group balances

---

## Data Flow Diagrams

### 1. User Registration Flow

```
Client → API Gateway → Auth Service
  │                        │
  │         Register       │
  │ ──────────────────────→│
  │                        │ Create User
  │                        │──────────→ MySQL
  │                        │           (split_auth_db)
  │                        │←──────────
  │                        │ Generate JWT
  │    JWT Token           │
  │←──────────────────────┘
```

### 2. Create Transaction Flow

```
Client → API Gateway → Transaction Service
  │                           │
  │    Create Transaction     │
  │─────────────────────────→ │
  │                           │ Validate JWT
  │                           │────────────→ Auth Service
  │                           │←────────────
  │                           │ Save Transaction
  │                           │─────────────→ MySQL
  │                           │              (split_trans_db)
  │                           │←─────────────
  │                           │ Update Balance
  │                           │─────────────→ Balance Service
  │                           │←─────────────
  │    Transaction Details    │
  │←─────────────────────────┘
```

### 3. Balance Calculation Flow

```
Transaction Service → Balance Service
         │                   │
         │ Update Balance    │
         │──────────────────→│
         │                   │ Find/Create Balance
         │                   │─────────────→ MySQL
         │                   │              (split_balance_db)
         │                   │←─────────────
         │                   │ Calculate New Amount
         │                   │ Save Updated Balance
         │                   │─────────────→ MySQL
         │                   │←─────────────
         │   Success         │
         │←──────────────────┘
```

### 4. Settlement Processing Flow

```
Client → API Gateway → Balance Service
  │                         │
  │   Create Settlement     │
  │───────────────────────→ │
  │                         │ Validate Settlement
  │                         │ (amount <= outstanding)
  │                         │ Create Settlement Record
  │                         │─────────────→ MySQL
  │                         │←─────────────
  │                         │ Update Balance
  │                         │─────────────→ MySQL
  │                         │←─────────────
  │   Settlement Details    │
  │←───────────────────────┘
```

---

## Inter-Service Communication

### Communication Patterns

#### 1. Synchronous HTTP Communication
- **RestTemplate** with service discovery
- **Load balancing** via Eureka
- **Circuit breaker** pattern for fault tolerance

#### 2. Service Discovery
```java
// Example: Transaction Service calling Auth Service
@Autowired
private RestTemplate restTemplate; // Load-balanced

// Call using service name (resolved by Eureka)
String url = "http://auth-service/validate";
AuthResponse response = restTemplate.postForObject(url, token, AuthResponse.class);
```

#### 3. JWT Token Validation
```java
// Common pattern across all services
public Long extractUserIdFromToken(String authHeader) {
    String token = authHeader.substring(7); // Remove "Bearer "
    AuthResponse response = authClientService.validateToken(token);
    return response.getUserId();
}
```

### Service Dependencies

```
┌─────────────────┐
│  Auth Service   │ ← Base service (no dependencies)
└─────────────────┘

┌─────────────────┐    ┌─────────────────┐
│  User Service   │───→│  Auth Service   │ (JWT validation)
└─────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐
│Transaction Svc  │───→│  Auth Service   │ (JWT validation)
│                 │───→│ Balance Service │ (balance updates)
└─────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐
│ Balance Service │───→│  Auth Service   │ (JWT validation)
└─────────────────┘    └─────────────────┘
```

---

## API Gateway Routing

### Routing Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service          # Load-balanced via Eureka
          predicates:
            - Path=/api/auth/**           # Match path pattern
          filters:
            - StripPrefix=2               # Remove /api/auth from forwarded request

        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2

        - id: transaction-service
          uri: lb://transaction-service
          predicates:
            - Path=/api/transactions/**
          filters:
            - StripPrefix=2

        - id: balance-service
          uri: lb://balance-service
          predicates:
            - Path=/api/balances/**
          filters:
            - StripPrefix=2
```

### Request Transformation

| Client Request | Gateway Processing | Service Receives |
|----------------|-------------------|------------------|
| `POST /api/auth/register` | Remove `/api/auth` | `POST /register` |
| `GET /api/users/profiles/1` | Remove `/api/users` | `GET /profiles/1` |
| `POST /api/transactions/` | Remove `/api/transactions` | `POST /` |
| `GET /api/balances/1/2` | Remove `/api/balances` | `GET /1/2` |

### Load Balancing

- **Client-side load balancing** using Spring Cloud LoadBalancer
- **Service instances** discovered via Eureka
- **Health checks** ensure requests only go to healthy instances
- **Failover** automatic switching to healthy instances

---

## Authentication Flow

### JWT Token Lifecycle

```
1. User Registration/Login
   ┌─────────────────┐    ┌─────────────────┐
   │     Client      │───→│  Auth Service   │
   │                 │    │  - Validate     │
   │                 │    │  - Generate JWT │
   │                 │←───│  - Return Token │
   └─────────────────┘    └─────────────────┘

2. Authenticated Request
   ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
   │     Client      │───→│  Any Service    │───→│  Auth Service   │
   │ Bearer JWT      │    │  - Extract JWT  │    │  - Validate JWT │
   │                 │    │  - Call Auth    │    │  - Return User  │
   │                 │←───│  - Process Req  │←───│    Info         │
   └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS512",
    "typ": "JWT"
  },
  "payload": {
    "sub": "karthik@k.com",
    "userId": 123,
    "name": "karthik k",
    "iat": 1609459200,
    "exp": 1609545600
  }
}
```

### Security Implementation

```java
// JWT Generation (Auth Service)
public String generateToken(String email, Long userId, String name) {
    return Jwts.builder()
            .setSubject(email)
            .claim("userId", userId)
            .claim("name", name)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
}

// JWT Validation (All Services)
public AuthResponse validateToken(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    
    return new AuthResponse(
        token,
        claims.getSubject(),
        claims.get("name", String.class),
        claims.get("userId", Long.class)
    );
}
```

---

## Business Logic Flows

### 1. Expense Splitting Scenario

**Scenario**: karthii pays ₹60 for dinner, split equally between karthii, adi, and pranay

```
Step 1: Create Transaction
POST /api/transactions/
{
  "paidBy": 1,           // karthii's ID
  "totalAmount": 60.00,
  "description": "Dinner at Italian Restaurant",
  "splitType": "EQUAL",
  "participants": [
    {"userId": 1},       // karthii
    {"userId": 2},       // adi  
    {"userId": 3}        // pranay
  ]
}

Step 2: Transaction Service Processing
- Calculate equal split: ₹60 ÷ 3 = ₹20 per person
- Create 3 transaction records:
  1. karthii paid ₹60, owes ₹20 (net: +₹40)
  2. adi owes karthii ₹20 (net: -₹20)
  3. pranay owes karthii ₹20 (net: -₹20)

Step 3: Balance Updates (Automatic)
Transaction Service → Balance Service
- Update balance karthii-adi: adi owes karthii ₹20
- Update balance karthii-pranay: pranay owes karthii ₹20

Step 4: Final State
- karthii is owed ₹40 total (₹20 from adi + ₹20 from pranay)
- adi owes ₹20 to karthii
- pranay owes ₹20 to karthii
```

### 2. Balance Optimization Scenario

**Scenario**: Group with circular debt

```
Initial State:
- adi owes pranay ₹30
- pranay owes Charlie ₹30  
- Charlie owes adi ₹30

Traditional Settlement (3 transactions):
1. adi pays pranay ₹30
2. pranay pays Charlie ₹30
3. Charlie pays adi ₹30

Optimized Settlement (0 transactions):
- Net result: Everyone is settled
- No money needs to change hands

POST /api/balances/optimize
{
  "userIds": [1, 2, 3]  // adi, pranay, Charlie
}

Response:
{
  "suggestedPayments": [],
  "optimizationSummary": "All users are already settled"
}
```

### 3. Settlement Processing

**Scenario**: adi pays pranay ₹20 via Phonepay

```
Step 1: Create Settlement
POST /api/balances/settle
{
  "payerId": 1,         // adi
  "payeeId": 2,         // pranay
  "amount": 20.00,
  "description": "Dinner settlement",
  "method": "Phonepay",
  "referenceId": "Phonepay-12345"
}

Step 2: Balance Service Processing
- Validate: adi owes pranay at least ₹20 ✓
- Create settlement record
- Update balance: reduce adi's debt by ₹20
- If balance becomes ≤ ₹0.01, mark as settled

Step 3: Final State
- Settlement recorded with Phonepay reference
- Balance updated automatically
- Both users can see settlement history
```

---

## Database Design

### 1. Auth Service Database (split_auth_db)

```sql
-- Core user authentication data
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
);
```

### 2. User Service Database (split_user_db)

```sql
-- Extended user profile information
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,  -- Foreign key to auth service
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15),
    bio TEXT,
    location VARCHAR(100),
    profile_picture_url VARCHAR(500),
    date_of_birth DATE,
    notification_enabled BOOLEAN DEFAULT TRUE,
    email_notifications BOOLEAN DEFAULT TRUE,
    privacy_level ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE') DEFAULT 'PUBLIC',
    profile_completed BOOLEAN DEFAULT FALSE,
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_email (email),
    INDEX idx_location (location),
    INDEX idx_privacy_level (privacy_level)
);
```

### 3. Transaction Service Database (split_transaction_db)

```sql
-- Transaction records for expense tracking
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paid_by BIGINT NOT NULL,         -- User who paid
    owed_by BIGINT NOT NULL,         -- User who owes
    amount DECIMAL(10,2) NOT NULL,   -- Amount owed by this user
    description VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    total_amount DECIMAL(10,2),      -- Original total expense
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    group_id VARCHAR(50),            -- For grouping related transactions
    status ENUM('ACTIVE', 'CANCELLED', 'SETTLED') DEFAULT 'ACTIVE',
    split_type ENUM('EQUAL', 'EXACT', 'PERCENTAGE') DEFAULT 'EQUAL',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_paid_by (paid_by),
    INDEX idx_owed_by (owed_by),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_group_id (group_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
);
```

### 4. Balance Service Database (split_balance_db)

```sql
-- Net balances between user pairs
CREATE TABLE balances (
    balance_id VARCHAR(50) PRIMARY KEY,  -- Format: "userId1_userId2"
    user1 BIGINT NOT NULL,              -- Lower user ID
    user2 BIGINT NOT NULL,              -- Higher user ID  
    amount DECIMAL(10,2) DEFAULT 0.00,  -- Positive: user1 owes user2
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_count BIGINT DEFAULT 0,
    last_transaction_id BIGINT,
    
    INDEX idx_user1 (user1),
    INDEX idx_user2 (user2),
    INDEX idx_amount (amount),
    INDEX idx_last_updated (last_updated)
);

-- Settlement records for debt payments
CREATE TABLE settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payer_id BIGINT NOT NULL,           -- User who made payment
    payee_id BIGINT NOT NULL,           -- User who received payment
    amount DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    balance_id VARCHAR(50),             -- Reference to balance
    settlement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    method ENUM('CASH', 'BANK_TRANSFER', 'Phonepay', 'PAYPAL', 'UPI', 'OTHER') DEFAULT 'CASH',
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED') DEFAULT 'COMPLETED',
    notes TEXT,
    reference_id VARCHAR(100),          -- External payment reference
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_payer_id (payer_id),
    INDEX idx_payee_id (payee_id),
    INDEX idx_balance_id (balance_id),
    INDEX idx_settlement_date (settlement_date),
    INDEX idx_method (method),
    INDEX idx_status (status)
);
```

### Database Relationships

```
Auth Service (users) 
    ↓ (1:1)
User Service (user_profiles.user_id → users.id)

Auth Service (users)
    ↓ (1:many)
Transaction Service (transactions.paid_by/owed_by → users.id)

Transaction Service (transactions)
    ↓ (triggers balance updates)
Balance Service (balances + settlements)
```

---

## Deployment Architecture

### Local Development Setup

```
Development Machine
├── Java 17 JDK
├── Maven 3.6+
├── MySQL 8.0
│   ├── split_auth_db
│   ├── split_user_db  
│   ├── split_transaction_db
│   └── split_balance_db
├── IntelliJ IDEA (or preferred IDE)
└── Postman (for API testing)

Service Ports:
├── 8761 - Eureka Server
├── 8080 - API Gateway
├── 8081 - Auth Service
├── 8082 - User Service
├── 8083 - Transaction Service
└── 8084 - Balance Service
```

### Production Architecture (Future)

```
Load Balancer (NGINX/AWS ALB)
    ↓
API Gateway Cluster (3 instances)
    ↓
Service Mesh (Istio/Linkerd)
    ↓
Kubernetes Cluster
├── Auth Service (3 pods)
├── User Service (3 pods)
├── Transaction Service (5 pods)  -- High traffic
├── Balance Service (3 pods)
└── Eureka Server (3 pods)

Data Layer:
├── MySQL Cluster (Master-Slave)

```

### Container Deployment (Docker)

```dockerfile
# Example Dockerfile for any service
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/*.jar app.jar
COPY wait-for-it.sh wait-for-it.sh

RUN chmod +x wait-for-it.sh

EXPOSE 8081

CMD ["./wait-for-it.sh", "mysql:3306", "--", "java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml example
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
    ports:
      - "3306:3306"

  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"

  auth-service:
    build: ./auth-service
    depends_on:
      - mysql
      - eureka-server
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/split_auth_db
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
```

---

## Technology Stack

### Core Technologies

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Language** | Java | 17 | Primary programming language |
| **Framework** | Spring Boot | 3.4.7 | Application framework |
| **Cloud** | Spring Cloud | 2024.0.1 | Microservices infrastructure |
| **Database** | MySQL | 8.0 | Data persistence |
| **Security** | Spring Security | 6.x | Authentication & authorization |
| **Auth** | JWT (JJWT) | 0.12.5 | Token-based authentication |

### Microservices Components

| Component | Purpose | Key Libraries |
|-----------|---------|---------------|
| **Service Discovery** | Eureka Server | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway | Spring WebFlux |
| **Load Balancing** | Spring Cloud LoadBalancer | Client-side LB |
| **HTTP Client** | RestTemplate | Service communication |
| **Documentation** | SpringDoc OpenAPI | Swagger UI |
| **Monitoring** | Spring Actuator | Health checks, metrics |
| **Validation** | Bean Validation | Request validation |
| **Database** | Spring Data JPA | ORM and data access |

### Development Tools

| Tool | Purpose | Configuration |
|------|---------|---------------|
| **Maven** | Build tool | Multi-module project |
| **IntelliJ IDEA** | IDE | Spring Boot support |
| **Postman** | API testing | Collection export available |
| **MySQL Workbench** | Database management | 4 separate databases |
| **Git** | Version control | Microservices monorepo |
| **Docker** | Containerization | Multi-service compose |

---

## API Documentation

### Swagger UI Endpoints

Each service provides interactive API documentation:

| Service | Direct Access | Via Gateway |
|---------|---------------|-------------|
| **Auth Service** | http://localhost:8081/swagger-ui.html | N/A |
| **User Service** | http://localhost:8082/swagger-ui.html | N/A |
| **Transaction Service** | http://localhost:8083/swagger-ui.html | N/A |
| **Balance Service** | http://localhost:8084/swagger-ui.html | N/A |

### Complete API Endpoints

#### Auth Service API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | No |
| POST | `/login` | Authenticate user | No |
| POST | `/validate` | Validate JWT token | No |
| GET | `/users/{id}` | Get user by ID | No* |
| GET | `/users/email/{email}` | Get user by email | No* |
| GET | `/health` | Service health check | No |
| GET | `/info` | Service information | No |

*Currently public for testing, should be secured in production

#### User Service API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/profiles` | Create user profile | No* |
| GET | `/profiles/{userId}` | Get user profile | No* |
| PUT | `/profiles/{userId}` | Update user profile | Yes |
| GET | `/search/email?q={email}` | Search users by email | No |
| GET | `/search/name?q={name}` | Search users by name | No |
| GET | `/location/{location}` | Get users by location | No |
| GET | `/complete` | Users with complete profiles | No |
| DELETE | `/profiles/{userId}` | Delete user profile | Yes |
| GET | `/stats` | Profile statistics | No |
| GET | `/health` | Service health check | No |
| GET | `/info` | Service information | No |

#### Transaction Service API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create new transaction | Yes |
| GET | `/{transactionId}` | Get transaction by ID | No |
| GET | `/user/{userId}` | Get user's transactions | No* |
| GET | `/between/{userId1}/{userId2}` | Transactions between users | No |
| GET | `/summary/{userId}` | Transaction summary | No |
| GET | `/balance/{userId1}/{userId2}` | Balance between users | No |
| GET | `/category/{category}` | Transactions by category | No |
| GET | `/search?q={description}` | Search transactions | No |
| PUT | `/{transactionId}/status` | Update transaction status | Yes |
| DELETE | `/{transactionId}` | Delete transaction | Yes |
| GET | `/recent/{userId}?limit={limit}` | Recent transactions | No |
| GET | `/stats` | Transaction statistics | No |
| GET | `/health` | Service health check | No |
| GET | `/info` | Service information | No |

#### Balance Service API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/update` | Update balance (internal) | No |
| GET | `/{userId1}/{userId2}` | Balance between users | No |
| GET | `/user/{userId}` | User's balances | No* |
| POST | `/settle` | Create settlement | Yes |
| GET | `/settlements/user/{userId}` | User's settlements | No |
| GET | `/settlements/{userId1}/{userId2}` | Settlements between users | No |
| GET | `/summary/{userId}` | Balance summary | No |
| POST | `/optimize` | Optimize group balances | No |
| GET | `/stats` | Balance statistics | No |
| GET | `/health` | Service health check | No |
| GET | `/info` | Service information | No |

### API Gateway Routing

All client requests should go through the API Gateway:

| Client Request | Routed To | Final Endpoint |
|----------------|-----------|----------------|
| `POST /api/auth/register` | Auth Service | `POST /register` |
| `GET /api/users/profiles/1` | User Service | `GET /profiles/1` |
| `POST /api/transactions/` | Transaction Service | `POST /` |
| `GET /api/balances/1/2` | Balance Service | `GET /1/2` |

---

## Error Handling and Monitoring

### Error Response Format

All services return consistent error responses:

```json
{
  "message": "Descriptive error message",
  "timestamp": "2024-07-10T16:30:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/transactions/"
}
```

### Common HTTP Status Codes

| Status Code | Meaning | When Used |
|-------------|---------|-----------|
| 200 | OK | Successful GET, PUT operations |
| 201 | Created | Successful POST operations |
| 400 | Bad Request | Validation errors, invalid data |
| 401 | Unauthorized | Invalid/missing JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Internal Server Error | Unexpected server errors |

### Health Check Endpoints

Each service provides health monitoring:

```bash
# Service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health

# Service discovery
curl http://localhost:8761/eureka/apps
```

### Logging Strategy

```yaml
# Common logging configuration
logging:
  level:
    com.service: DEBUG           # Application logs
    org.springframework: INFO   # Framework logs
    org.hibernate.SQL: DEBUG    # SQL queries
    root: INFO                  # Default level
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %level - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## Development Guidelines

### Code Organization

```
each-service/
├── src/main/java/com/service/{servicename}/
│   ├── Application.java           # Main application class
│   ├── controller/                # REST controllers
│   ├── service/                   # Business logic
│   ├── repository/                # Data access layer
│   ├── entity/                    # JPA entities
│   ├── dto/                       # Data transfer objects
│   └── config/                    # Configuration classes
├── src/main/resources/
│   ├── application.yml            # Configuration
│   └── logback-spring.xml         # Logging config
└── src/test/java/                 # Unit and integration tests
```

### Coding Standards

#### Naming Conventions
- **Classes**: PascalCase (`UserService`, `TransactionController`)
- **Methods**: camelCase (`getUserById`, `createTransaction`)
- **Variables**: camelCase (`userId`, `transactionAmount`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_TRANSACTION_AMOUNT`)
- **Database**: snake_case (`user_id`, `created_at`)

#### API Design Principles
- **RESTful endpoints** with proper HTTP methods
- **Consistent response formats** across all services
- **Meaningful HTTP status codes**
- **Input validation** on all endpoints
- **Error messages** that are helpful but not revealing

#### Security Best Practices
- **Never log sensitive data** (passwords, tokens)
- **Validate all inputs** at service boundaries
- **Use parameterized queries** to prevent SQL injection
- **Implement proper authorization** checks
- **Sanitize error messages** to prevent information leakage

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private TransactionService transactionService;
    
    @Test
    void shouldCreateTransactionSuccessfully() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        // ... setup test data
        
        // Act
        List<TransactionResponse> result = transactionService.createTransaction(request, 1L);
        
        // Assert
        assertThat(result).hasSize(2);
        verify(transactionRepository).saveAll(any());
    }
}
```

#### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TransactionControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateTransactionEndToEnd() {
        // Test complete request flow
    }
}
```

---

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. Service Discovery Issues

**Problem**: Services not registering with Eureka

**Solutions**:
```bash
# Check Eureka server is running
curl http://localhost:8761

# Verify service configuration
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

#### 2. Database Connection Issues

**Problem**: Cannot connect to MySQL

**Solutions**:
```bash
# Check MySQL is running
mysql -u root -p

# Verify database exists
SHOW DATABASES;

# Check connection string
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/split_auth_db
    username: root
    password: yourpassword
```

#### 3. JWT Token Issues

**Problem**: Token validation failures

**Solutions**:
```bash
# Check token format
Authorization: Bearer <actual-jwt-token>

# Verify secret key length (must be 64+ characters for HS512)
jwt:
  secret: myVeryLongSecretKeyThatIsAtLeast64CharactersLong...

# Check token expiration
jwt:
  expiration: 86400000  # 24 hours
```

#### 4. Inter-Service Communication Issues

**Problem**: Services cannot call each other

**Solutions**:
```java
// Verify RestTemplate is load-balanced
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
    return new RestTemplate();
}

// Use service names, not direct URLs
String url = "http://auth-service/validate";  // ✓ Correct
String url = "http://localhost:8081/validate";  // ✗ Wrong
```

#### 5. CORS Issues

**Problem**: Browser requests blocked by CORS

**Solutions**:
```yaml
# API Gateway CORS configuration
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: 
              - "http://localhost:3000"
            allowedMethods: "*"
            allowedHeaders: "*"
```

### Debugging Tips

#### 1. Check Service Health
```bash
# Individual service health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health

# Eureka registry
curl http://localhost:8761/eureka/apps
```

#### 2. Enable Debug Logging
```yaml
logging:
  level:
    com.service: DEBUG
    org.springframework.web.client: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

#### 3. Test Direct Service Access
```bash
# Bypass API Gateway for testing
curl http://localhost:8081/health
curl http://localhost:8082/health
curl http://localhost:8083/health
curl http://localhost:8084/health
```

#### 4. Database State Verification
```sql
-- Check data consistency
USE split_auth_db;
SELECT COUNT(*) FROM users;

USE split_transaction_db;
SELECT COUNT(*) FROM transactions;

USE split_balance_db;
SELECT COUNT(*) FROM balances;
SELECT COUNT(*) FROM settlements;
```

---

## Performance Considerations

### Scalability Patterns

#### Horizontal Scaling
```yaml
# Multiple service instances
eureka:
  instance:
    instance-id: ₹{spring.application.name}:₹{random.value}
```

#### Database Optimization
```sql
-- Essential indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_balance_users ON balances(user1, user2);
CREATE INDEX idx_settlement_date ON settlements(settlement_date);
```

#### Caching Strategy
```java
// Redis cache for frequently accessed data
@Cacheable(value = "users", key = "#userId")
public User getUserById(Long userId) {
    return userRepository.findById(userId);
}

@CacheEvict(value = "users", key = "#userId")
public User updateUser(Long userId, User user) {
    return userRepository.save(user);
}
```

### Load Testing

```bash
# Apache Bench example
ab -n 1000 -c 10 -H "Authorization: Bearer <token>" \
   http://localhost:8080/api/transactions/user/1

# JMeter test plan for complete user journey
1. Register user
2. Create profile  
3. Add transactions
4. Check balances
5. Create settlements
```

---

## Security Considerations

### Production Security Checklist

#### Authentication & Authorization
- [ ] Strong JWT secret keys (256+ bit)
- [ ] Token expiration (reasonable timeframe)
- [ ] Refresh token mechanism
- [ ] Role-based access control (RBAC)
- [ ] Rate limiting on auth endpoints

#### Data Protection
- [ ] HTTPS only in production
- [ ] Sensitive data encryption at rest
- [ ] Database connection encryption
- [ ] Input validation and sanitization
- [ ] SQL injection prevention

#### Infrastructure Security
- [ ] Network segmentation
- [ ] Firewall rules
- [ ] Secret management (Vault/K8s secrets)
- [ ] Regular security updates
- [ ] Vulnerability scanning

### Security Implementation Examples

#### Rate Limiting
```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final int maxRequests = 100; // per minute
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        // Implement rate limiting logic
    }
}
```

#### Input Validation
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    // @Valid triggers validation annotations
    // Additional custom validation if needed
}

public class RegisterRequest {
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*₹", 
             message = "Password must contain upper, lower case and digit")
    private String password;
}
```

---

## Future Enhancements

### Planned Features

#### 1. Group Management
- Create expense groups for trips, roommates, etc.
- Group-specific balances and settlements
- Group invitations and permissions

#### 2. Advanced Split Types
- Tax and tip calculations
- Percentage-based splits with custom ratios
- Item-level splitting for detailed receipts

#### 3. Notification System
- Email notifications for new expenses
- SMS reminders for outstanding balances
- Push notifications for mobile app

#### 5. Reporting and Analytics
- Spending categorization and insights
- Monthly/yearly expense reports
- Budget tracking and alerts

### Technical Improvements

#### 1. Event-Driven Architecture
```java
// Replace synchronous calls with events
@EventListener
public void handleTransactionCreated(TransactionCreatedEvent event) {
    balanceService.updateBalance(event.getTransaction());
}
```

#### 2. CQRS Pattern
```java
// Separate read and write models
public class TransactionCommandService {
    public void createTransaction(CreateTransactionCommand command) {
        // Write operations
    }
}

public class TransactionQueryService {
    public List<Transaction> getUserTransactions(Long userId) {
        // Read operations with optimized queries
    }
}
```

#### 3. GraphQL API
```graphql
type Query {
  user(id: ID!): User
  transactions(userId: ID!, limit: Int): [Transaction]
  balances(userId: ID!): [Balance]
}

type Mutation {
  createTransaction(input: TransactionInput!): Transaction
  settleBalance(input: SettlementInput!): Settlement
}
```

---

## Conclusion

The Split Group microservices architecture demonstrates modern software development practices with:

- **Clean separation of concerns** across focused services
- **Scalable and maintainable** codebase structure
- **Industry-standard technologies** and patterns
- **Comprehensive API documentation** with Swagger
- **Robust error handling** and monitoring capabilities
- **Security best practices** with JWT authentication
- **Database design** optimized for financial transactions

This architecture provides a solid foundation for building production-ready expense sharing applications and can serve as a reference for microservices development patterns.

### Key Learning Outcomes

1. **Microservices Architecture**: Understanding service decomposition and communication
2. **Spring Cloud Ecosystem**: Service discovery, API Gateway, and configuration
3. **RESTful API Design**: Consistent, well-documented APIs across services
4. **Database Design**: Multi-database architecture with proper relationships
5. **Security Implementation**: JWT-based authentication across services
6. **Inter-Service Communication**: Synchronous HTTP with service discovery
7. **Error Handling**: Consistent error responses and monitoring
8. **Documentation**: Comprehensive API documentation with Swagger

This system successfully demonstrates how to build a complex, real-world application using microservices principles while maintaining code quality, scalability, and maintainability.
|