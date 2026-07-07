# Digital Banking Platform: Saga Choreography

## Overview
This microservice is a prototype designed to efficiently and scalably manage the inventory of a distributed warehouse. Its main objective is to demonstrate the viability of the Saga pattern (Choreography) for maintaining eventual consistency in distributed transactions without requiring a central orchestrator.

The system is based on independent services built with Spring Boot that communicate asynchronously via a message broker (RabbitMQ), ensuring temporal and spatial decoupling.



**Digital Banking Platform** is a distributed microservices prototype that simulates the processing of a banking payment using the **Saga pattern with choreography**.

The main objective of the project is to demonstrate how a distributed transaction can be coordinated without a central orchestrator. Each microservice executes its own local transaction, persists its state independently, and communicates with the rest of the system through asynchronous events.

The system uses **Spring Boot**, **RabbitMQ** and **PostgreSQL** to implement an event-driven architecture with eventual consistency.

---

## Contexto del proyecto

La aplicación representa una operación bancaria distribuida compuesta por varios pasos independientes:

1. creación del pago,
2. cálculo de comisión,
3. reserva del saldo,
4. registro contable del movimiento,
5. confirmación final del pago.

Cada paso pertenece a un microservicio distinto. La consistencia global no se obtiene mediante una transacción distribuida, sino mediante la publicación y consumo de eventos entre servicios.

---

## System Components

| Component | Technology | Role | Port / Note |
|---|---|---|---|
| API Gateway | Spring Cloud Gateway | Entry point / request routing | 8080 |
| Payment Service | Spring Boot | Payment creation and final state management | 8081 |
| Commission Service | Spring Boot | Commission calculation and compensation | 8082 |
| Account Service | Spring Boot | Balance reservation, confirmation and release | 8083 |
| Ledger Service | Spring Boot | Ledger movement registration | 8084 |
| Messaging Broker | RabbitMQ | Asynchronous event communication | 5672 / 15672 |
| Database | PostgreSQL | Persistent storage per service | 5432 |

---

## Application Design

```mermaid
flowchart LR
    User[User] --> Gateway[API Gateway]
    Gateway --> Payment[Payment Service]

    Payment -->|payment.created| RabbitMQ[(RabbitMQ)]
    RabbitMQ -->|payment.created| Commission[Commission Service]

    Commission -->|commission.calculated| RabbitMQ
    RabbitMQ -->|commission.calculated| Account[Account Service]

    Account -->|amount.reserved| RabbitMQ
    RabbitMQ -->|amount.reserved| Ledger[Ledger Service]

    Ledger -->|movement.recorded| RabbitMQ
    RabbitMQ -->|movement.recorded| Account

    Account -->|amount.debited| RabbitMQ
    RabbitMQ -->|amount.debited| Payment

    Payment -->|COMPLETED| Completed[Payment Completed]

    PaymentDB[(paymentdb)]
    CommissionDB[(commissiondb)]
    AccountDB[(accountdb)]
    LedgerDB[(movementdb)]

    Payment --> PaymentDB
    Commission --> CommissionDB
    Account --> AccountDB
    Ledger --> LedgerDB
```

---

## Happy Path / Saga Flow

El flujo principal representa una operación bancaria completada correctamente:

1. El usuario crea un pago mediante `Payment Service`.
2. `Payment Service` registra el pago en estado `CREATED` y publica el evento `payment.created`.
3. `Commission Service` consume el evento, calcula la comisión y publica `commission.calculated`.
4. `Account Service` consume la comisión calculada, reserva el importe total y publica `amount.reserved`.
5. `Ledger Service` consume la reserva, registra el movimiento contable y publica `movement.recorded`.
6. `Account Service` consume la confirmación del movimiento, confirma el importe reservado y publica `amount.debited`.
7. `Payment Service` consume la confirmación final y actualiza el pago a `COMPLETED`.

Resumen del flujo:

```text
payment.created
→ commission.calculated
→ amount.reserved
→ movement.recorded
→ amount.debited
→ payment COMPLETED
```

---

## Transactions

| Service | Transaction | Compensation | Local Transactions |
|---|---|---|---|
| Payment Service | `createPayment()` | `rejectPayment()` | `cancelPayment()` |
| Commission Service | `calculateCommission()` | `releaseCommission()` | `cancelCommission()` |
| Account Service | `reserveAmount()` | `releaseAmount()` | `cancelReserveAmount()` |
| Ledger Service | `recordMovement()` | — | `cancelMovement()` |

---

## Events

| Service | Events published | Events consumed |
|---|---|---|
| Payment Service | `payment.created`<br>`payment.canceled` | `amount.debited`<br>`commission.released` |
| Commission Service | `commission.calculated`<br>`commission.released`<br>`operation.canceled` | `payment.created`<br>`amount.rejected`<br>`operation.canceled` |
| Account Service | `amount.reserved`<br>`amount.debited`<br>`amount.rejected`<br>`amount.released` | `commission.calculated`<br>`movement.recorded`<br>`movement.failed`<br>`payment.canceled` |
| Ledger Service | `movement.recorded`<br>`movement.failed` | `amount.reserved`<br>`operation.canceled` |

---

## Details of Each Microservice

| Service | Responsibility | Documentation |
|---|---|---|
| Payment Service | Creación y finalización del pago | [payment-service/README.md](payment-service/README.md) |
| Commission Service | Cálculo y compensación de comisiones | [commission-service/README.md](commission-service/README.md) |
| Account Service | Reserva, confirmación y liberación de saldo | [account-service/README.md](account-service/README.md) |
| Ledger Service | Registro del movimiento contable | [ledger-service/README.md](ledger-service/README.md) |
| API Gateway | Punto de entrada HTTP de la plataforma | [api-gateway/README.md](api-gateway/README.md) |

---

## Current Status

The current version supports the complete successful Saga flow in a local environment:

```text
Payment CREATED
→ Commission CALCULATED
→ Account RESERVED
→ Ledger RECORDED
→ Account CONFIRMED
→ Payment COMPLETED
```

The execution can be verified through service logs, RabbitMQ queues and PostgreSQL queries over the independent databases of each microservice.

---

## Technologies

- **Java**
- **Spring Boot**
- **Spring Cloud Gateway**
- **Spring Web / REST**
- **Spring Data JPA**
- **RabbitMQ**
- **PostgreSQL**
- **Maven**
- **Docker / Docker Compose**

---

## Next Steps

- Add evidence of the successful Saga execution.
- Document compensation scenarios.
- Add service failure tests.
- Integrate Prometheus and Grafana for monitoring.
- Prepare deployment in AWS.


## Technologies

- **Spring Boot**: Microservice development framework
- **Spring Cloud Gateway**: API Gateway and request routing
- **Spring Web (REST)**: HTTP communication between APIs and microservices
- **Spring Data JPA**: Data persistence layer
- **RabbitMQ**: For asynchronous event-driven communication
- **PostgreSQL**: Relational database
- **Maven**: Build and dependency management
- **Java**: Programming language

#
