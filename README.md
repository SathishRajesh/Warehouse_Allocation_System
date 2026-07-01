# Warehouse Allocation System

A Spring Boot application developed to manage product inventory and warehouse stock allocation. This project allows users to manage warehouses, products, inventory, stock allocation, and stock transfers efficiently.

## Technologies Used

- Java 17
- Spring Boot 3.3.0
- Spring Data JPA
- MySQL 8
- Maven
- Lombok
- Swagger UI
- JUnit 5
- Mockito

## Features

- Warehouse Management
- Product Management
- Inventory Management
- Stock Allocation
- Stock Transfer Between Warehouses
- Input Validation
- Exception Handling
- RESTful APIs
- Unit Testing
- API Documentation using Swagger

## Prerequisites

- Java 17 or above
- MySQL 8
- Maven

## Project Setup

### Clone the Repository

```bash
git clone:https://github.com/SathishRajesh/Warehouse_Allocation_System.git
```

### Create Database

```sql
CREATE DATABASE warehouse_db;
```

### Configure Database

Update the `application.properties` file.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/warehouse_db
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### Run the Application

```bash
mvn spring-boot:run
```

The application will start on:

```
http://localhost:8080
```

## Swagger Documentation

```
http://localhost:8080/swagger-ui/index.html
```

## Testing

Run the unit tests using:

```bash
mvn test
```

### Unit Test Coverage

- Warehouse Service Tests
- Product Service Tests
- Allocation Service Tests
- Stock Transfer Service Tests

Total Unit Tests: **46**

## Project Structure

```
src
├── controller
├── service
├── repository
├── entity
├── dto
├── exception
├── config
└── test
```
