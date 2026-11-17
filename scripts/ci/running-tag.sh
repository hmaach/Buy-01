#!/usr/bin/env bash
# Prints the image tag the live stack runs right now, or nothing if this pipeline never deployed it.
# Read before a deploy, it is the rollback target: the last version that was running.
set -euo pipefail

image=$(docker inspect --format '{{.Config.Image}}' api-gateway 2>/dev/null || true)

case "$image" in
  buy01/api-gateway:*) echo "${image#buy01/api-gateway:}" ;;
  *) echo "" ;;
esac
