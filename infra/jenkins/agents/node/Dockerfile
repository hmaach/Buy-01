# Container for the frontend test stage. Built by the pipeline on the agent (agent { dockerfile }).
# Google Chrome has no Linux ARM64 build, so the tests use Debian's Chromium, which does.
FROM node:22.23.5-bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends chromium \
    && rm -rf /var/lib/apt/lists/*

# Read by karma-chrome-launcher to find the browser
ENV CHROME_BIN=/usr/bin/chromium
