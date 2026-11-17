# Jenkins controller. Pinned LTS on JDK 21, multi-arch image (runs on the ARM64 VM).
# The controller never builds anything: it has no Docker CLI and no Docker socket.
# Builds run on the SSH agent described in infra/README.md.
FROM jenkins/jenkins:2.580.2-lts-jdk21

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
