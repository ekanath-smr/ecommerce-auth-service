# Ecommerce Auth Service

A production-style JWT Authentication & Authorization Microservice built using Spring Boot and Spring Security for an e-commerce backend.

Provides secure user registration, login, refresh token handling, logout with token blacklisting, and role-based access control for downstream services.

---

## 🚀 Features

- JWT Access Token Authentication
- Refresh Token Support
- Role-Based Authorization (USER / ADMIN)
- Secure Password Hashing using BCrypt
- Logout with Token Blacklisting
- Stateless Spring Security Architecture
- Custom JWT Authentication Filter
- Custom AuthenticationEntryPoint & AccessDeniedHandler
- Global Exception Handling
- DTO Validation with Jakarta Validation
- Structured Logging with SLF4J
- Swagger / OpenAPI Documentation
- Spring Boot Actuator Monitoring
- Layered Clean Architecture
- Unit Testing with JUnit 5 & Mockito

---

## 🧠 Architecture Overview

This service is designed as a stateless authentication microservice.

- Follows layered architecture (Controller → Service → Repository)
- Stateless JWT-based authentication (no session storage)
- Token revocation handled via blacklist
- Designed to integrate with API Gateway for centralized authentication
- Horizontally scalable (except in-memory blacklist limitation)

### High-Level Flow

Client → API Gateway → Auth Service → Downstream Services

---

## 🏗️ Tech Stack

- Java 17+
- Spring Boot
- Spring Security
- JWT (JJWT)
- Spring Data JPA
- MySQL
- Maven
- Lombok
- Swagger / OpenAPI
- Spring Boot Actuator
- JUnit 5
- Mockito

---

## 📂 Project Structure

ecommerce-auth-service
│
├── controllers
│   └── AuthController
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
│   └── Role
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
├── exceptions
│   ├── UserAlreadyExistsException
│   ├── InvalidCredentialsException
│   └── InvalidRoleException
│
└── advices
└── GlobalExceptionHandler

---

## 🔐 Authentication Flow

### Registration

1. User sends request to `/auth/register`
2. Password is encrypted using BCrypt
3. Roles are validated and assigned
4. Access + Refresh tokens are generated
5. Tokens returned to client

---

### Login

1. User sends credentials to `/auth/login`
2. AuthenticationManager validates credentials
3. Access + Refresh tokens generated
4. Tokens returned to client

---

### Authenticated Requests

Client sends Access Token:

Authorization: Bearer <ACCESS_TOKEN>

JwtFilter:
- Validates token signature
- Checks blacklist
- Loads user details
- Sets SecurityContext

---

### Refresh Token

1. Client sends Refresh Token to `/auth/refresh`
2. Service validates refresh token
3. Generates new Access Token
4. Returns new Access Token

---

### Logout

1. Client sends Access Token to `/auth/logout`
2. Token added to blacklist
3. Future use of token is rejected

---

## 🔄 Authentication Sequence

### Login Flow

Client → AuthController → AuthenticationManager → UserDetailsService → JWT Generation → Client

### Request Flow

Client → JwtFilter → Token Validation → SecurityContext → Controller

---

## 👤 Supported Roles

- ROLE_USER
- ROLE_ADMIN

### Security Rules

/auth/**   -> Public  
/user/**   -> USER or ADMIN  
/admin/**  -> ADMIN only

---

## 📡 API Endpoints

### Register

POST /auth/register

{
"email": "user@example.com",
"password": "password123",
"roles": ["USER"]
}

---

### Login

POST /auth/login

{
"email": "user@example.com",
"password": "password123"
}

---

### Refresh Token

POST /auth/refresh

{
"refreshToken": "jwt-refresh-token"
}

---

### Logout

POST /auth/logout

Authorization: Bearer <ACCESS_TOKEN>

---

## 🔑 JWT Claims

JWT Tokens include:

- Subject (Email)
- Roles
- Token Type (ACCESS / REFRESH)
- Issued At
- Expiration Time

---

## ⏱ Token Expiration

- Access Token: 15 Minutes (configurable)
- Refresh Token: 7 Days (configurable)

---

## 🔒 Security Considerations

- Passwords hashed using BCrypt
- JWT signed with HMAC SHA-256
- Role-based authorization enforced via Spring Security
- Access vs Refresh token separation
- Token blacklist prevents reuse after logout
- Input validation using Jakarta Validation
- Method-level security using @PreAuthorize

---

## ⚖️ Design Tradeoffs

- In-memory blacklist is fast but not scalable across instances
- JWT is stateless but requires explicit revocation strategy
- Refresh token reuse vs rotation (currently reuse for simplicity)
- Roles embedded in token reduce DB calls but need reissue on role change

---

## 📊 Monitoring

### Health Check

GET /actuator/health

{
"status": "UP"
}

---

## 📘 API Documentation

Swagger UI:

http://localhost:9000/swagger-ui/index.html

- Supports JWT authentication via Authorize button
- All secured endpoints require Bearer token
- Includes request/response schemas

---

## ▶️ Running the Project

### Clone Repository

git clone https://github.com/ekanath-smr/ecommerce-auth-service.git

### Navigate

cd ecommerce-auth-service

### Run Application

mvn spring-boot:run

---

## 🧪 Testing

Run Unit Tests:

mvn test

Includes tests for:

- Registration Flow
- Login Flow
- Invalid Credentials
- Role Validation
- Logout / Blacklisting
- Token Validation

---

## ⚠️ Current Limitation

Token blacklist uses in-memory storage:

ConcurrentHashMap / ConcurrentHashSet

### Production Recommendation

Use Redis with TTL for scalable distributed token revocation.

---

## ☁️ Deployment Readiness

- Can be containerized using Docker
- Suitable for Kubernetes deployment
- Externalized configuration via application.properties
- Supports environment-based configs

---

## 📌 Future Improvements

- Redis-based Token Blacklisting
- API Gateway Integration
- OAuth2 / Social Login
- Email Verification
- Rate Limiting
- Refresh Token Rotation
- Device/Session Management
- Distributed Tracing & Metrics

---

## 👨‍💻 Author

Ekanath S M R

Backend Developer | Java | Spring Boot | Microservices

Focused on building scalable backend systems and preparing for SDE roles.