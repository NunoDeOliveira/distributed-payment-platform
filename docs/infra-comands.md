
## AWS and K3s Deployment Guide

This document contains the operational commands for provisioning, inspecting,scaling and destroying the AWS/K3s.

### Prerequisites

- AWS account and configured credentials.

- Terraform.

- An existing EC2 key pair.

- The SSH private key for the control-plane instance.

**1. Provision the infrastructure**

```bash
./scripts/deploy-aws.sh
```

**2. Destroy the AWS infrastructure**

```bash
terraform destroy
```


## Kubernetes K3s commands

**1. Verify the deployment**

```bash
sudo k3s kubectl get nodes
sudo k3s kubectl get deployments
sudo k3s kubectl get pods
sudo k3s kubectl get services
```

**2. Scaling each microservice to 3 replicas to validate concurrent request handling across distributed instances.**

```bash
sudo k3s kubectl scale deployment payment-service --replicas=3 &&
sudo k3s kubectl scale deployment commission-service --replicas=3 &&
sudo k3s kubectl scale deployment account-service --replicas=3 &&
sudo k3s kubectl scale deployment ledger-service --replicas=3
```

**3. Scaling each microservice to 3 replicas to validate concurrent request handling across distributed instances.**

```bash
sudo k3s kubectl scale deployment payment-service --replicas=3 &&
sudo k3s kubectl scale deployment commission-service --replicas=3 &&
sudo k3s kubectl scale deployment account-service --replicas=3 &&
sudo k3s kubectl scale deployment ledger-service --replicas=3
```


## AWS access by SSH

**4. Access the cluster**

```bash
ssh -i YOUR_KEY.pem ubuntu@<CONTROL_PLANE_IP>
```

## Destroy the Infrastructure

Run from local

```bash
terraform destroy
```

### References

Terraform destroy: 
Kubernetes kubectl get: 
