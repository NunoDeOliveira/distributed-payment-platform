
## Container Image Verification and Troubleshooting


**1. Show the name, tag, and download policy defined in the manifests.**

```bash
rg -n "image:|imagePullPolicy:" kubernetes-k3s/
```


**2. Build local images and create new container**

```bash
docker compose up -d --build account-service  
```

**3. Check if the container is running or has failed.**

```bash
docker compose ps account-service
```

**4. Show the last errors logs generate during the start-up**

```bash
docker compose logs --tail=100 account-service
```

**5. Check the pods of kubernetes. Errors such as ImagePullBackOff or CrashLoopBackOff**

```bash
kubectl get pods -A -o wide
```

**6. Show the image deploy in Kuberntes. Name, tag**

```bash
kubectl get deployment account-service -n default -o jsonpath='{.spec.template.spec.containers[*].image}{"\n"}'
```

**7. When the pod is CrashLoopBackOff, show the errors of Spring Boot**

```bash
kubectl logs <name-pod> -n default --tail=100
```

**8. When the pod is rebbot, this command shows the logs of last execution**

```bash
kubectl logs <name-pod> -n default --previous --tail=100
```

**9. Show the last dowload, creation and execution of pod**

```bash
kubectl get events -n default --sort-by='.lastTimestamp'
```
---

After modify java code

```bash
docker compose up -d --build account-service
docker compose ps account-service
docker compose logs --tail=100 account-service
```

If the problem is in Kubernetes

```bash
kubectl get pods -A -o wide
kubectl describe pod <name-pod> -n default
kubectl logs <name-pod> -n default --tail=100
kubectl get events -n default --sort-by='.lastTimestamp'
```























