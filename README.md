# Cloud-Native Distributed Payment Platform

[![CI](https://github.com/NunoDeOliveira/tfg-microservices-saga-pattern/actions/workflows/ci.yml/badge.svg?branch=digital-banking-platform)](https://github.com/NunoDeOliveira/tfg-microservices-saga-pattern/actions/workflows/ci.yml)

### Infrastructure and Deployment

![AWS](https://img.shields.io/badge/AWS-EC2-FF9900)
![Terraform](https://img.shields.io/badge/Terraform-Infrastructure%20as%20Code-7B42BC)
![K3s](https://img.shields.io/badge/Kubernetes-K3s-326CE5)
![Docker](https://img.shields.io/badge/Docker-Container%20Images-2496ED)
[![CI](https://github.com/NunoDeOliveira/tfg-microservices-saga-pattern/actions/workflows/ci.yml/badge.svg?branch=digital-banking-platform)](https://github.com/NunoDeOliveira/tfg-microservices-saga-pattern/actions/workflows/ci.yml)

### Application Stack

![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Event%20Messaging-FF6600)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Persistence-4169E1)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Microservices-6DB33F)
![Java](https://img.shields.io/badge/Java-17-007396)


## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Domain Behavior](#domain-behavior)
   - [Events and Transactions](#events-and-transactions)
     - [Application Transactions](#application-transactions)
     - [Events Published and Consumed](#events-published-and-consumed)
   - [Happy Path Flow](#happy-path-flow)
   - [Cancellation Flow](#cancellation-flow)
4. [Technology Stack](#technology-stack)
5. [Infrastructure Design](#infrastructure-design)
   - [AWS Network and Compute](#aws-network-and-compute)
   - [Kubernetes (K3s)](#kubernetes-k3s)
6. [Deployment of application](#deployment)
   - [Docker](#docker)
   - [CI/CD with GitHub Actions](#cicd-with-github-actions)



## Overview

Traditional banking systems can have difficulties when they need to coordinate operations between independent services without using one central database. This project shows how distributed transactions can be managed with the Saga choreography pattern. Each service completes its own local transaction and publishes an event that starts the next step, without using a central coordinator.

---


## Architecture of Application

The application consists of an API Gateway and four independent microservices. The services communicate asynchronously through RabbitMQ, and each microservice owns its own PostgreSQL database.

![microservices](docs/microservices-diagram.png)

##### *Logical architecture of the application and communication between the Saga participants.*

- **API Gateway**: entry point for all HTTP requests. Routes operations to the appropriate service.
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
| Payment Service | `createPayment()` | `cancelPayment()` |
| Commission Service | `calculateCommission()` | `cancelCommission()` |
| Account Service | `reserveAmount()` | `cancelReservationAmount()` |
| Ledger Service | `recordMovement()` | `cancelMovement()` |


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

####      *Successful Saga flow, from payment creation to movement recorded.*


### Cancellation Flow

If one of the services rejects the operation or reports an error, the cancellation flow starts. The participating services execute compensating transactions to release reserved resources and restore a consistent state.

![Cancellation Flow Diagram](docs/cancelation-diagram.png)

####               *Cancellation flow and compensating transactions*



## Technology Stack

| Area | Technologies |
|---|---|
| Backend | Spring Boot, Spring Cloud Gateway |
| Persistence | PostgreSQL, Spring Data JPA |
| Communication | RabbitMQ, HTTP |
| Infrastructure cloud-native | AWS EC2, Kubernetes (K3s), Docker |
| For Deployment | Terraform (IaC), GitHub Actions (CI/CD) |



## Design of Infraestructure

The infrastructure runs in the AWS `eu-west-2` region and is created with Terraform. It includes a network, three EC2 instances, and a K3s cluster distributed across three Availability Zones.

![Infrastructure](docs/infrastructure-diagram.png)

*AWS infrastructure and Kubernetes K3s cluster where Application is deployment.*


### AWS Network and Compute

- **VPC**: all infrastructure resources are placed inside a `10.0.0.0/16` Virtual Private Cloud.

- **Subnets**: the VPC has one public subnet for the K3s server and two private subnets for the worker nodes. Each subnet is located in a different Availability Zone.

- **Internet connectivity**: the Internet Gateway connects the public subnet to the Internet. The NAT Gateway allows the private worker nodes to access the Internet without accepting direct connections from outside the VPC.

- **EC2 instances**: one EC2 instance runs the K3s control plane, and two EC2 instances work as worker nodes.

- **Security groups**: security groups control access to SSH, the K3s API, the application `NodePort`, PostgreSQL, and communication between the cluster nodes.

### Kubernetes (K3s)

- **Control plane**: the K3s server runs the Kubernetes API Server, Scheduler, Controller Manager, and SQLite datastore.

- **Worker nodes**: the two K3s agents run the application Pods selected by the Kubernetes Scheduler.

- **Application workloads**: the API Gateway, microservices, RabbitMQ, and PostgreSQL databases run as Kubernetes workloads inside the cluster.

- **Application access**: external test requests reach the API Gateway through a Kubernetes `NodePort` service on port `30000`. Internal Kubernetes Services allow the application components to communicate with each other.
  


## Deployment of application

Each microservice is packaged as a Docker image. GitHub Actions automates the build and publication of the images and applies the Kubernetes manifests to the K3s cluster.

### Docker

Each service has its own Docker image containing the application and its runtime dependencies.

### CI/CD with GitHub Actions

GitHub Actions builds and publishes the Docker images and deploys the Kubernetes resources required by the application.


  
## Getting Started
## Local Test Evidence
## Design Decisions



