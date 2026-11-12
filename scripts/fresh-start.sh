#!/usr/bin/env bash
# Wipe the app stack (containers, images, volumes, uploaded files, build cache),
# then rebuild every image from scratch and start the stack.
# Run from anywhere, as root: sudo ./scripts/fresh-start.sh [--yes]

set -euo pipefail

cd "$(dirname "$0")/.."

# Services that Docker builds from our code. Built one at a time:
# parallel Maven builds run the 1 OCPU / 6 GB app VM out of memory.
BUILT_SERVICES="discovery-server user-service product-service media-service api-gateway frontend"

# Containers run as this uid (see backend/Dockerfile), so it must own the bind-mounted upload folders.
APP_UID=10001

if [ "$(id -u)" -ne 0 ]; then
    echo "Run as root: sudo $0 $*"
    exit 1
fi

# Fail early instead of after a 10-minute build.
for key in JWT_PUBLIC_KEY JWT_PRIVATE_KEY SSL_PASSWORD; do
    if ! grep -q "^${key}=." .env 2>/dev/null; then
        echo "Missing ${key} in .env (run 'make env' for the JWT keys)."
        exit 1
    fi
done

if [ "${1:-}" != "--yes" ]; then
    echo "This deletes ALL app data: Mongo volumes (users, products, media records),"
    echo "the Kafka volume, uploads/media, uploads/avatars, the app images and the build cache."
    read -r -p "Type 'wipe' to continue: " answer
    if [ "$answer" != "wipe" ]; then
        echo "Aborted."
        exit 1
    fi
fi

echo "==> Stopping the stack and removing its containers, volumes and images"
docker compose down --volumes --rmi local --remove-orphans

echo "==> Removing the Docker build cache"
docker builder prune --all --force

echo "==> Emptying the upload folders"
mkdir -p uploads/media uploads/avatars
find uploads/media uploads/avatars -mindepth 1 -delete
chown -R "$APP_UID:$APP_UID" uploads/media uploads/avatars

for service in $BUILT_SERVICES; do
    echo "==> Building $service"
    docker compose build --no-cache --pull "$service"
done

echo "==> Starting the stack (waits until every health check passes)"
if ! docker compose up -d --wait --wait-timeout 600; then
    echo "Stack did not become healthy. Status and recent logs:"
    docker compose ps
    docker compose logs --tail=30
    exit 1
fi

docker compose ps --format 'table {{.Name}}\t{{.Status}}'
echo "==> Fresh stack is up and healthy."
