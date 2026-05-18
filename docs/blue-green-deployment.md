# Blue/Green Deployment

This project is prepared for a simple Nginx + Spring Boot blue/green deployment.

## Runtime Shape

```text
User -> Nginx :80 -> Spring Boot active port

blue  = 127.0.0.1:8081
green = 127.0.0.1:8082
```

Spring Boot still runs with embedded Tomcat. Nginx only decides which running app receives traffic.

## Health Check

Actuator exposes the health endpoint:

```text
/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## Manual Deployment Flow

1. Current traffic goes to blue.

```text
Nginx -> 127.0.0.1:8081
```

2. Start the new version on green.

```bash
java -jar ronnefeldt.jar --server.port=8082
```

3. Check the new version.

```bash
curl http://127.0.0.1:8082/actuator/health
```

4. If health is `UP`, switch Nginx to green.

```nginx
proxy_pass http://127.0.0.1:8082;
```

5. Reload Nginx.

```bash
sudo nginx -s reload
```

6. If a problem appears, rollback by switching Nginx back to blue.

```nginx
proxy_pass http://127.0.0.1:8081;
```

Then reload Nginx again.

## Portfolio Summary

The service uses Spring Boot Actuator health checks and an Nginx reverse proxy to support blue/green deployment. Rollback is handled by switching Nginx traffic back to the previous healthy port.
