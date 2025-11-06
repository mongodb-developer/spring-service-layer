# Service Layer Pattern Demo with Spring Boot & MongoDB

A complete demonstration of the Service Layer architectural pattern in Spring Boot, showing how to build clean, maintainable, and testable REST APIs with proper separation of concerns.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📖 About This Project

This project demonstrates the **Service Layer Pattern** - an architectural pattern that separates business logic from controllers and data access layers. It shows best practices for building scalable Spring Boot applications with:

- **Clean Architecture**: Clear separation between Controller, Service, and Repository layers
- **Business Logic Isolation**: All business rules centralized in the service layer
- **Proper Exception Handling**: Custom exceptions with global error handling
- **Input Validation**: Bean Validation with comprehensive error responses
- **MongoDB Integration**: Using Spring Data MongoDB for data persistence
- **Comprehensive Testing**: Unit and integration tests with high coverage

## Features

- User management (CRUD operations)
- Email validation and uniqueness checks
- Active/inactive user states
- RESTful API design
- Global exception handling with proper HTTP status codes
- Mock email service for demonstration
- Unit tests for service and controller layers
- Spring Data MongoDB integration


## Technologies Used

- **Java 17**
- **Spring Boot 3.5.7**
- **Spring Data MongoDB**
- **Spring Boot Validation**
- **MongoDB**
- **JUnit 5** & **Mockito** for testing
- **Maven** for dependency management

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))
- **MongoDB Atlas account** ([Sign up free](https://www.mongodb.com/cloud/atlas/register)) or local MongoDB installation

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR-USERNAME/spring-service-layer.git
cd spring-service-layer
```

### 2. Set Up MongoDB

#### MongoDB Atlas

1. Create a free account at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register)
2. Create a new cluster (free tier M0 is fine)
3. Create a database user with read/write permissions
4. Whitelist your IP address (or use `0.0.0.0/0` for development)
5. Get your connection string by clicking "Connect" → "Connect your application"

Your connection string will look like:
```
mongodb+srv://username:password@cluster0.abc12.mongodb.net/?appName=Cluster0
```

### 3. Configure Application

Update `src/main/resources/application.properties`:

```properties
spring.application.name=spring-service-layer

# MongoDB Configuration
# IMPORTANT: Replace ?appName=Cluster0 with ?appName=spring-service-layer-tutorial
# This helps track tutorial usage!

# For MongoDB Atlas:
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.abc12.mongodb.net/?appName=spring-service-layer-tutorial

# For Local MongoDB:
# spring.data.mongodb.uri=mongodb://localhost:27017/serviceLayer?appName=spring-service-layer-tutorial

spring.data.mongodb.database=serviceLayer

# Logging
logging.level.com.mongodb.springservicelayer=INFO
```

**Important:** Replace `username`, `password`, and `cluster.abc12` with your actual MongoDB credentials.

### 4. Build the Project
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com"
  }'
```

**Response (201 Created):**
```json
{
  "id": "generated-uuid",
  "email": "john@example.com",
  "name": "John Doe",
  "createdAt": "2024-11-06T10:30:45.123",
  "active": true
}
```

### Get User by ID
```bash
curl -X GET http://localhost:8080/api/users/{id}
```

### Update User Name
```bash
curl -X PUT http://localhost:8080/api/users/{id}/name \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe"
  }'
```

### Deactivate User
```bash
curl -X DELETE http://localhost:8080/api/users/{id}
```

### Get All Active Users
```bash
curl -X GET http://localhost:8080/api/users/active
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
# Controller tests
mvn test -Dtest=UserControllerTest

# Service tests
mvn test -Dtest=UserServiceImplTest
```

### Test Coverage
The project includes comprehensive tests for:
- Controller layer (REST API endpoints)
- Service layer (business logic)
- Exception handling
- Validation logic

## Project Structure
```
spring-service-layer/
├── src/
│   ├── main/
│   │   ├── java/com/mongodb/springservicelayer/
│   │   │   ├── SpringServiceLayerApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   └── UpdateNameRequest.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── EmailServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java
│   │   │   ├── model/
│   │   │   │   └── User.java
│   │   │   └── exception/
│   │   │       ├── DuplicateEmailException.java
│   │   │       ├── InvalidEmailException.java
│   │   │       ├── UserNotFoundException.java
│   │   │       ├── UserInactiveException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/mongodb/springservicelayer/
│           ├── controller/
│           │   └── UserControllerTest.java
│           └── service/
│               └── UserServiceImplTest.java
├── pom.xml
└── README.md
```

## Key Concepts Demonstrated

### 1. Service Layer Pattern
Business logic is isolated in the service layer, keeping controllers thin and repositories focused on data access.

### 2. Dependency Injection
All components use constructor injection for better testability and immutability.

### 3. Exception Handling
Custom business exceptions with a global exception handler that returns proper HTTP status codes:
- `400 Bad Request` - Validation errors
- `403 Forbidden` - Business rule violations (inactive users)
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resources

### 4. Validation
Bean Validation annotations (`@NotBlank`, `@Email`) with automatic error responses.

### 5. Testing Best Practices
- Unit tests with mocked dependencies
- Integration tests for API endpoints
- High test coverage for critical business logic

Topics covered:
- What is the Service Layer pattern and why use it?
- How it fits with MVC architecture
- Step-by-step implementation guide
- Best practices and common mistakes
- MongoDB integration


## Acknowledgments

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MongoDB Java Driver](https://www.mongodb.com/docs/drivers/java/)
- [Martin Fowler's Service Layer Pattern](https://martinfowler.com/eaaCatalog/serviceLayer.html)


---

⭐ If you found this project helpful, please give it a star!
