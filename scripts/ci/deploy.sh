#!/usr/bin/env bash
# Starts the app stack on the given image tag and waits until every container is healthy.
# The same script does the normal deploy (new tag) and the rollback (previous tag).
# Compose only recreates the containers whose image changed: MongoDB and Kafka keep running.
set -euo pipefail

TAG="${1:?usage: deploy.sh <image-tag>}"
: "${APP_ENV_FILE:?APP_ENV_FILE is required}"

echo "Deploying image tag $TAG"
# --wait fails if a container is unhealthy after the timeout. Spring services need up to 2 minutes each.
IMAGE_TAG="$TAG" docker compose --env-file "$APP_ENV_FILE" up -d --no-build --wait --wait-timeout 420
