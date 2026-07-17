#!/usr/bin/env bash

set -Eeuo pipefail

TERRAFORM_DIR="/home/vant/aws-k3s-cluster-terraform/terraform"
REPOSITORY="NunoDeOliveira/distributed-payment-platform"
BRANCH="digital-banking-platform"
WORKFLOW="deploy-k3s.yml"

SSH_USER="ubuntu"
SSH_KEY="/home/vant/.ssh/infra-k3s-aws.pem"

echo "=== 1. Comprobando requisitos ==="

command -v terraform >/dev/null || {
  echo "ERROR: Terraform no está instalado."
  exit 1
}

command -v gh >/dev/null || {
  echo "ERROR: GitHub CLI no está instalado."
  exit 1
}

command -v ssh >/dev/null || {
  echo "ERROR: SSH no está disponible."
  exit 1
}

gh auth status >/dev/null || {
  echo "ERROR: GitHub CLI no está autenticado. Ejecuta: gh auth login"
  exit 1
}

test -f "$SSH_KEY" || {
  echo "ERROR: No existe la clave SSH: $SSH_KEY"
  exit 1
}

test -d "$TERRAFORM_DIR" || {
  echo "ERROR: No existe el directorio Terraform: $TERRAFORM_DIR"
  exit 1
}

echo "=== 2. Creando infraestructura AWS y K3s ==="

cd "$TERRAFORM_DIR"

terraform init -input=false
terraform apply -auto-approve

echo "=== 3. Obteniendo IP del control plane ==="

K3S_HOST=$(terraform output -raw control_plane_public_IP)

if [[ -z "$K3S_HOST" ]]; then
  echo "ERROR: Terraform no devolvió control_plane_public_IP."
  exit 1
fi

echo "Control plane: $K3S_HOST"

echo "=== 4. Esperando a que K3s esté operativo ==="

K3S_READY=false

for attempt in $(seq 1 60); do
  echo "Intento $attempt/60..."

  if ssh \
    -i "$SSH_KEY" \
    -o BatchMode=yes \
    -o StrictHostKeyChecking=accept-new \
    -o ConnectTimeout=5 \
    "$SSH_USER@$K3S_HOST" \
    "sudo k3s kubectl get nodes >/dev/null 2>&1"
  then
    K3S_READY=true
    break
  fi

  sleep 10
done

if [[ "$K3S_READY" != "true" ]]; then
  echo "ERROR: K3s no estuvo disponible después de 10 minutos."
  exit 1
fi

echo "K3s está operativo."

echo "=== 5. Actualizando K3S_HOST en GitHub Secrets ==="

gh secret set K3S_HOST \
  --repo "$REPOSITORY" \
  --body "$K3S_HOST"

echo "Secret K3S_HOST actualizado."

echo "=== 6. Ejecutando workflow de deployment ==="

PREVIOUS_RUN_ID=$(
  gh run list \
    --repo "$REPOSITORY" \
    --workflow "$WORKFLOW" \
    --branch "$BRANCH" \
    --limit 1 \
    --json databaseId \
    --jq '.[0].databaseId // empty'
)

gh workflow run "$WORKFLOW" \
  --repo "$REPOSITORY" \
  --ref "$BRANCH"

RUN_ID=""

for attempt in $(seq 1 30); do
  RUN_ID=$(
    gh run list \
      --repo "$REPOSITORY" \
      --workflow "$WORKFLOW" \
      --branch "$BRANCH" \
      --limit 1 \
      --json databaseId \
      --jq '.[0].databaseId // empty'
  )

  if [[ -n "$RUN_ID" && "$RUN_ID" != "$PREVIOUS_RUN_ID" ]]; then
    break
  fi

  sleep 2
done

if [[ -z "$RUN_ID" || "$RUN_ID" == "$PREVIOUS_RUN_ID" ]]; then
  echo "ERROR: No se pudo identificar la nueva ejecución del workflow."
  exit 1
fi

echo "Workflow iniciado. Run ID: $RUN_ID"

echo "=== 7. Esperando el resultado del deployment ==="

gh run watch "$RUN_ID" \
  --repo "$REPOSITORY" \
  --exit-status

echo
echo "=========================================="
echo " DEPLOYMENT SUCCESSFUL"
echo " Control plane: $K3S_HOST"
echo "=========================================="
