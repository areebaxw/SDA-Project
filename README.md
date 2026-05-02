# AWS Cloud Governance Tool

A comprehensive JavaFX-based desktop application for managing and monitoring AWS cloud resources with built-in cost tracking, idle resource detection, and alerting capabilities.

## Tech Stack

- **Java 17** - Core application logic
- **JavaFX 17** - Modern UI framework with external CSS styling
- **MySQL (XAMPP)** - Database for user data and AWS credentials
- **AWS SDK v2** - Integration with AWS services
- **Maven** - Build and dependency management
- **JUnit 5 + Mockito** - Testing framework with JaCoCo coverage

## How to Run

### Prerequisites
- Java 17 installed
- Maven installed
- MySQL (XAMPP) running on localhost:3306

### Database Setup
1. Start MySQL service via XAMPP
2. Import the database schema:
   ```sql
   SOURCE database_schema_sprint1.sql;
   ```
3. Default admin account (auto-created):
   - Username: `admin`
   - Password: `admin123`

### Configure Database Connection
If your MySQL password is not empty, edit `src/database/DBConnection.java`:
```java
private static final String PASSWORD = "your_password_here";
```

### Run the Application
```bash
mvn clean javafx:run
```

Or build and run as JAR:
```bash
mvn clean package
java -jar target/aws-governance-1.0.0.jar
```

## Key Features

### User Management
- User registration and authentication
- Secure password hashing
- Session-based login/logout

### AWS Integration
- Secure storage of AWS credentials (encrypted)
- Multi-region support
- Credential validation and testing

### Dashboard & Monitoring
- Real-time cost dashboard
- Resource count tracking (EC2, RDS, ECS, SageMaker)
- Visual statistics and status indicators

### Design Patterns
- **Singleton** - Database connection, AWS client factory
- **Factory** - AWS client creation
- **Observer** - Alert service for notifications
- **Strategy** - Idle detection algorithms

### Architecture
- MVC pattern with Controllers, Services, and DAOs
- Separation of concerns across packages (models, dao, controllers, services, aws, utils, database)
- External CSS styling for consistent UI
- Comprehensive test coverage with JUnit 5 and Mockito
