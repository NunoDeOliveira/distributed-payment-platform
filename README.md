
============================================================================

# Distributed Payment Platform - Saga Choreography

## Indice
1. Overview
2. Architecture of Application
3. Behavior of Application
- Events and Transactions
- Application transactions
- Events published and consumed
- Happy Path Flow
- Cancellation Flow
4. Tech Stack
5. Design of Infraestructure
6. Deployment of microservices
7. Getting Started
    - Developer of aplication
    - Local tests
    - Implementing infrastructure in AWS
    - Deployment microservices
8. Observability and monitoring

---

## Overview

Traditional banking systems can have difficulties when they need to coordinate operations between independent services without using one central database. This project shows how distributed transactions can be managed with the Saga choreography pattern. Each service completes its own local transaction and publishes an event that starts the next step, without using a central coordinator.

---

## Architecture of Application

The application consists of an API Gateway and four independent microservices.
The services communicate asynchronously through RabbitMQ, and each microservice
owns its own PostgreSQL database.

![microservices](docs/microservices-diagram.png)

- **API Gateway**: entry point for all HTTP requests. Routes operations to the 
  appropriate service.
[api-gateway/README.md](api-gateway/README.md)
- **Payment Service**: manages the type of payments and creates the payment and starts the Saga flow.
[payment-service/README.md](payment-service/README.md)
- **Commission Service**: calculates the commission according to the payment method and sends the total amount to the next service.
[commission-service/README.md](commission-service/README.md)
- **Account Service**: manages the account balance. It reserves the funds when a payment starts and restores them if the payment is cancelled.
[account-service/README.md](account-service/README.md)
- **Ledger Service**: stores a permanent record of all money movements and works as the accounting ledger of the system.
[ledger-service/README.md](ledger-service/README.md)
  
  ---
  
## Behavior of Application

### Events and Transactions

The Saga is implemented as a sequence of local transactions and asynchronous events. Each service publishes the result of its transaction, while compensating transactions are used when the operation fails or is cancelled.

#### Application transactions

| Service | Local transaction | Compensation |
|---|---|---|
| Payment Service | `createPayment()` | `rejectPayment()` |
| Commission Service | `calculateCommission()` | `releaseCommission()` |
| Account Service | `reserveAmount()` | `releasedAmount()` |
| Ledger Service | `recordMovement()` | `releaseMovement()` |

#### Events published and consumed

| Service | Events published | Events consumed |
|---|---|---|
| Payment Service | `payment.created`<br>`operation.canceled` | `amount.deducted`<br>`operation.canceled` |
| Commission Service | `commission.calculated`<br>`commission.released`<br>`operation.canceled` | `payment.created`<br>`operation.rejected`<br>`operation.canceled` |
| Account Service | `amount.reserved`<br>`amount.deducted`<br>`amount.rejected` | `commission.calculated`<br>`movement.recorded`<br>`movement.rejected`<br>`operation.canceled` |
| Ledger Service | `movement.recorded`<br>`movement.rejected`<br>`operation.canceled` | `amount.reserved`<br>`amount.deducted`<br>`operation.canceled` |

### Happy Path Flow

In the successful flow, every service completes its local transaction without errors. Each published event starts the next step until the payment is completed and the account and ledger are updated.

![Happy Path Diagram](docs/happy-path-diagram.png)

### Cancellation Flow

If one of the services rejects the operation or reports an error, the cancellation flow starts. The participating services execute compensating transactions to release reserved resources and restore a consistent state.

![Cancellation Flow Diagram](docs/cancelation-diagram.png)

---
    
## Tech Stack

**Backend**
- **Spring Boot**: framework for building each microservice
- **Spring Cloud**: API Gateway 
- **Spring Data JPA + Hibernate**: ORM for database access and entity management
- **PostgreSQL**: relational database, one instance per service

**Messaging**
- **RabbitMQ**: message broker for event-driven communication between services

**Infrastructure**
- **AWS EC2**: virtual machines hosting the K3s cluster nodes
- **Kubernetes K3s**: lightweight Kubernetes distribution for container orchestration
- **Terraform**: infrastructure as code for provisioning AWS resources
- **Docker**: containerization of each microservice and its dependencies

**CI/CD**
- **GitHub Actions**: automated pipeline for building Docker images in Kubernetes K3s

---

## Design of Infraestructure

The application infrastructure is deployed in AWS. Terraform creates the required cloud resources, while K3s manages the application workloads inside the Kubernetes cluster.

![Infrastructure](docs/infrastructure-diagram.png)


 - **AWS and Terraform** — AWS provides the computing and networking resources required by the application. Terraform defines and creates these resources using infrastructure as code.

- **Kubernetes (K3s)** — K3s manages the microservices inside the cluster. It deploys the containers, maintains the required replicas, and provides internal communication between services.

---

## Deployment of microservices

The deployment process packages each microservice as a Docker image and uses GitHub Actions to automate the delivery of new application versions.

- **Docker** — each microservice is packaged as an independent Docker image, including the application and its required dependencies.

- **CI/CD with GitHub Actions** — GitHub Actions automates the build and publication of Docker images and deploys the Kubernetes configuration to the K3s cluster.

---
  
## Getting Started
## Local Test Evidence
## Design Decisions



