# Ecommerce Auth Service

A JWT-based Authentication Microservice built using Spring Boot and Spring Security for an e-commerce backend system.

This service provides secure user authentication, role-based authorization, and token-based access control for other microservices such as Product Service.

--------------------------------------------------

## 🚀 Features

- JWT Authentication
- Role-Based Authorization (ADMIN / USER)
- Secure Password Hashing using BCrypt
- Stateless Security Architecture
- Custom UserDetails Implementation
- Global Exception Handling
- DTO Validation
- Logging using SLF4J
- API Documentation with Swagger UI
- Application Monitoring with Spring Boot Actuator
- Default Admin & User Initialization on Application Startup
- Clean Layered Architecture
- Unit Testing with Mockito

--------------------------------------------------

## 🏗️ Tech Stack

- Java
- Spring Boot
- Spring Security
- JWT (JJWT)
- Spring Data JPA
- MySQL
- Maven
- Lombok
- Swagger / OpenAPI
- Spring Boot Actuator

--------------------------------------------------

## 📂 Project Structure

ecommerce-auth-service

controllers  
AuthController  
TestController

services  
AuthService  
AuthServiceImpl

repositories  
UserRepository

models  
User  
Role

dtos  
RegisterRequestDto  
LoginRequestDto  
AuthResponseDto

security  
JwtService  
JwtFilter  
SecurityConfig  
CustomUserDetails  
CustomUserDetailsService  
CustomAccessDeniedHandler

config  
DataInitializer
PasswordEncoderConfig

exceptions  
UserAlreadyExistsException  
InvalidCredentialsException

controllerAdvices  
GlobalExceptionHandler

--------------------------------------------------

## 🔐 Authentication Flow

1. User registers via `/auth/register`
2. Password is encrypted using BCrypt
3. User logs in using `/auth/login`
4. Server validates credentials
5. A JWT token is generated
6. Client sends the token in request headers

Authorization: Bearer <JWT_TOKEN>

7. Requests are authenticated using JwtFilter
8. Access is granted based on user roles

--------------------------------------------------

## 👤 Roles

Two roles are supported:

ROLE_USER  
ROLE_ADMIN

Security rules:

/auth/**   -> Public  
/user/**   -> USER or ADMIN  
/admin/**  -> ADMIN only

--------------------------------------------------

## 📡 API Endpoints

### Register User

POST /auth/register

Request

{
"email": "user@example.com",
"password": "password123",
"role": "ROLE_USER"
}

Response

{
"token": "jwt_token_here"
}

--------------------------------------------------

### Login

POST /auth/login

Request

{
"email": "user@example.com",
"password": "password123"
}

Response

{
"token": "jwt_token_here"
}

--------------------------------------------------

## 🔑 JWT Token

JWT contains:

- User email
- User role
- Issued time
- Expiration time

Token validity: 24 hours

Clients must include the token in request headers:

Authorization: Bearer <JWT_TOKEN>

--------------------------------------------------

## 🔒 Role-Based Access Testing

Example protected endpoints:

GET /admin/test  
GET /user/test

Access Rules:

/admin/test -> ADMIN only  
/user/test -> USER or ADMIN

--------------------------------------------------

## 📊 Monitoring

Application health monitoring is available using Spring Boot Actuator.

GET /actuator/health

Example response:

{
"status": "UP"
}

--------------------------------------------------

## 📘 API Documentation

Interactive API documentation is available using Swagger UI.

http://localhost:8080/swagger-ui/index.html

OpenAPI JSON specification:

/v3/api-docs

--------------------------------------------------

## 👥 Default Users (Created at Startup)

The application automatically creates default users on startup.

admin@example.com  
Password: admin123  
Role: ROLE_ADMIN

user@example.com  
Password: user123  
Role: ROLE_USER

These users are created only if they do not already exist.

--------------------------------------------------

## ▶️ Running the Project

Clone the repository

git clone https://github.com/yourusername/ecommerce-auth-service.git

Navigate to the project

cd ecommerce-auth-service

Run the application

mvn spring-boot:run

--------------------------------------------------

## 📌 Future Improvements

- API Gateway Integration
- Refresh Token Implementation
- Email Verification
- OAuth2 / Social Login
- Rate Limiting
- Redis Token Blacklisting

--------------------------------------------------

## 👨‍💻 Author

Ekanath S M R

Backend Developer | Java | Spring Boot | Microservices