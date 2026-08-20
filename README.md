# ZeroBug Agent - AI-Powered Unit Test Generation Platform

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat&logo=spring)
![AWS](https://img.shields.io/badge/AWS-Cloud%20Native-FF9900?style=flat&logo=amazon-aws)
![Status](https://img.shields.io/badge/Status-Active%20Development-brightgreen)

## 📌 Overview

**ZeroBug Agent** is an enterprise-grade, AI-powered platform designed to automatically generate unit tests for Java projects. Built on **AWS cloud infrastructure**, it leverages advanced technolo[...]

The platform operates as **two independent services** (Frontend and Backend) communicating through RESTful APIs via the **ProjectApiController**, enabling scalability and independent deployment.

---

## 📖 Workshop & Demo

For in-depth architecture details and AWS integration patterns, and a short demo of the project running on AWS, see:

- **FCAJ Workshop** — Detailed workshop notes and walkthrough of the AWS setup and what was built for this project:
  https://ngokyhod.github.io/FCAJ-Workshop/  
  (Tiếng Việt: bài workshop này trình bày chi tiết những gì tôi đã làm với đồ án trên AWS)

- **YouTube Demo (Video walkthrough & demo)** — A short demo video showing the project in action on AWS:
  https://youtu.be/aVMADaZOKDU  
  (Tiếng Việt: video demo)

The workshop and demo cover:
- AWS infrastructure setup
- Spring Boot best practices
- Bedrock AI integration
- PostgreSQL vector search
- Microservices patterns

---

## 🏗️ Architecture

### System Architecture Overview

The ZeroBug Agent platform is built using modern AWS cloud-native technologies:

```
┌────────────────────────────────────────────────────────────────�[...]
│                          USER TIER                               │
├────────────────────────────────────────────────────────────────�[...]
│ • Web Browser (SPA)          • Desktop Client (Electron)        │
│ • Developer IDE Integration  • CLI Tools                        │
└────────────────────────┬──────────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────────┐
│                    API GATEWAY TIER                             │
├──────────────────────────────────────────────────────────────┤
│ • AWS API Gateway (REST)     • JWT Authorization              │
│ • Amazon Cognito (Auth)      • Request Rate Limiting          │
│ • CloudFront (CDN)           • SSL/TLS Encryption             │
└────────────────────────┬──────────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────────┐
│                APPLICATION TIER                                 │
├──────────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────────┐ │
│ │          Spring Boot Microservices                       │ │
│ ├──────────────────────────────────────────────────────────┤ │
│ │ • ProjectApiController (REST API)                        │ ���
│ │ • Source File Service  • Result Service                  │ │
│ │ • History Service      • AI Invoke Service               │ │
│ │ • Project Import Service (Git/JPA)                       │ │
│ └──────────────────────────────────────────────────────────┘ │
│                                                                 │
│ Runs on: EC2 Instance (Spring Boot Application)               │
└────────────────────────┬──────────────────────────────────────┘
                         │
     ┌───────────────────┼───────────────────┐
     │                   │                   │
┌────▼─────┐    ┌────────▼────────┐   ┌────▼──────┐
│    AI     │    │   DATA STORAGE  │   │  EXTERNAL │
│  SERVICE  │    │                 │   │ SERVICES  │
└──────────┘    └─────────────────┘   └───────────┘
   │                   │                   │
   │            ┌──────┴──────┐             │
   │            │             │             │
┌──▼─────┐  ┌───▼──┐   ┌──────▼────┐  ┌────▼────┐
│Bedrock │  │  S3  │   │PostgreSQL  │  │Secrets  │
│Runtime │  │Cache │   │  + pgVector│  │Manager  │
│(Claude)│  └──────┘   └────────────┘  └─────────┘
└────────┘  
```

---

## 🔧 Technology Stack

### Backend Core
| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Spring Boot | 3.2.5 | REST API & Application Server |
| Language | Java | 17+ | Core Application Logic |
| Build Tool | Maven | Latest | Dependency Management & Build |
| Security | Spring Security | 3.2.5 | Authentication & Authorization |
| Database ORM | Spring Data JPA | 3.2.5 | Database Access Layer |

### AWS Services
| Service | Purpose |
|---------|---------|
| **EC2** | Application hosting (Spring Boot) |
| **Amazon RDS** (PostgreSQL) | Primary database with vector support |
| **Amazon S3** | Source code storage & artifact cache |
| **AWS Secrets Manager** | Credentials & API keys management |
| **Amazon Bedrock** | AI model inference (Claude) |
| **Amazon Cognito** | User authentication & authorization |
| **AWS Lambda** | Async task processing |
| **CloudFront** | CDN for static assets |
| **AWS CloudWatch** | Logging & monitoring |

### Data & AI
| Library | Purpose | Version |
|---------|---------|---------|
| JavaParser | Java source code parsing | 3.25.9 |
| pgVector | Vector embeddings in PostgreSQL | 0.1.4 |
| Hibernate Vector | ORM support for embeddings | 6.4.4.Final |
| AWS SDK for Java | AWS service integration | 2.25.11 |
| Spring AI | LLM integration framework | 1.0.0-M1 |

### Additional Libraries
- **Lombok** - Code generation & reduction
- **JGit** - Git repository interaction
- **PostgreSQL Driver** - Database connectivity
- **H2 Database** - Testing support
- **Spring Mail** - Email notifications

---

## 📁 Project Structure

```
ZeroBug-Agent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/zerobug/
│   │   │       ├── controller/
│   │   │       │   └── ProjectApiController.java      # REST API endpoints
│   │   │       ├── service/
│   │   │       │   ├── ProjectService.java
│   │   │       │   ├── SourceFileService.java
│   │   │       │   │   ├── ResultService.java
│   │   │       │   │   ├── HistoryService.java
│   │   │       │   └── AIInvokeService.java            # Bedrock integration
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       ├── dto/
│   │   │       ├── config/
│   │   │       └── ZeroBugAgentApplication.java        # Entry point
│   │   └── resources/
│   │       ├── application.yml                         # Configuration
│   │       └── schema.sql                              # Database schema
│   └── test/
│       └── java/
├── frontend/                                            # (Optional) Frontend SPA
│   ├── public/
│   ├── src/
│   └── package.json
├── data/
│   └── projects/                                        # Sample project data
│       └── [project-uuid]/
│           ├── source-files/
│           └── generated-tests/
├── .mvn/                                                # Maven wrapper
├── .vscode/                                             # VS Code configuration
├── pom.xml                                              # Maven dependencies
├── mvnw / mvnw.cmd                                      # Maven executables
└── README.md                                            # This file
```

---

## 🚀 Core Features

### 1. **Intelligent Source Code Analysis**
- Parses Java source code using **JavaParser**
- Extracts class structures, methods, parameters, and dependencies
- Identifies testing patterns and edge cases

### 2. **AI-Powered Test Generation**
- Integrates with **Amazon Bedrock** for Claude AI inference
- Generates comprehensive unit tests with multiple test scenarios
- Supports multiple testing frameworks (JUnit 5, Mockito, AssertJ)

### 3. **Vector-Based Code Search**
- Stores code embeddings in **PostgreSQL with pgVector**
- Enables semantic search for similar code patterns
- Improves test relevance through context awareness

### 4. **Project Management**
- Git repository integration via **JGit**
- Project import from GitHub/GitLab repositories
- Version tracking and history management
- S3-based artifact storage

### 5. **Independent Microservices**
- **Backend**: Spring Boot REST API (ProjectApiController)
- **Frontend**: Separate SPA application (React/Vue.js)
- Services communicate through well-defined RESTful APIs
- Can be scaled and deployed independently

### 6. **Security & Authentication**
- JWT-based authentication
- AWS Cognito integration
- Role-based access control (RBAC)
- Encrypted credential storage in AWS Secrets Manager

---

## 📊 API Endpoints

### ProjectApiController - Main REST API

**Base URL**: `/api/projects`

#### Project Management
```
POST   /api/projects                 # Create new project
GET    /api/projects                 # List all projects
GET    /api/projects/{id}            # Get project details
PUT    /api/projects/{id}            # Update project
DELETE /api/projects/{id}            # Delete project
```

#### Source File Operations
```
GET    /api/projects/{id}/sources    # List source files
POST   /api/projects/{id}/sources    # Upload source files
DELETE /api/projects/{id}/sources/{fileId}  # Remove source file
```

#### Test Generation
```
POST   /api/projects/{id}/generate   # Generate unit tests
GET    /api/projects/{id}/results    # Get generated tests
POST   /api/projects/{id}/results/export  # Export as ZIP/JAR
```

#### History & Results
```
GET    /api/projects/{id}/history    # View generation history
GET    /api/results/{resultId}       # Get specific result
PUT    /api/results/{resultId}       # Update/refine result
```

#### Async Operations (AWS Lambda)
```
POST   /api/projects/{id}/import     # Async project import
GET    /api/tasks/{taskId}/status    # Check async task status
```

---

## 🏃 Getting Started

### Prerequisites
- **Java 17+** installed
- **Maven 3.8+** installed
- **PostgreSQL 14+** with pgVector extension enabled
- **AWS Account** with configured credentials
- **Git** for repository integration

### Environment Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/ngokyhod/ZeroBug-Agent.git
cd ZeroBug-Agent
```

#### 2. Configure AWS Credentials
```bash
# Using AWS CLI
aws configure

# Or set environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

#### 3. Application Configuration
Create `src/main/resources/application-aws.yml`:

```yaml
spring:
  application:
    name: zerobug-agent
  datasource:
    url: jdbc:postgresql://your-rds-endpoint:5432/zerobug_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL10Dialect
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://cognito-idp.${AWS_REGION}.amazonaws.com/${COGNITO_USER_POOL_ID}

aws:
  region: us-east-1
  s3:
    bucket: your-zerobug-bucket
  bedrock:
    model-id: anthropic.claude-3-sonnet-20240229-v1:0
  secrets-manager:
    secret-name: zerobug/api-keys
```

#### 4. Database Setup
```bash
# Connect to PostgreSQL
psql -h your-rds-endpoint -U postgres

# Create database and extensions
CREATE DATABASE zerobug_db;
\c zerobug_db
CREATE EXTENSION vector;

# Run Hibernate DDL
# Migrations are handled by Spring Data JPA with ddl-auto: create
```

#### 5. Build the Application
```bash
# Using Maven wrapper (no Maven installation required)
./mvnw clean install

# Or with installed Maven
mvn clean install
```

#### 6. Run the Application
```bash
# Development mode with Spring Boot plugin
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=aws"

# Production: Build JAR and run
./mvnw clean package -DskipTests
java -jar target/zerobug-agent-app-1.0.0.jar --spring.profiles.active=aws
```

#### 7. Verify Startup
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health
```

---

## 🔌 Backend & Frontend Separation

### Architecture Benefits

The ZeroBug Agent separates Frontend and Backend into **independent services**:

#### **Backend (This Repository)**
- **Role**: REST API Server
- **Technology**: Spring Boot 3.2.5 + Java 17
- **Port**: `8080` (configurable)
- **Deployment**: AWS EC2 / ECS
- **Responsibilities**:
  - Handle all business logic
  - Manage database operations
  - Integrate with AWS services (Bedrock, S3, Lambda)
  - Provide RESTful APIs via **ProjectApiController**
  - Process AI model invocations

#### **Frontend (Separate Repository/Module)**n- **Role**: User Interface
- **Technology**: React/Vue.js + JavaScript/TypeScript
- **Port**: `3000` (configurable)
- **Deployment**: AWS S3 + CloudFront
- **Responsibilities**:
  - Provide user interface
  - Display generated tests
  - Handle user interactions
  - Call backend APIs

### Communication Flow

```
┌──────────────┐
│   Frontend   │
│   (React)    │
└──────┬───────┘
       │ HTTP/REST via CORS
       │ JSON payloads
       │
       ▼
┌──────────────────────────────────┐
│   ProjectApiController (REST)    │
│   Endpoints: /api/projects/*     │
└──────────────────────────────────┘
       │
       ├─► Database (PostgreSQL + pgVector)
       ├─► AWS S3 (Code storage)
       ├─► Bedrock Runtime (AI inference)
       └─► Lambda (Async processing)
```

---

## 📚 Key Components

### ProjectApiController
The main REST API controller handling all HTTP requests:
- Route pattern: `/api/projects/*`
- Manages project lifecycle
- Orchestrates service calls
- Handles async operations via AWS Lambda

### AIInvokeService
Manages integration with Amazon Bedrock:
```java
// Example usage
public class AIInvokeService {
    public String generateTestsWithBedrock(String sourceCode) {
        // Sends code to Claude 3 Sonnet via Bedrock
        // Returns generated test code
    }
}
```

### SourceFileService
Handles source code management:
- Parse Java files
- Extract metadata
- Store in database
- Upload to S3

### ResultService
Manages generated test results:
- Store test results
- Version control
- Export capabilities (ZIP/JAR/PDF)

### HistoryService
Tracks project history:
- Generation history
- User actions
- Audit logs

---

## 🔒 Security Considerations

### Authentication & Authorization
- **JWT tokens** via AWS Cognito
- Role-based access control (RBAC)
- Scope-based API permissions

### Data Protection
- Credentials stored in **AWS Secrets Manager**
- S3 bucket encryption at rest
- Database encryption via RDS
- TLS/SSL for all communications

### Code Safety
- Input validation on all endpoints
- SQL injection prevention via JPA
- CORS configuration for frontend origin
- Rate limiting on API endpoints

---

## 🚢 Deployment Guide

### AWS EC2 Deployment

```bash
# 1. SSH into EC2 instance
ssh -i your-key.pem ec2-user@your-ec2-ip

# 2. Install Java 17
sudo yum install java-17-amazon-corretto

# 3. Upload JAR file
scp -i your-key.pem target/zerobug-agent-app-1.0.0.jar ec2-user@your-ec2-ip:/opt/app/

# 4. Create systemd service file
sudo cat > /etc/systemd/system/zerobug-agent.service << EOF
[Unit]
Description=ZeroBug Agent Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/app
ExecStart=java -jar zerobug-agent-app-1.0.0.jar --spring.profiles.active=aws
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

# 5. Start service
sudo systemctl daemon-reload
sudo systemctl enable zerobug-agent
sudo systemctl start zerobug-agent
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/zerobug-agent-app-1.0.0.jar .
EXPOSE 8080
CMD ["java", "-jar", "zerobug-agent-app-1.0.0.jar", "--spring.profiles.active=aws"]
```

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ProjectApiControllerTest

# Generate coverage report
./mvnw test jacoco:report
```

---

## 📝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

This project is proprietary and closed-source. All rights reserved.

---

## 🤝 Support & Contact

- **Author**: @ngokyhod
- **Repository**: [ngokyhod/ZeroBug-Agent](https://github.com/ngokyhod/ZeroBug-Agent)
- **Workshop**: [FCAJ Workshop](https://ngokyhod.github.io/FCAJ-Workshop/)
- **Demo Video**: https://youtu.be/aVMADaZOKDU

---

## 🎯 Roadmap

- [ ] Support for other JVM languages (Kotlin, Scala)
- [ ] Multiple AI model providers (GPT-4, Gemini)
- [ ] Test coverage analytics dashboard
- [ ] IDE plugin integrations (IntelliJ, VS Code)
- [ ] CI/CD pipeline integration
- [ ] Test result quality scoring
- [ ] Automated test maintenance

---

**Last Updated**: 2026-07-22 | **Status**: 🟢 Active Development
