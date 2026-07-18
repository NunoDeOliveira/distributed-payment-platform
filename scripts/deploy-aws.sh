#!/usr/bin/env bash

set -Eeuo pipefail

TERRAFORM_DIR="/home/vant/aws-k3s-cluster-terraform/terraform"
REPOSITORY="NunoDeOliveira/distributed-payment-platform"
BRANCH="digital-banking-platform"
WORKFLOW="deploy-k3s.yml"

SSH_USER="ubuntu"
SSH_KEY="/home/vant/.ssh/infra-k3s-aws.pem"

echo "==========================================="
echo "=== 1. Verify requirements ==="

command -v terraform >/dev/null || {
  echo "ERROR: Terraform is not installed"
  exit 1
}

command -v gh >/dev/null || {
  echo "ERROR: GitHub CLI is not installed"
  exit 1
}

command -v ssh >/dev/null || {
  echo "ERROR: SSH not available"
  exit 1
}

gh auth status >/dev/null || {
  echo "ERROR: GitHub CLI is not authenticated. Run: gh auth login"
  exit 1
}

test -f "$SSH_KEY" || {
  echo "ERROR: SSH key does not exist: $SSH_KEY"
  exit 1
}

test -d "$TERRAFORM_DIR" || {
  echo "ERROR: Incorrect Terraform: $TERRAFORM_DIR"
  exit 1
}

echo "=== Requirement verification completed ===="
echo ""


echo "========================================0======="
echo "=== 2. Creating AWS and K3s infraestructure ==="

cd "$TERRAFORM_DIR"
terraform init -input=false
terraform apply -auto-approve



echo "======================================"
echo "=== 3. Getting IP of control plane ==="

K3S_HOST=$(terraform output -raw control_plane_public_IP)

if [[ -z "$K3S_HOST" ]]; then
  echo "ERROR: Terraform has not returned control_plane_public_IP."
  exit 1
fi
echo "Control plane: $K3S_HOST"



echo "==================================="
echo "=== 4. Waiting K3s is working ==="

COUNTER=0

until (ssh -i "$SSH_KEY" -o BatchMode=yes -o StrictHostKeyChecking=accept-new -o ConnectTimeout=5 "$SSH_USER@$K3S_HOST" sudo k3s kubectl get nodes) >/dev/null 2>&1; do
  COUNTER=$((COUNTER + 1))
  if [[ $COUNTER -eq 10 ]]; then
    echo "ERROR: K3s not available after 10 attempts"
    exit 1
  fi
  echo "K3s not ready, attempt $COUNTER/10, retrying in 10s..."
  sleep 10
done
echo "K3s is working"



echo "=============================================="
echo "=== 5. Update K3S_HOST and GitHub Secrets ==="

gh secret set K3S_HOST --repo "$REPOSITORY" --body "$K3S_HOST"
echo "Secret K3S_HOST updated."



echo "========================================"
echo "=== 6. Runnig workflow of deployment ==="

PREVIOUS_RUN_ID=$(gh run list --repo "$REPOSITORY" --workflow "$WORKFLOW" --branch "$BRANCH" --limit 1 --json databaseId --jq '.[0].databaseId // empty')

gh workflow run "$WORKFLOW" --repo "$REPOSITORY" --ref "$BRANCH"

COUNTER=0
RUN_ID=""

while [[ $COUNTER -lt 15 ]]; do
  RUN_ID=$(gh run list --repo "$REPOSITORY" --workflow "$WORKFLOW" --branch "$BRANCH" --limit 1 --json databaseId --jq '.[0].databaseId // empty')

  if [[ -n "$RUN_ID" && "$RUN_ID" != "$PREVIOUS_RUN_ID" ]]; then
    echo "Workflow started. Run ID: $RUN_ID"
    break
  fi

  COUNTER=$((COUNTER + 1))
  echo "Waiting for GitHub Actions: $COUNTER/15"
  sleep 5
done

if [[ -z "$RUN_ID" || "$RUN_ID" == "$PREVIOUS_RUN_ID" ]]; then
  echo "ERROR: The new workflow run could not be identified."
  exit 1
fi

echo "Workflow starting. Run ID: $RUN_ID"



echo "============================================"
echo "=== 7. Waiting the result of deployment ==="

gh run watch "$RUN_ID" --repo "$REPOSITORY" --exit-status

echo
echo "=========================================="
echo " DEPLOYMENT SUCCESSFUL"
echo " Control plane public IP: $K3S_HOST"
echo "=========================================="
