# 🏨 Booking System - Hair Salon Appointment Booking

Appointment booking system for hair salons with JWT authentication, role-based access control, and comprehensive testing.

**Project Status:** 🚧 In Development

---

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Development Roadmap](#development-roadmap)

---

## 🛠️ Tech Stack

**Backend:**
- Java 21
- Spring Boot 3.5.9
- Spring Security 6.x (JWT authentication)
- Spring Data JPA
- PostgreSQL 15

**Testing:**
- JUnit 5
- Mockito
- TestContainers (real PostgreSQL in tests)
- MockMvc

**Build & DevOps:**
- Maven
- Docker & Docker Compose
- Flyway (database migrations)

---

## ✨ Features

### ✅ Completed (Story 1 - User Authentication)

- **User Authentication**
    - User registration with BCrypt password hashing
    - JWT-based login (stateless authentication)
    - Role-based access control (USER, ADMIN)

- **Security**
    - Spring Security configuration
    - JWT token generation & validation
    - Password encryption (BCrypt)
    - Global exception handling with error codes

- **Testing**
    - Unit tests (Mockito) - 5 tests
    - Integration tests (TestContainers) - 6 tests
    - Coverage: >60%

### 🔄 In Progress (Story 2 - Service Management CRUD with JWT Authentication Filter)

- Service management (CRUD for hair services)
- JWT Authentication Filter
- Role-based authorization (@PreAuthorize)

### 📅 Planned (Story 3-7)

- Booking system (appointments)
- Availability checking (time slots)
- Business rules validation
- Docker deployment

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Postman (optional, for API testing)

### 1️⃣ Clone Repository
```bash
git clone https://github.com/konrad-wojdyna/booking-system.git
cd booking-system
```

### 2️⃣ Start PostgreSQL (Docker)
```bash
docker-compose up -d
```

**Database will be available at:**
- Host: `localhost:5433`
- Database: `booking_db`
- User: `admin`
- Password: `secret_password`

### 3️⃣ Build & Run Application
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

**Application will start at:** `http://localhost:8080`

### 4️⃣ Verify Setup
```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
{"status":"UP"}
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### 🔹 Register New User

**POST** `/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Success Response:** `201 Created`
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "createdAt": "2026-01-03T10:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - Validation failed
- `409 Conflict` - Email already exists

---

#### 🔹 Login

**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response:** `200 OK`
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "createdAt": "2026-01-03T10:30:00",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `400 Bad Request` - Validation failed
- `401 Unauthorized` - Invalid credentials

---

### Error Response Format

All errors follow this structure:
```json
{
  "timestamp": "2024-01-03T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "ERR_001",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

**Error Codes:**
- `ERR_001` - Validation error
- `ERR_002` - Resource not found
- `ERR_003` - Email already exists
- `ERR_004` - Invalid credentials
- `ERR_005` - Access forbidden
- `ERR_999` - Internal server error

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
# Unit tests
mvn test -Dtest=UserServiceTest

# Integration tests
mvn test -Dtest=AuthControllerTest
```

### Test Coverage Report
```bash
mvn clean test jacoco:report

# View report: target/site/jacoco/index.html
```

**Current Coverage:** >60% (line coverage)

---

## 📁 Project Structure
```
booking-system/
├── src/
│   ├── main/
│   │   ├── java/com/booking/bookingsystem/
│   │   │   ├── config/          # Spring Security, JWT config
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── exceptions/      # Custom exceptions + handlers
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── service/         # Business logic
│   │   │   └── utils/           # JWT utilities
│   │   └── resources/
│   │       ├── application.yml  # App configuration
│   │       └── db/migration/    # Flyway migrations
│   └── test/
│       └── java/com/booking/bookingsystem/
│           ├── AbstractIntegrationTest.java  # TestContainers base
│           ├── controller/      # Integration tests
│           └── service/         # Unit tests
├── docs/                        # Documentation & Postman
├── docker-compose.yml           # PostgreSQL container
├── pom.xml                      # Maven dependencies
└── README.md
```

---

## 🗺️ Development Roadmap

### Phase 1: MVP (Story 1-7)

- [x] **Story 1:** User Authentication ✅
- [ ] **Story 2:** Service Management
- [ ] **Story 3:** Booking Entity + Relationships
- [ ] **Story 4:** Booking CRUD + Availability
- [ ] **Story 5:** Business Rules + Validation 
- [ ] **Story 6:** Comprehensive Testing
- [ ] **Story 7:** Docker + Cleanup

### Phase 2: REFACTOR + Add more Features (Story 8-12)

- [ ] **Story 8:** DTO + MapStruct
- [ ] **Story 9:** Email Notifications 
- [ ] **Story 10:** File Upload 
- [ ] **Story 11:** Background Jobs 
- [ ] **Story 12:** Production Config 

### Phase 3: ARCHITECTURE (Story 13-17)

- [ ] **Story 13:** Hexagonal Architecture 
- [ ] **Story 14:** Application Layer
- [ ] **Story 15:** Domain Events
- [ ] **Story 16:** Design Patterns + Caching 
- [ ] **Story 17:** Architecture Tests + CI/CD

---

## 👨‍💻 Author

**Konrad Wojdyna** - Full-Stack Developer 

---

## 📝 License

This is a learning project for portfolio purposes.

---

## 🔗 Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security JWT Guide](https://spring.io/guides/tutorials/spring-security-and-angular-js/)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [Postman Collection](./docs/Booking-System.postman_collection.json)

---

**Last Updated:** January 3, 2026  
**Version:** v1.0.0