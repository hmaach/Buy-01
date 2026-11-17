#!/usr/bin/env bash
# Builds the app images for one commit, tagged buy01/<service>:<IMAGE_TAG>.
# One service at a time: parallel Maven builds run the 6 GB VM out of memory.
# Called by the Jenkinsfile with IMAGE_TAG (commit SHA) and APP_ENV_FILE (Jenkins secret file).
set -euo pipefail

: "${IMAGE_TAG:?IMAGE_TAG is required}"
: "${APP_ENV_FILE:?APP_ENV_FILE is required}"

SERVICES="discovery-server user-service product-service media-service api-gateway frontend"

for service in $SERVICES; do
  echo "Building buy01/$service:$IMAGE_TAG"
  docker compose --env-file "$APP_ENV_FILE" build "$service"
done

# The VM is ARM64: refuse to deploy an image built for another architecture.
for service in $SERVICES; do
  arch=$(docker image inspect --format '{{.Architecture}}' "buy01/$service:$IMAGE_TAG")
  echo "buy01/$service:$IMAGE_TAG is built for $arch"
  if [ "$arch" != "arm64" ]; then
    echo "Expected arm64" >&2
    exit 1
  fi
done
