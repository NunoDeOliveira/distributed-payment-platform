# Production Service

This microservice is part of a distributed system implementing the Saga pattern.

## Description

The Production Service is responsible for managing production processes. PostgreSQL is used as the database for persistence.

The service handles the lifecycle of a production:

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



