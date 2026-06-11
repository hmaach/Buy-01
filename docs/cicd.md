# CI/CD Pipeline with Jenkins — Buy-01 E-Commerce Platform

## Overview

This document covers setting up a full CI/CD pipeline using Jenkins for the Buy-01 microservices platform. The pipeline covers: code checkout → build → test → deploy → notify → rollback.

The stack:
- **Backend**: 5 Spring Boot services (Java 21, Maven)
- **Frontend**: Angular 20
- **Infrastructure**: Docker Compose, Kafka, MongoDB x3
- **Jenkins**: already included in `docker-compose.yml` on port `8090`

---

## 1. Jenkins Setup

### 1.1 Start Jenkins

Jenkins is already defined in `docker-compose.yml`. Start it with:

```bash
make docker-up
# or just jenkins + network
docker compose up -d jenkins
```

Access Jenkins at: `http://localhost:8090`

### 1.2 Unlock Jenkins

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Paste the password in the browser, install **suggested plugins**, then create your admin user.

### 1.3 Install Required Plugins

Go to **Manage Jenkins → Plugins → Available plugins** and install:

| Plugin | Purpose |
|---|---|
| Git | Source code checkout |
| Pipeline | Declarative pipeline support |
| Docker Pipeline | Build/push Docker images |
| JUnit | Publish test results |
| Email Extension | Email notifications |
| Slack Notification | Slack notifications |
| Credentials Binding | Inject secrets safely |
| Blue Ocean (optional) | Better pipeline UI |

### 1.4 Configure Global Tools

Go to **Manage Jenkins → Tools**:

- **JDK**: Add JDK 21 (name: `JDK-21`)
- **Maven**: Auto-install Maven 3.9+ (name: `Maven-3.9`)
- **NodeJS**: Install Node 20+ (name: `Node-20`) — requires NodeJS plugin

### 1.5 Add Credentials

Go to **Manage Jenkins → Credentials → Global → Add Credentials**:

| ID | Type | Value |
|---|---|---|
| `github-credentials` | Username/Password or SSH key | Your GitHub credentials |
| `env-file` | Secret file | Your `.env` file |
| `slack-token` | Secret text | Slack Bot token |
| `email-config` | (via SMTP config) | SMTP credentials |

### 1.6 Configure Email Notifications

Go to **Manage Jenkins → System → Extended E-mail Notification**:

```
SMTP server: smtp.gmail.com
Port: 465
Use SSL: true
Credentials: your Gmail app password credential
Default recipient: team@example.com
```

### 1.7 Configure Slack Notifications

Go to **Manage Jenkins → System → Slack**:
- Workspace: your Slack workspace
- Credential: `slack-token`
- Default channel: `#ci-cd`

---

## 2. Jenkins Agent Setup (Optional but Recommended)

For distributed builds, set up a build agent on the host machine.

### On the host machine:

```bash
# Install Java 21
sudo apt install openjdk-21-jdk -y

# Create jenkins user
sudo useradd -m -s /bin/bash jenkins-agent
```

### In Jenkins:

Go to **Manage Jenkins → Nodes → New Node**:
- Name: `build-agent`
- Type: Permanent Agent
- Remote root: `/home/jenkins-agent`
- Launch via SSH
- Host: your server IP
- Credentials: SSH key pair

---

## 3. Repository Setup

### 3.1 Webhook (GitHub → Jenkins)

In your GitHub repository:
1. Go to **Settings → Webhooks → Add webhook**
2. Payload URL: `http://<your-server-ip>:8090/github-webhook/`
3. Content type: `application/json`
4. Events: **Just the push event**

> If Jenkins is on localhost, use [ngrok](https://ngrok.com) to expose it:
> ```bash
> ngrok http 8090
> ```

### 3.2 Jenkinsfile

Create a `Jenkinsfile` at the root of your repository:

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9'
        nodejs 'Node-20'
    }

    environment {
        SERVICES = 'api-gateway discovery-server media-service product-service user-service'
        COMPOSE_FILE = 'docker-compose.yml'
        ROLLBACK_TAG = ''
    }

    triggers {
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.ROLLBACK_TAG = sh(script: 'git rev-parse --short HEAD~1', returnStdout: true).trim()
                    env.CURRENT_TAG = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
            }
        }

        stage('Setup Environment') {
            steps {
                withCredentials([file(credentialsId: 'env-file', variable: 'ENV_FILE')]) {
                    sh 'cp $ENV_FILE .env'
                }
                sh 'make ssl'
            }
        }

        stage('Build Backend') {
            steps {
                sh '''
                    for service in $SERVICES; do
                        echo "Building $service..."
                        cd backend/$service && ./mvnw clean package -DskipTests && cd -
                    done
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Test Backend') {
            steps {
                sh '''
                    for service in $SERVICES; do
                        echo "Testing $service..."
                        cd backend/$service && ./mvnw test && cd -
                    done
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Test Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm run test -- --watch=false --browsers=ChromeHeadless --code-coverage'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'frontend/coverage/**/TESTS-*.xml'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker compose down --remove-orphans
                    docker compose up -d
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    echo "Waiting for services to start..."
                    sleep 20
                    docker compose ps | grep -E "Exit|unhealthy" && exit 1 || echo "All services healthy"
                '''
            }
        }
    }

    post {
        success {
            slackSend(
                channel: '#ci-cd',
                color: 'good',
                message: "✅ *${env.JOB_NAME}* build #${env.BUILD_NUMBER} succeeded on commit `${env.CURRENT_TAG}`\n${env.BUILD_URL}"
            )
            emailext(
                subject: "✅ Build Succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build succeeded on commit ${env.CURRENT_TAG}.\n\nDetails: ${env.BUILD_URL}",
                to: 'team@example.com'
            )
        }
        failure {
            script {
                echo "Build failed — triggering rollback to ${env.ROLLBACK_TAG}"
                sh '''
                    git checkout $ROLLBACK_TAG -- docker-compose.yml || true
                    docker compose down --remove-orphans
                    docker compose up -d || true
                '''
            }
            slackSend(
                channel: '#ci-cd',
                color: 'danger',
                message: "❌ *${env.JOB_NAME}* build #${env.BUILD_NUMBER} FAILED. Rolled back to `${env.ROLLBACK_TAG}`\n${env.BUILD_URL}"
            )
            emailext(
                subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build failed. Rolled back to ${env.ROLLBACK_TAG}.\n\nDetails: ${env.BUILD_URL}",
                to: 'team@example.com'
            )
        }
        always {
            cleanWs()
        }
    }
}
```

---

## 4. Create the Jenkins Pipeline Job

1. Go to Jenkins → **New Item**
2. Name it `buy-01-pipeline`, type: **Pipeline**
3. Under **Build Triggers**: check `GitHub hook trigger for GITScm polling`
4. Under **Pipeline**:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: your GitHub repo URL
   - Credentials: `github-credentials`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
5. Click **Save**

---

## 5. Automated Testing Details

### Backend (JUnit via Maven)

Each Spring Boot service uses `spring-boot-starter-test` (JUnit 5 + Mockito). Tests run with:

```bash
./mvnw test
```

Reports are output to `target/surefire-reports/*.xml` and published to Jenkins via the JUnit plugin.

### Frontend (Karma + Jasmine)

Angular uses Karma with ChromeHeadless for CI. The `karma.conf.js` needs a CI-friendly browser config:

```js
// frontend/karma.conf.js — add inside config.set({})
browsers: ['ChromeHeadless'],
customLaunchers: {
  ChromeHeadless: {
    base: 'Chrome',
    flags: ['--no-sandbox', '--headless', '--disable-gpu']
  }
}
```

Run tests with:
```bash
npm run test -- --watch=false --browsers=ChromeHeadless
```

---

## 6. Rollback Strategy

The pipeline implements a git-based rollback:

1. Before deployment, `ROLLBACK_TAG` captures the previous commit hash (`HEAD~1`)
2. If the `Deploy` or `Health Check` stage fails, the `post { failure }` block:
   - Checks out the previous `docker-compose.yml`
   - Runs `docker compose up -d` to restore the last known working state
3. Notifications include the rollback commit tag

For a more robust rollback with Docker image tagging, tag images with the git commit SHA:

```bash
# In the Build Docker Images stage
docker compose build
docker tag buy-01-user-service:latest buy-01-user-service:${CURRENT_TAG}
```

Then rollback simply re-deploys the previously tagged images.

---

## 7. Parameterized Builds (Bonus)

Add parameters to the pipeline for flexible deployments:

```groovy
parameters {
    choice(name: 'DEPLOY_ENV', choices: ['staging', 'production'], description: 'Target environment')
    booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip test stages')
    string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build')
}
```

Use them in stages:
```groovy
stage('Test Backend') {
    when { expression { !params.SKIP_TESTS } }
    // ...
}
```

---

## 8. Pipeline Summary

```
GitHub Push
    │
    ▼
[Checkout] → clone repo, capture rollback tag
    │
    ▼
[Setup Env] → inject .env, generate SSL certs
    │
    ▼
[Build Backend] → mvn package (all 5 services)
    │
    ▼
[Build Frontend] → npm ci && ng build
    │
    ▼
[Test Backend] → mvn test → JUnit report
    │
    ▼
[Test Frontend] → karma/jasmine → JUnit report
    │
    ▼
[Build Docker Images] → docker compose build
    │
    ▼
[Deploy] → docker compose up -d
    │
    ▼
[Health Check] → verify no exited/unhealthy containers
    │
    ├── SUCCESS → Slack ✅ + Email ✅
    └── FAILURE → Rollback → Slack ❌ + Email ❌
```

---

## 9. Checklist

- [ ] Jenkins running at `http://localhost:8090`
- [ ] Required plugins installed
- [ ] JDK 21, Maven, NodeJS configured as global tools
- [ ] `.env` file added as a secret file credential (`env-file`)
- [ ] GitHub credentials added (`github-credentials`)
- [ ] Slack token and channel configured
- [ ] SMTP email configured
- [ ] `Jenkinsfile` committed to the root of the repo
- [ ] GitHub webhook pointing to Jenkins
- [ ] `karma.conf.js` updated for `ChromeHeadless`
- [ ] Pipeline job created and linked to the repo
- [ ] First manual build triggered successfully
