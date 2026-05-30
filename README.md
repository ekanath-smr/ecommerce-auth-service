# Ecommerce Auth Service

A production-grade Authentication & Authorization Microservice built using Spring Boot and Spring Security for a distributed e-commerce platform.

This service provides secure user registration, login, JWT-based authentication, refresh token support, logout with token blacklisting, role-based access control, and centralized authentication for downstream microservices through API Gateway integration.

---

## 🚀 Features

### Authentication

* JWT Access Token Authentication
* Refresh Token Support
* User Registration
* User Login
* Secure Password Hashing using BCrypt
* Logout with Token Blacklisting

### Authorization

* Role-Based Access Control (RBAC)
* USER Role Support
* ADMIN Role Support
* Method-Level Security using `@PreAuthorize`

### Security

* Stateless Spring Security Configuration
* Custom JWT Authentication Filter
* Custom AuthenticationEntryPoint
* Custom AccessDeniedHandler
* Global Exception Handling
* DTO Validation with Jakarta Validation

### Microservices Integration

* Eureka Service Discovery Client
* API Gateway Integration
* Dynamic Service Registration
* Client-Side Load Balancing using Spring Cloud LoadBalancer
* Centralized Authentication for Downstream Services

### Observability

* Spring Boot Actuator
* Structured Logging with SLF4J
* Swagger/OpenAPI Documentation

### Quality

* Layered Architecture
* Unit Testing with JUnit 5 & Mockito
* Clean Separation of Concerns

---

## 🧠 Architecture Overview

This service acts as the centralized authentication provider within a microservices ecosystem.

### Responsibilities

* Authenticate users
* Issue JWT tokens
* Validate user credentials
* Enforce role-based authorization
* Revoke tokens on logout
* Integrate with API Gateway
* Register with Service Discovery

### High-Level Architecture

```text
                         +------------------+
                         |  API Gateway     |
                         +--------+---------+
                                  |
                                  |
                                  v
                        +---------+---------+
                        | Auth Service      |
                        | (JWT Provider)    |
                        +---------+---------+
                                  |
                                  |
                                  v
                         +--------+--------+
                         | MySQL Database  |
                         +-----------------+

                                  ^
                                  |
                     Registers With Eureka
                                  |
                                  v

                     +----------------------+
                     | Service Discovery    |
                     | (Eureka Server)      |
                     +----------------------+
```

---

## 🔄 Authentication Flow

### Registration Flow

```text
Client
   |
   v
POST /auth/register
   |
   v
Validate Request
   |
   v
Encrypt Password
   |
   v
Assign Roles
   |
   v
Generate Access Token
Generate Refresh Token
   |
   v
Return Tokens
```

---

### Login Flow

```text
Client
   |
   v
POST /auth/login
   |
   v
AuthenticationManager
   |
   v
UserDetailsService
   |
   v
JWT Generation
   |
   v
Return Tokens
```

---

### Request Authentication Flow

```text
Client
   |
Bearer Token
   |
   v
API Gateway
   |
   v
Auth Service Validation
   |
   v
JwtFilter
   |
   v
SecurityContext
   |
   v
Protected Resource
```

---

### Logout Flow

```text
Client
   |
POST /auth/logout
   |
   v
Extract JWT
   |
   v
Blacklist Token
   |
   v
Future Requests Rejected
```

---

## 🏗️ Tech Stack

### Backend

* Java 17+
* Spring Boot
* Spring Security
* Spring Data JPA
* Spring Validation

### Security

* JWT (JJWT)
* BCrypt Password Encoder

### Database

* MySQL

### Microservices

* Spring Cloud Netflix Eureka Client
* Spring Cloud LoadBalancer
* Spring Cloud Gateway Integration

### Documentation

* Swagger/OpenAPI

### Monitoring

* Spring Boot Actuator

### Testing

* JUnit 5
* Mockito

### Build Tool

* Maven

### Utilities

* Lombok

---

## 📂 Project Structure

```text
ecommerce-auth-service
│
├── controllers
│   ├── AuthController
│   └── TestController
│
├── services
│   ├── AuthService
│   ├── AuthServiceImpl
│   └── TokenBlacklistService
│
├── repositories
│   ├── UserRepository
│   └── RoleRepository
│
├── models
│   ├── User
│   ├── Role
│   └── BaseModel
│
├── dtos
│   ├── RegisterRequestDto
│   ├── LoginRequestDto
│   ├── RefreshTokenRequestDto
│   └── AuthResponseDto
│
├── security
│   ├── JwtService
│   ├── JwtFilter
│   ├── SecurityConfig
│   ├── CustomUserDetails
│   ├── CustomUserDetailsService
│   ├── CustomAuthenticationEntryPoint
│   └── CustomAccessDeniedHandler
│
├── config
│   ├── OpenApiConfig
│   └── DiscoveryConfig
│
├── exceptions
│   ├── UserAlreadyExistsException
│   ├── InvalidCredentialsException
│   └── InvalidRoleException
│
└── advices
    └── GlobalExceptionHandler
```

---

## 👤 Supported Roles

### ROLE_USER

Can access:

```text
/test/user
```

### ROLE_ADMIN

Can access:

```text
/test/admin
```

---

## 🔒 Security Rules

```text
/auth/**                 -> Public

/swagger-ui/**           -> Public

/v3/api-docs/**          -> Public

/actuator/**             -> Public

/test/user               -> ROLE_USER

/test/admin              -> ROLE_ADMIN
```

---

## 📡 API Endpoints

### Register User

```http
POST /auth/register
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "roles": ["USER"]
}
```

---

### Login

```http
POST /auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

### Refresh Token

```http
POST /auth/refresh
```

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

---

### Logout

```http
POST /auth/logout
```

Authorization Header:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

---

## 🔑 JWT Claims

JWT Tokens contain:

* Subject (Email)
* Roles
* Token Type
* Issued Timestamp
* Expiration Timestamp

### Token Types

```text
ACCESS
REFRESH
```

---

## ⏱ Token Expiration

| Token Type    | Default Expiration |
| ------------- | ------------------ |
| Access Token  | 15 Minutes         |
| Refresh Token | 7 Days             |

---

## 🌐 Service Discovery

The service registers itself with Eureka Service Discovery.

### Benefits

* Dynamic service registration
* Dynamic service lookup
* No hardcoded service URLs
* Better scalability
* Better fault tolerance

Example:

```text
AUTH-SERVICE
```

appears automatically in Eureka Dashboard.

---

## ⚖️ Load Balancing

This service participates in a load-balanced microservices architecture.

### Supported

* Client-Side Load Balancing
* Multiple Service Instances
* Dynamic Instance Discovery

Example:

```text
PRODUCT-SERVICE

Instance-1
Instance-2
Instance-3
```

Requests are distributed automatically using Spring Cloud LoadBalancer.

---

## 📊 Monitoring

### Health Endpoint

```http
GET /actuator/health
```

Example Response:

```json
{
  "status": "UP"
}
```

---

## 📘 API Documentation

Swagger UI:

```text
http://localhost:9000/swagger-ui/index.html
```

Features:

* JWT Authorization Support
* Interactive API Testing
* Request/Response Schemas
* OpenAPI 3 Specification

---

## ▶️ Running the Project

### Clone Repository

```bash
git clone https://github.com/ekanath-smr/ecommerce-auth-service.git
```

### Navigate

```bash
cd ecommerce-auth-service
```

### Run Application

```bash
mvn spring-boot:run
```

---

## 🧪 Testing

Run Tests

```bash
mvn test
```

Coverage Includes:

* Registration Flow
* Login Flow
* Invalid Credentials
* Role Validation
* Logout Flow
* Token Blacklisting
* Token Validation

---

## 🔒 Security Considerations

* BCrypt Password Hashing
* JWT Signature Verification
* Stateless Authentication
* Role-Based Authorization
* Token Blacklisting
* Request Validation
* Custom Security Handlers
* Method-Level Access Control

---

## ⚠️ Current Limitation

Token blacklist currently uses:

```text
ConcurrentHashMap
```

This works for a single application instance but is not shared across multiple instances.

### Production Recommendation

Use:

```text
Redis + TTL
```

for distributed token revocation.

---

## ☁️ Deployment Readiness

* Docker Friendly
* Kubernetes Friendly
* Environment-Based Configuration
* Horizontally Scalable
* Eureka Service Discovery Enabled
* API Gateway Compatible
* Cloud-Native Architecture

---

## 📌 Future Improvements

* Redis-based Token Blacklisting
* Refresh Token Rotation
* OAuth2 Login
* Google Login
* GitHub Login
* Email Verification
* Password Reset Flow
* Rate Limiting
* Distributed Tracing
* Metrics Dashboard
* Session Management

---

## 👨‍💻 Author

**Ekanath S M R**

Backend Developer | Java | Spring Boot | Microservices

Focused on designing scalable, secure, and production-ready backend systems with distributed microservice architectures.
