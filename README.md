# Ecommerce Auth Service

A production-style JWT Authentication & Authorization Microservice built using Spring Boot and Spring Security for an e-commerce microservices backend.

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

```text
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
```

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
1. Client sends Access Token:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

2. JwtFilter:
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

## 👤 Supported Roles

- ROLE_USER
- ROLE_ADMIN

### Security Rules

```text
/auth/**   -> Public
/user/**   -> USER or ADMIN
/admin/**  -> ADMIN only
```

---

## 📡 API Endpoints

### Register

```http
POST /auth/register
```

#### Request

```json
{
  "email": "user@example.com",
  "password": "password123",
  "roles": ["USER"]
}
```

#### Response

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "email": "user@example.com"
}
```

---

### Login

```http
POST /auth/login
```

#### Request

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

#### Request

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

#### Header

```http
Authorization: Bearer <ACCESS_TOKEN>
```

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

- Access Token: Configurable (Default: 15 Minutes)
- Refresh Token: Configurable (Default: 7 Days)

---

## 📊 Monitoring

### Health Check

```http
GET /actuator/health
```

#### Example Response

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

OpenAPI JSON:

```text
/v3/api-docs
```

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

Run Unit Tests:

```bash
mvn test
```

Includes unit tests for:

- Registration Flow
- Login Flow
- Invalid Credentials
- Role Validation
- Logout / Blacklisting
- Token Validation

---

## ⚠️ Current Limitation

Token blacklist is currently implemented using **in-memory storage**:

```text
ConcurrentHashMap / ConcurrentHashSet
```

### Production Recommendation

Use **Redis with TTL** for distributed scalable token revocation.

---

## 📌 Future Improvements

- Redis-based Token Blacklisting
- API Gateway Integration
- OAuth2 / Social Login
- Email Verification
- Rate Limiting
- Refresh Token Rotation
- Device/Session Management
- Distributed Tracing / Metrics

---

## 👨‍💻 Author

**Ekanath S M R**

Backend Developer  
Java | Spring Boot | Microservices