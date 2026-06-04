🏦 NexusBank
 
NexusBank is a microservices-based banking platform built using Java and Spring Boot to explore enterprise backend development concepts such as authentication, authorization, distributed systems, service discovery, API gateway routing, inter-service communication, and banking business workflows.
 
The project started as a monolithic banking application and was gradually transformed into a microservices architecture to better understand real-world software engineering practices.
 
---
 
🚀 Features
 
Identity Service
 
- User Registration
- User Login
- Password Encryption using BCrypt
- JWT Token Generation
- Role-Based Access Control (ADMIN / CUSTOMER)
 
Banking Service
 
- Account Creation
- Deposit Funds
- Withdraw Funds
- Fund Transfers
- Transaction History
- Ownership Validation for Account Access
 
Loan Service
 
- Loan Application Management
- Loan Approval & Rejection Workflow
- EMI Schedule Generation
- Loan Status Tracking
 
Infrastructure
 
- Service Discovery using Eureka
- API Gateway for Centralized Routing
- Inter-Service Communication using OpenFeign
- Global Exception Handling
- Swagger/OpenAPI Documentation
 
---
 
🛠️ Tech Stack
 
Backend
 
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Spring Cloud
 
Security
 
- JWT Authentication
- Role-Based Authorization
- BCrypt Password Encryption
 
Microservices
 
- Eureka Server
- OpenFeign
- Spring Cloud Gateway
 
Database
 
- MySQL
 
Documentation & Testing
 
- Swagger/OpenAPI
- Postman
 
Build Tool
 
- Maven
 
---
 
📂 Services
 
Service| Port| Responsibility
Identity Service| 8080| Authentication, Users, JWT
Banking Service| 8081| Accounts, Transactions
Loan Service| 8082| Loans, EMI Management
Eureka Server| 8761| Service Discovery
API Gateway| 9000| Centralized Routing
 
---
 
🔐 Security Features
 
- JWT-based Authentication
- Stateless Authorization
- Role-Based Access Control
- Ownership Validation
- Protected Service Endpoints
 
---
 
📖 Key Concepts Practiced
 
- Layered Architecture
- DTO Pattern
- REST API Design
- Authentication & Authorization
- Microservices Architecture
- Service Discovery
- API Gateway Pattern
- Inter-Service Communication
- Distributed System Design
- Exception Handling
- Banking Transaction Integrity
 
---
 
🎯 Project Status
 
Completed
 
- Identity Service
- Banking Service
- Loan Service
- Eureka Service Discovery
- API Gateway Routing
- JWT Security
- OpenFeign Communication
- Swagger Documentation
 
Upcoming
 
- Docker Containerization
- Docker Compose
- Centralized Logging
- Monitoring & Observability
- CI/CD Pipeline
 
---
 
👨‍💻 Author
 
Janaki Ram
 
Building NexusBank as a hands-on journey to understand enterprise backend engineering, microservices, security, and distributed system design.
