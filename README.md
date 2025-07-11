# Split Group Microservices - Local Setup Guide

A complete expense-splitting application built with Spring Boot microservices architecture, similar to Splitwise.

## 📋 Prerequisites

### Required Software
- **Java 17** (JDK 17 or later)
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**
- **IDE** (IntelliJ IDEA recommended)

### Optional Tools
- **Postman** (for API testing)
- **MySQL Workbench** (for database management)

## 🏗️ Architecture Overview

The application consists of 6 microservices:

| Service | Port | Purpose |
|---------|------|---------|
| **Eureka Server** | 8761 | Service Discovery |
| **API Gateway** | 8080 | Single Entry Point |
| **Auth Service** | 8081 | User Authentication |
| **User Service** | 8082 | Profile Management |
| **Transaction Service** | 8083 | Expense Tracking |
| **Balance Service** | 8084 | Balance Management |

## 🛠️ Installation Steps

### Step 1: Clone the Repository

```bash
git clone <your-repository-url>
cd SplitGroupService
```

### Step 2: Install Java 17

#### Windows:
1. Download from [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. Install and set JAVA_HOME environment variable

#### macOS:
```bash
brew install openjdk@17
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

#### Linux (Ubuntu):
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**Verify Installation:**
```bash
java -version
# Should show: openjdk version "17.x.x"
```

### Step 3: Install Maven

#### Windows:
1. Download from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract and add to PATH

#### macOS:
```bash
brew install maven
```

#### Linux:
```bash
sudo apt install maven
```

**Verify Installation:**
```bash
mvn -version
```

### Step 4: Install MySQL

#### Windows:
1. Download [MySQL Installer](https://dev.mysql.com/downloads/installer/)
2. Install with default settings
3. Remember your root password

#### macOS:
```bash
brew install mysql
brew services start mysql
```

#### Linux:
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

**Verify Installation:**
```bash
mysql -u root -p
# Enter your password
```

## 🗄️ Database Setup

### Step 1: Create Databases

Open MySQL and run these commands:

```sql
-- Create databases for each service
CREATE DATABASE IF NOT EXISTS split_auth_db;
CREATE DATABASE IF NOT EXISTS split_user_db;
CREATE DATABASE IF NOT EXISTS split_transaction_db;
CREATE DATABASE IF NOT EXISTS split_balance_db;

-- Verify databases created
SHOW DATABASES;
```

### Step 2: Configure Database Passwords

Update MySQL password in each service's `application.yml` file:

```bash
# Update these files with your MySQL root password:
auth-service/src/main/resources/application.yml
user-service/src/main/resources/application.yml
transaction-service/src/main/resources/application.yml
balance-service/src/main/resources/application.yml
```

Change this line in each file:
```yaml
password: yourpassword  # Replace with your actual MySQL password
```

## 🚀 Running the Application

### Step 1: Start Services in Order

**Important:** Services must be started in this specific order!

#### 1. Start Eureka Server (Service Discovery)
```bash
cd eureka-server
./mvnw spring-boot:run
```
✅ **Wait for**: "Started EurekaServerApplication"  
🌐 **Check**: http://localhost:8761

#### 2. Start API Gateway
```bash
# In a new terminal
cd api-gateway
./mvnw spring-boot:run
```
✅ **Wait for**: "Started ApiGatewayApplication"

#### 3. Start Auth Service
```bash
# In a new terminal
cd auth-service
./mvnw spring-boot:run
```
✅ **Wait for**: "Started AuthServiceApplication"

#### 4. Start User Service
```bash
# In a new terminal
cd user-service
./mvnw spring-boot:run
```
✅ **Wait for**: "Started UserServiceApplication"

#### 5. Start Transaction Service
```bash
# In a new terminal
cd transaction-service
./mvnw spring-boot:run
```
✅ **Wait for**: "Started TransactionServiceApplication"

#### 6. Start Balance Service
```bash
# In a new terminal
cd balance-service
./mvnw spring-boot:run
```
✅ **Wait for**: "Started BalanceServiceApplication"

### Step 2: Verify All Services are Running

Open Eureka Dashboard: http://localhost:8761

You should see all 6 services registered:
- ✅ EUREKA-SERVER
- ✅ API-GATEWAY
- ✅ AUTH-SERVICE
- ✅ USER-SERVICE
- ✅ TRANSACTION-SERVICE
- ✅ BALANCE-SERVICE

## 📚 API Documentation (Swagger)

Each service has interactive API documentation:

| Service | Swagger UI |
|---------|------------|
| **Auth Service** | http://localhost:8081/swagger-ui.html |
| **User Service** | http://localhost:8082/swagger-ui.html |
| **Transaction Service** | http://localhost:8083/swagger-ui.html |
| **Balance Service** | http://localhost:8084/swagger-ui.html |

## 🧪 Testing the Application

### Option 1: Using Swagger UI (Recommended)

1. **Register a User**:
    - Open: http://localhost:8081/swagger-ui.html
    - Use `POST /register` endpoint
    - Save the JWT token from response

2. **Test Complete Flow**:
    - Create user profiles
    - Add transactions
    - Check balances
    - Create settlements

### Option 2: Using Postman

#### Test Endpoints via API Gateway:

**Base URL**: `http://localhost:8080`

```bash
# Register User
POST http://localhost:8080/api/auth/register
{
  "email": "karthii@k.com",
  "password": "password123",
  "name": "karthii k"
}

# Create Transaction
POST http://localhost:8080/api/transactions/
Authorization: Bearer YOUR_JWT_TOKEN
{
  "paidBy": 1,
  "totalAmount": 60.00,
  "description": "Dinner",
  "splitType": "EQUAL",
  "participants": [{"userId": 1}, {"userId": 2}]
}

# Check Balance
GET http://localhost:8080/api/balances/1/2
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Services Won't Start
```bash
# Check if ports are busy
netstat -tlnp | grep :8080
netstat -tlnp | grep :8081

# Kill processes if needed
sudo kill -9 <process-id>
```

#### 2. Database Connection Issues
```bash
# Test MySQL connection
mysql -u root -p -e "SHOW DATABASES;"

# Check if databases exist
mysql -u root -p -e "SHOW DATABASES LIKE 'split_%';"
```

#### 3. Maven Build Issues
```bash
# Clean and rebuild
./mvnw clean install

# Skip tests if needed
./mvnw clean install -DskipTests
```

#### 4. Service Discovery Issues
- Ensure Eureka Server starts first
- Check all services show in http://localhost:8761
- Restart services if they don't register

### Error Messages

| Error | Solution |
|-------|----------|
| `Port already in use` | Kill the process using the port |
| `Database connection failed` | Check MySQL is running and password is correct |
| `Service not found` | Ensure Eureka Server is running first |
| `JWT token invalid` | Get a new token from login endpoint |

## 📝 Project Structure

```
SplitGroupService/
├── pom.xml (Parent POM)
├── eureka-server/
├── api-gateway/
├── auth-service/
├── user-service/
├── transaction-service/
├── balance-service/
└── README.md
```

## 🌟 Key Features

- **👥 User Management**: Registration, login, profiles
- **💰 Expense Tracking**: Record and split bills
- **⚖️ Balance Calculation**: Automatic debt tracking
- **💳 Settlements**: Record payments between users
- **📊 Optimization**: Minimize transactions in groups
- **🔐 Security**: JWT-based authentication
- **📱 API Gateway**: Single entry point for all services
- **📚 Documentation**: Interactive Swagger UIs

## 📞 Support

### Check Service Health
```bash
curl http://localhost:8080/api/auth/health
curl http://localhost:8080/api/users/health
curl http://localhost:8080/api/transactions/health
curl http://localhost:8080/api/balances/health
```

### View Logs
Each service shows logs in its terminal. Look for:
- ✅ "Started [ServiceName]Application"
- ❌ Error messages with stack traces

### Common Fixes
1. **Restart services** in the correct order
2. **Check database** connectivity
3. **Verify JWT tokens** are valid
4. **Ensure all ports** are available


---

**🎉 Congratulations!** You now have a fully functional microservices-based expense-splitting application running locally!

For questions or issues, check the troubleshooting section or review the individual service documentation.