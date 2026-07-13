# Distributed Payment Platform

### Tech Stack

![AWS](https://img.shields.io/badge/AWS-EC2-FF9900)
![Terraform](https://img.shields.io/badge/Terraform-Infrastructure%20as%20Code-7B42BC)
![K3s](https://img.shields.io/badge/Kubernetes-K3s-yellow)
![Docker](https://img.shields.io/badge/Docker-Containers-2496ED)
![CI/CD](https://img.shields.io/badge/CI/CD-GitHub%20Actions-black)

![RabbitMQ](https://img.shields.io/badge/RabbitMQ-AMQP-FF6600)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Persistence-4169E1)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-microservices-6DB33F)
![Java](https://img.shields.io/badge/Java-17-0074BD)


## Table of Contents

1. [Overview](#overview)
2. [Application Architecture](#architecture)
3. [Saga Behavior](#domain-behavior)
   - [Events and Transactions](#events-and-transactions)
   - [Happy Path: Successful Payment](#happy-path-flow)
   - [Cancellation and Compensation](#cancellation-flow)
4. [Design Decisions](#design-decisions)
5. [Technology Stack](#technology-stack)
6. [Infrastructure](#infrastructure-design)
7. [Deployment](#deployment)
8. [Getting Started](#getting-started)
9. [Testing and Validation](#testing-and-validation)
11. [Current Limitations](#current-limitations)
13. [Repository Structure](#repository-structure)


## Overview

Traditional banking systems can have difficulties when they need to coordinate operations between independent services without using one central database. This project shows how distributed transactions can be managed with the Saga choreography pattern. Each service completes its own local transaction and publishes an event that starts the next step, without using a central coordinator.

---

## 

The project studies how a distributed payment operation can be coordinated across independent services without using a shared database or a global transaction. The system implements Saga choreography and evaluates its functional behavior, temporary inconsistencies, concurrent execution, and performance.


## Application Architecture

The application consists of an API Gateway and four independent microservices. The services communicate asynchronously through RabbitMQ, and each microservice owns its own PostgreSQL database.

![microservices](docs/microservices-diagram.png)

##### *Logical architecture of the application and communication between the Saga participants.*

- **API Gateway**: entry point for all HTTP requests. Routes operations to the appropriate service.
- **Payment Service**: manages the type of payments and creates the payment and starts the Saga flow.
- **Commission Service**: calculates the commission according to the payment method and sends the total amount to the next service.
- **Account Service**: manages the account balance. It reserves the funds when a payment starts and restores them if the payment is cancelled.
- **Ledger Service**: stores a permanent record of all money movements and works as the accounting ledger of the system.
  
---
 
 
## Saga Behavior

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
| Infrastructure | AWS EC2, Kubernetes (K3s), Docker |
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

For deployment, each microservice is packaged as a Docker image. GitHub Actions automates the build and publication of the images and applies the Kubernetes manifests to the K3s cluster.

CI/CD For more detail, check this link --> 

### Docker

Each service has its own Docker image containing the application and its runtime dependencies.

### CI/CD with GitHub Actions

GitHub Actions builds and publishes the Docker images and deploys the Kubernetes resources required by the application.


  
## Getting Started

### Run locally

Clone the repository and start all services with Docker Compose:

```bash
git clone https://github.com/NunoDeOliveira/distributed-payment-platform
cd distributed-payment-platform
docker-compose up
```

Before making any payment, add an initial balance to the account:

```bash
curl -X POST "http://localhost:8083/balances/add?balanceAccount=1000.00"
```

Create a payment:

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "method": "INTERNATIONAL_TRANSFER"}'
```

Cancel a payment by its ID:

```bash
curl -X DELETE http://localhost:8080/payments/{id}
```


### Local Evidence


### Run in AWS


### AWS Evidence


## Design Decisions



## References

[1] C. Richardson, *Microservices Patterns: With Examples in Java*.
Shelter Island, NY, USA: Manning Publications, 2019.

[2] E. Daraghmi, C.-P. Zhang, and S.-M. Yuan, “Enhancing Saga Pattern
for Distributed Transactions within a Microservices Architecture,”
*Applied Sciences*, vol. 12, no. 12, Art. no. 6242, Jun. 2022,
doi: 10.3390/app12126242.

[3] GitHub, “Workflow syntax for GitHub Actions,” *GitHub Docs*.
[Online]. Available:
https://docs.github.com/actions/using-workflows/workflow-syntax-for-github-actions.
[Accessed: Jul. 13, 2026].

[4] GitHub, “Publishing Docker images,” *GitHub Docs*. [Online].
Available:
https://docs.github.com/actions/guides/publishing-docker-images.
[Accessed: Jul. 13, 2026].

[5] Apache Maven Project, “Maven Surefire Plugin.” [Online].
Available:
https://maven.apache.org/surefire/maven-surefire-plugin/.
[Accessed: Jul. 13, 2026].

[6] Kubernetes Authors, “Deployments,” *Kubernetes Documentation*.
[Online]. Available:
https://kubernetes.io/docs/concepts/workloads/controllers/deployment/.
[Accessed: Jul. 13, 2026].

[7] Kubernetes Authors, “kubectl port-forward,”
*Kubernetes Documentation*. [Online]. Available:
https://kubernetes.io/docs/reference/kubectl/generated/kubectl_port-forward/.
[Accessed: Jul. 13, 2026].

[8] Spring, “Actuator endpoints,” *Spring Boot Reference Documentation*.
[Online]. Available:
https://docs.spring.io/spring-boot/reference/actuator/endpoints.html.
[Accessed: Jul. 13, 2026].

[9] K3s Project, “Architecture,” *K3s Documentation*. [Online].
Available:
https://docs.k3s.io/architecture.
[Accessed: Jul. 13, 2026].

[10] HashiCorp, “What is Terraform?,” *Terraform Documentation*.
[Online]. Available:
https://developer.hashicorp.com/terraform/intro.
[Accessed: Jul. 13, 2026].

[11] Amazon Web Services, “What is Amazon VPC?,”
*Amazon VPC User Guide*. [Online]. Available:
https://docs.aws.amazon.com/vpc/latest/userguide/what-is-amazon-vpc.html.
[Accessed: Jul. 13, 2026].

[12] Amazon Web Services, “What is Amazon EC2?,”
*Amazon EC2 User Guide*. [Online]. Available:
https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/concepts.html.
[Accessed: Jul. 13, 2026].
