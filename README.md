# Microservices: Saga Choreography

## Overview
This microservice is a prototype designed to efficiently and scalably manage the inventory of a distributed warehouse. Its main objective is to demonstrate the viability of the Saga pattern (Choreography) for maintaining eventual consistency in distributed transactions without requiring a central orchestrator.

The system is based on independent services built with Spring Boot that communicate asynchronously via a message broker (RabbitMQ), ensuring temporal and spatial decoupling.


## System Components

| Component          | Technology         | Role                                   | Port / Note                          |
|--------------------|--------------------|----------------------------------------|--------------------------------------|
| API Gateway        | Spring Cloud       | Entry point / routing                  | 8080                                |
| Production Service | Spring Boot        | Production management                  | 8081                                |
| Delivery Service   | Spring Boot        | Delivery lifecycle                     | 8082                                |
| Inventory Service  | Spring Boot        | Stock & reservation (event-driven)     | Messaging only                       |
| Messaging Broker   | RabbitMQ           | Asynchronous communication             | 5672 / 15672                       |
| Database           | PostgreSQL         | Persistent storage                     | 5432                                |


### Communication Model

#### Synchronous (External Access)
- User → API Gateway → Production Service
- User → API Gateway → Delivery Service
- Protocol: HTTP/REST

#### Asynchronous (Internal Communication)
- Production Service → RabbitMQ → Inventory Service
- Delivey Service → RabbitMQ → Inventory Service
- Protocol: Advanced Message Queuing Protocol (AMQP)



## Technologies

- **Spring Boot**: Microservice development framework
- **Spring Cloud Gateway**: API Gateway and request routing
- **Spring Web (REST)**: HTTP communication between APIs and microservices
- **Spring Data JPA**: Data persistence layer
- **RabbitMQ**: For asynchronous event-driven communication
- **PostgreSQL**: Relational database
- **Maven**: Build and dependency management
- **Java**: Programming language
###### Update
##############Update
