#!/usr/bin/env bash
# Every deploy adds 6 images to a small disk. Keep the newest KEEP_TAGS tags
# (the live one plus rollback targets) and delete older ones. Never deletes the running tag.
set -euo pipefail

KEEP_TAGS="${KEEP_TAGS:-3}"
SERVICES="discovery-server user-service product-service media-service api-gateway frontend"
running=$(scripts/ci/running-tag.sh)

# docker images lists the newest first
old_tags=$(docker images buy01/api-gateway --format '{{.Tag}}' | tail -n +"$((KEEP_TAGS + 1))")

for tag in $old_tags; do
  if [ "$tag" = "$running" ]; then
    continue
  fi
  echo "Removing images tagged $tag"
  for service in $SERVICES; do
    docker rmi "buy01/$service:$tag" > /dev/null 2>&1 || true
  done
done

# Layers left without a tag by rebuilds
docker image prune -f > /dev/null
