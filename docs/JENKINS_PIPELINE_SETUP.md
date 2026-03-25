# Jenkins Pipeline Setup

This project uses a Jenkins declarative pipeline defined in [Jenkinsfile](/home/nervan/Desktop/Buy-01/Jenkinsfile).

The pipeline is designed for `product-service` with two behaviors:

- Pull request builds: test only, no deploy
- Pushes to `main`: test, build image, smoke test, deploy

## Pipeline Behavior

### Pull Request

When Jenkins builds a pull request branch:

1. Checkout source code
2. Prepare `.env`
3. Run `backend/product-service` tests
4. Publish JUnit test results
5. Stop

It does **not** build or deploy the container.

### Push To `main`

When Jenkins builds a direct push on `main`:

1. Checkout source code
2. Prepare `.env`
3. Run `backend/product-service` tests
4. Publish JUnit test results
5. Build Docker image for `product-service`
6. Smoke test the built image
7. Recreate the running `product-service` container with the new image

## Current Deployment Logic

The deploy stage runs:

```sh
PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}" docker compose up -d --no-deps --force-recreate product-service
```

This means:

- Jenkins deploys a newly built image
- Docker Compose recreates the `product-service` container
- It does not redeploy other services
- It does not update code inside the running container directly

The image name used by Compose is controlled in [docker-compose.yml](/home/nervan/Desktop/Buy-01/docker-compose.yml):

```yaml
image: ${PRODUCT_SERVICE_IMAGE:-product-service-image:latest}
```

So Jenkins can deploy the exact build image, for example:

```text
product-service-image:25
```

## Jenkins Requirements

## 1. Job Type

Use a **Multibranch Pipeline** job or GitHub Branch Source setup.

This is important because the pipeline relies on Jenkins branch metadata:

- `BRANCH_NAME`
- `CHANGE_ID`

Without multibranch/PR support, Jenkins may not detect pull requests correctly.

## 2. Jenkinsfile Source

The job must build from this repository:

```text
https://github.com/rachid-serraf/Buy-01.git
```

And it must use the `Jenkinsfile` in the repository root.

## 3. Jenkins Agent Requirements

The Jenkins agent that runs this job must have:

- Git
- Docker
- Docker Compose v2 (`docker compose`)
- Java 21 support for Maven wrapper usage
- permission to run Docker commands

If Jenkins cannot access Docker on the host, build and deploy stages will fail.

## 4. Recommended Jenkins Plugins

Recommended plugins:

- Pipeline
- Git
- GitHub Branch Source
- JUnit
- Workspace Cleanup
- Timestamper

Optional:

- AnsiColor

Note: `ansiColor` was removed from the pipeline because some Jenkins instances do not support it in declarative `options`.

## Environment Setup

The pipeline currently prepares environment values like this:

```sh
if [ -f .env ]; then
  echo "Using existing .env from workspace"
elif [ -f .env.example ]; then
  echo "Creating .env from .env.example"
  cp .env.example .env
else
  echo "Missing .env and .env.example"
  exit 1
fi
```

Then it checks:

- `.env` exists
- `SPRING_PROFILES_ACTIVE` exists

## Recommended Approach

For production or shared Jenkins:

- store secrets in **Jenkins Credentials**
- store non-secret configuration in **Jenkins environment variables**
- generate `.env` inside the pipeline if needed

Examples of sensitive values:

- `JWT_PUBLIC_KEY`
- `JWT_PRIVATE_KEY`
- database passwords

Examples of non-sensitive values:

- `SPRING_PROFILES_ACTIVE`
- ports
- service URLs

## Minimal `.env` Notes

If deployment uses Docker Compose, the final `.env` should contain values needed by `product-service` and its dependencies, especially:

- `SPRING_PROFILES_ACTIVE`
- `JWT_PUBLIC_KEY`
- Mongo configuration
- Kafka configuration
- Eureka configuration if used

If `.env.example` has empty secret values, deployment may succeed in Jenkins but the service may fail to start at runtime.

## GitHub Webhook Setup

To trigger Jenkins automatically from GitHub:

1. Open GitHub repository settings
2. Go to `Webhooks`
3. Add a webhook pointing to your Jenkins GitHub webhook endpoint
4. Enable:
   - push events
   - pull request events

Typical Jenkins GitHub webhook URL:

```text
http://<your-jenkins-url>/github-webhook/
```

If Jenkins is behind a reverse proxy, make sure GitHub can reach it.

## How Stage Conditions Work

In the pipeline:

- `Build Image` runs only on branch `main`
- `Smoke Test Image` runs only on branch `main`
- `Deploy` runs only on branch `main` and not on a pull request build

This is controlled with Jenkins `when` conditions.

## Test Reports

The pipeline publishes Maven Surefire reports from:

[`backend/product-service/target/surefire-reports/*.xml`](/home/nervan/Desktop/Buy-01/backend/product-service/target/surefire-reports)

This lets Jenkins show test failures in the UI.

## Suggested First-Time Setup

1. Install the required Jenkins plugins
2. Create a Multibranch Pipeline job
3. Connect the GitHub repository
4. Make sure webhook delivery works
5. Ensure the Jenkins agent has Docker access
6. Provide runtime config for `.env`
7. Run a pull request build and confirm it only tests
8. Push to `main` and confirm it tests, builds, and deploys

## Troubleshooting

### `Invalid option type "ansiColor"`

Cause:

- Jenkins does not support the `ansiColor` declarative option

Fix:

- remove `ansiColor(...)` from the pipeline
- or install/configure the AnsiColor plugin correctly

### `Could not find credentials entry with ID ...`

Cause:

- Jenkinsfile references a credential that does not exist

Fix:

- create the credential in Jenkins
- or change the pipeline to use existing credentials/env values

### Pull requests are deploying

Cause:

- job is not running as multibranch/PR-aware build
- Jenkins is not setting `CHANGE_ID` or `BRANCH_NAME` correctly

Fix:

- use Multibranch Pipeline with GitHub Branch Source

### Docker deploy fails

Cause:

- Jenkins agent cannot access Docker
- Docker Compose is missing
- `.env` is incomplete

Fix:

- verify `docker version`
- verify `docker compose version`
- verify required env values exist

## Related Files

- [Jenkinsfile](/home/nervan/Desktop/Buy-01/Jenkinsfile)
- [docker-compose.yml](/home/nervan/Desktop/Buy-01/docker-compose.yml)
- [backend/product-service/Dockerfile](/home/nervan/Desktop/Buy-01/backend/product-service/Dockerfile)
