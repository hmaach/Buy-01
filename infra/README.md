# CI infrastructure

Everything that runs on the CI VM: Jenkins now, then the reverse proxy, SonarQube (Phase 2) and Nexus (Phase 4). The app stack stays in the root `docker-compose.yml` on the app VM. Keeping them apart means a deploy of the app never restarts the Jenkins that runs it, and a heavy build never slows the live site.

| Folder | What | Status |
|---|---|---|
| `jenkins/` | Jenkins image (pinned LTS, JDK 21, Docker CLI, plugins) and its Configuration as Code | Phase 1 |
| `proxy/` | Caddy reverse proxy with automatic Let's Encrypt TLS | Phase 1, next step |
| `sonarqube/` | SonarQube and its PostgreSQL | Phase 2 |
| `nexus/` | Nexus repository manager (Maven and Docker) | Phase 4 |

Jenkins is fully defined in git: the image (`jenkins/Dockerfile`, `jenkins/plugins.txt`) and its configuration (`jenkins/casc/jenkins.yaml`). Rebuilding the VM from zero gives the same Jenkins. Settings changed in the web UI are lost on restart, so change the YAML instead.

## Bring the VM up from zero

1. **Create the VM** in Oracle Cloud: `VM.Standard.A1.Flex`, 3 OCPU, 18 GB RAM, 120 GB boot volume, Ubuntu 24.04 (aarch64), same VCN and subnet as the app VM. Allow SSH from your IP only.

2. **Install Docker** (official repository):
   ```bash
   sudo apt-get update && sudo apt-get install -y ca-certificates curl
   sudo install -m 0755 -d /etc/apt/keyrings
   sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
   echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
     | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
   sudo apt-get update && sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
   sudo usermod -aG docker "$USER"     # log out and back in after this
   ```

3. **Get the repository**:
   ```bash
   git clone https://github.com/hmaach/Buy-01.git ~/buy-01 && cd ~/buy-01/infra
   ```

4. **Create `infra/.env`** from the example and fill every value:
   ```bash
   cp .env.example .env
   getent group docker | cut -d: -f3          # value for DOCKER_GID
   openssl rand -base64 24 | tr -d '/+='      # value for JENKINS_ADMIN_PASSWORD
   nano .env
   ```

5. **Build and start**:
   ```bash
   docker compose up -d --build --wait
   docker compose ps                          # jenkins must show (healthy)
   ```

6. **Open Jenkins.** Until the proxy is set up, Jenkins only listens on the VM's localhost. Tunnel to it from your machine:
   ```bash
   ssh -L 8080:127.0.0.1:8080 ubuntu@<ci-vm-ip>
   ```
   Then browse to http://localhost:8080 and log in with `JENKINS_ADMIN_ID` and `JENKINS_ADMIN_PASSWORD`. There is no setup wizard: Configuration as Code already created the admin and the security settings.

## Why the Docker socket is mounted

The pipeline builds the app images with `docker build`. Jenkins has the Docker CLI only, and talks to the host's Docker daemon through `/var/run/docker.sock`. The `jenkins` user is added to a group with the host docker group id (`DOCKER_GID`) so it can use the socket without running as root.

Trade-off to know for the audit: access to the Docker socket is equivalent to root on the CI VM. It is accepted here because the VM runs only CI tools, it is reachable only by SSH from one IP and later through the authenticated proxy, and the jobs only build this repository.
