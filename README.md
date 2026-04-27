# Logistics Warehouse Microservices — Saga Choreography

## Overview
This microservice is a prototype designed to efficiently and scalably manage the inventory of a distributed warehouse. Its main objective is to demonstrate the viability of the Saga pattern (Choreography) for maintaining eventual consistency in distributed transactions without requiring a central orchestrator.

The system is based on independent services built with Spring Boot that communicate asynchronously via a message broker (RabbitMQ), ensuring temporal and spatial decoupling.


## Architecture

### Microservices
- **Production Service** → Handles Production creation and emits domain events.
- **Inventory Service** → Manages stock and reservations.
- **Delivery Service** → Handles shipment and delivery lifecycle.

Each service:
- Runs independently
- Has its own database
- Exposes a REST API
- Communicates via events (RabbitMQ)


### Communication Model

#### Synchronous (External Access)
- User → API Gateway → Production Service
- User → API Gateway → Delivery Service
- Protocol: HTTP/REST

#### Asynchronous (Internal Communication)
- Production Service → RabbitMQ → Inventory Service
- Delivey Service → RabbitMQ → Inventory Service
- Protocol: Advanced Message Queuing Protocol (AMQP)


## Saga Pattern (Choreography)

This system implements the **Saga pattern using choreography**, meaning:

- There is **no central coordinator**
- Each service reacts to events and triggers the next step


## Technologies

- **Spring Boot**: Microservice
- **Spring Cloud**: API Gateway
- **RabbitMQ**: Message broker for event-driven communication
- **REST APIs**: External communication
- **Maven**: Build tool
- **

## System Components

| Component          | Technology         | Role                                   | Port / Note                          |
|--------------------|--------------------|----------------------------------------|--------------------------------------|
| API Gateway        | Spring Cloud       | Entry point / routing                  | :8080                                |
| Production Service | Spring Boot        | Production management                  | :8081                                |
| Delivery Service   | Spring Boot        | Delivery lifecycle                     | :8082                                |
| Inventory Service  | Spring Boot        | Stock & reservation (event-driven)     | Messaging only                       |
| Messaging Broker   | RabbitMQ           | Asynchronous communication             | :5672 / :15672                       |
| Database           | PostgreSQL         | Persistent storage                     | :5432                                |

