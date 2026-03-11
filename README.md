# Ecommerce Auth Service

A **Spring Boot microservice** that provides **JWT-based authentication and role-based authorization** for an e-commerce system.

This service handles **user registration, login, and secure token generation** and is designed to be integrated with other microservices like Product Service.

---

## 🚀 Features

* JWT Authentication
* Role-Based Authorization (ADMIN / USER)
* Secure Password Hashing using BCrypt
* Custom UserDetails Implementation
* Global Exception Handling
* DTO Validation
* Logging using SLF4J
* Stateless Spring Security
* Clean Layered Architecture

---

## 🏗️ Tech Stack

* Java
* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* Spring Data JPA
* MySQL
* Maven
* Lombok

---

## 📂 Project Structure

```
ecommerce-auth-service
│
├── controllers
│       AuthController
│
├── services
│       AuthService
│       AuthServiceImpl
│
├── repositories
│       UserRepository
│
├── models
│       User
│       Role
│
├── dtos
│       RegisterRequestDto
│       LoginRequestDto
│       AuthResponseDto
│
├── security
│       JwtService
│       JwtFilter
│       SecurityConfig
│       CustomUserDetails
│
├── exceptions
│       UserAlreadyExistsException
│       InvalidCredentialsException
│
├── controllerAdvices
│       GlobalExceptionHandler
```

---

## 🔐 Authentication Flow

1. User registers using `/auth/register`
2. Password is encrypted using **BCrypt**
3. User logs in using `/auth/login`
4. Server validates credentials
5. A **JWT token is generated**
6. Client sends the token in request headers

```
Authorization: Bearer <JWT_TOKEN>
```

7. Requests are authenticated via **JwtFilter**

---

## 👤 Roles

Two roles are supported:

* **USER**
* **ADMIN**

Security rules:

```
/auth/**   -> Public
/user/**   -> USER or ADMIN
/admin/**  -> ADMIN only
```

---

## 📡 API Endpoints

### Register User

```
POST /auth/register
```

Request

```
{
  "email": "user@example.com",
  "password": "password123",
  "role": "USER"
}
```

Response

```
{
  "token": "jwt_token_here"
}
```

---

### Login

```
POST /auth/login
```

Request

```
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response

```
{
  "token": "jwt_token_here"
}
```

---

## 🔑 JWT Token

JWT contains:

* User email
* User roles
* Issued time
* Expiration time

Token validity: **24 hours**

---

## ⚙️ Security

* Stateless Authentication
* JWT Token Verification
* BCrypt Password Encoding
* Role-Based Access Control

---

## 🧪 Testing Role-Based Access

Example protected endpoints:

```
GET /admin/dashboard
GET /user/profile
```

Header required:

```
Authorization: Bearer <JWT_TOKEN>
```

---

## ▶️ Running the Project

Clone the repository

```
git clone https://github.com/yourusername/ecommerce-auth-service.git
```

Navigate to project

```
cd ecommerce-auth-service
```

Run the application

```
mvn spring-boot:run
```

---

## 📌 Future Improvements

* API Gateway Integration
* Refresh Tokens
* Email Verification
* OAuth2 / Social Login
* Rate Limiting

---

## 👨‍💻 Author

**Ekanath S M R**

Backend Developer | Java | Spring Boot | Microservices
