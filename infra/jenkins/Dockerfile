# Jenkins controller for the CI VM. Pinned LTS on JDK 21, multi-arch (runs on the ARM64 VM).
FROM jenkins/jenkins:2.580.2-lts-jdk21

USER root

# Docker CLI and compose plugin only: the pipeline builds and pushes images through the host
# Docker daemon (socket mounted in docker-compose.yml), there is no daemon inside this container.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl \
    && install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        > /etc/apt/sources.list.d/docker.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends docker-ce-cli docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

# Give the jenkins user access to the host Docker socket: its group id must match the host's docker group.
ARG DOCKER_GID
RUN test -n "${DOCKER_GID}" \
    && (getent group "${DOCKER_GID}" || groupadd --gid "${DOCKER_GID}" docker-host) \
    && usermod -aG "$(getent group "${DOCKER_GID}" | cut -d: -f1)" jenkins

USER jenkins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
