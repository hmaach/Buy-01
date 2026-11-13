# Configuration

## One root .env per environment

All configuration comes from one file: `.env` at the repository root. Each environment has its own copy (your laptop, the app VM, the CI agent) and it is never committed (`.gitignore`).

`docker compose` reads `.env` automatically and passes each value to the services through the explicit `environment:` block of every service in `docker-compose.yml`. Each service receives only the variables it needs, and the compose file shows exactly which ones. There is no `env_file:` and no per-service `.env`.

Inside a service, `application.yaml` reads the values as `${VARIABLE}`. Secrets (Mongo users and passwords, JWT keys, `SSL_PASSWORD`) have no default value, so a missing secret stops the service at startup instead of falling back to a known value.

## .env.example is the contract

`.env.example` lists every variable used by `docker-compose.yml` and by every `application.yaml`. Secrets are empty placeholders, with a comment on how to generate them. Non-secret values (ports, database names, rate limits) hold working defaults.

Rules:
- Adding a `${VARIABLE}` to compose or to an `application.yaml` means adding it to `.env.example` in the same commit.
- Never put a real value in `.env.example`.

Create a new environment:
```bash
cp .env.example .env
make env    # writes a new JWT key pair (base64 DER) into .env
# then fill SSL_PASSWORD and the Mongo users and passwords, for example with:
openssl rand -base64 24 | tr -d '/+='
```

## Running one service locally without Docker

Spring does not read `.env` by itself. Export it into your shell first. `set -a` marks every variable defined afterwards for export, so the Java process inherits them:

```bash
set -a; source .env; set +a
cd backend && ./mvnw -B -pl user-service -am spring-boot:run
```

What the service still needs:
- Its infrastructure reachable from your machine. `docker-compose.yml` does not publish the Mongo, Kafka or Eureka ports, so publish them with a local `docker-compose.override.yml` (not committed).
- The Kafka name resolvable from your machine. Kafka tells every client to reconnect to the address it advertises, `kafka:9092` (the `KAFKA_BROKER` value), so add `127.0.0.1 kafka` to `/etc/hosts` once. `EUREKA_URL` and the `*_MONGO_HOST` variables in `.env.example` already point to `localhost`.
