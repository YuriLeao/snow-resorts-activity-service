# snow-resorts-activity-service

Activity microservice for Snow Resorts: descent tracking (start/points/finish), run
metrics (max/avg speed, distance, vertical drop, inclination, duration), history, map
replay and friend leaderboards. Publishes `RunCompletedEvent` to SNS (prod) or logs locally (dev).

- **Port:** 8085
- **DB schema:** `activity` (`runs`, `run_metrics`, `gps_points`)
- **Shared libs:** `com.snowresorts:security-lib` + `com.snowresorts:contracts` (from GitHub Packages)

## Build & test

Requires a `github` server credential in `~/.m2/settings.xml` (see
[`settings.xml.example`](settings.xml.example)) to resolve the shared libraries.

```bash
./mvnw clean verify
./mvnw spring-boot:run    # `local` profile against the local Docker stack
```

Bring up Postgres/Redis/MinIO from [`snow-resorts-infra`](https://github.com/yurileao/snow-resorts-infra) (`make dev`).

## CI/CD

See [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml). Requires repo secret
`AWS_DEPLOY_ROLE_ARN`.
