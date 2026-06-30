# Payment Service

This microservice is part of a distributed system implementing the Saga pattern.

## Description

The Payment Service is responsible for managing payment processes. PostgreSQL is used as the database for persistence.

The service handles the lifecycle of a payment:

- PENDING
- PREPARING
- COMPLETED

## Tech Stack

- Java 24
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Docker

## Architecture

This service is based on this architecture:

  Controller → Service → Repository → Domain

## Endpoints



