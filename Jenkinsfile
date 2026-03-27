pipeline {
    agent any

    environment {
        REPO_URL              = 'https://github.com/rachid-serraf/Buy-01.git'
        PRODUCT_IMAGE_REPO    = 'product-service-image'
        USER_IMAGE_REPO       = 'user-service-image'
        MEDIA_IMAGE_REPO      = 'media-service-image'
        IMAGE_TAG             = "${BUILD_NUMBER}"
        PRODUCT_SERVICE_IMAGE = "${PRODUCT_IMAGE_REPO}:${IMAGE_TAG}"
        USER_SERVICE_IMAGE    = "${USER_IMAGE_REPO}:${IMAGE_TAG}"
        MEDIA_SERVICE_IMAGE   = "${MEDIA_IMAGE_REPO}:${IMAGE_TAG}"
        COMPOSE_PROJECT_NAME  = 'buy01'
        HEALTH_RETRIES        = '20'
        HEALTH_INTERVAL       = '10'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Unit Tests') {
            steps {
                dir('backend/product-service') { sh './mvnw clean test' }
                dir('backend/media-service')   { sh './mvnw clean test' }
                dir('backend/user-service')    { sh './mvnw clean test' }
            }
            post {
                always {
                    junit '''
                        backend/product-service/target/surefire-reports/*.xml,
                        backend/media-service/target/surefire-reports/*.xml,
                        backend/user-service/target/surefire-reports/*.xml
                    '''
                }
            }
        }

        stage('Build Images') {
            when { branch 'main' }
            steps {
                sh '''
                    docker build -t "${PRODUCT_SERVICE_IMAGE}" \
                                 -t "${PRODUCT_IMAGE_REPO}:latest" \
                                 backend/product-service

                    docker build -t "${USER_SERVICE_IMAGE}" \
                                 -t "${USER_IMAGE_REPO}:latest" \
                                 backend/user-service

                    docker build -t "${MEDIA_SERVICE_IMAGE}" \
                                 -t "${MEDIA_IMAGE_REPO}:latest" \
                                 backend/media-service
                '''
            }
        }

        stage('Smoke Test Images') {
            when { branch 'main' }
            steps {
                sh '''
                    docker run --rm --entrypoint java "${PRODUCT_SERVICE_IMAGE}" -version
                    docker run --rm --entrypoint java "${USER_SERVICE_IMAGE}"    -version
                    docker run --rm --entrypoint java "${MEDIA_SERVICE_IMAGE}"   -version
                '''
            }
        }

        stage('Deploy') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }
            steps {
                // APP_ENV_FILE — Jenkins "Secret file" credential
                withCredentials([file(credentialsId: 'APP_ENV_FILE', variable: 'ENV_FILE')]) {
                    sh '''
                        # Inject .env from Jenkins — never from the repo
                        cp "${ENV_FILE}" .env
                        test -s .env
                        grep -q '^SPRING_PROFILES_ACTIVE=' .env

                        # Snapshot current images for rollback
                        docker inspect product-service \
                            --format '{{.Config.Image}}' > .previous_product 2>/dev/null || true
                        docker inspect user-service \
                            --format '{{.Config.Image}}' > .previous_user    2>/dev/null || true
                        docker inspect media-service \
                            --format '{{.Config.Image}}' > .previous_media   2>/dev/null || true

                        COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                        PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}" \
                        USER_SERVICE_IMAGE="${USER_SERVICE_IMAGE}" \
                        MEDIA_SERVICE_IMAGE="${MEDIA_SERVICE_IMAGE}" \
                        docker compose up -d --no-deps --force-recreate \
                            user-service product-service media-service
                    '''
                }
            }
        }

        stage('Health Check') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }
            steps {
                sh '''
                    check_healthy() {
                        SERVICE=$1
                        RETRIES=${HEALTH_RETRIES}
                        until [ "$(docker inspect --format='{{.State.Health.Status}}' \
                                "$SERVICE" 2>/dev/null)" = "healthy" ]; do
                            RETRIES=$((RETRIES - 1))
                            if [ "$RETRIES" -le 0 ]; then
                                echo "ERROR: $SERVICE did not become healthy."
                                return 1
                            fi
                            echo "Waiting for $SERVICE... ($RETRIES retries left)"
                            sleep ${HEALTH_INTERVAL}
                        done
                        echo "$SERVICE is healthy."
                    }

                    check_healthy product-service || exit 1
                    check_healthy user-service    || exit 1
                    check_healthy media-service   || exit 1
                '''
            }
            post {
                failure {
                    sh '''
                        echo "Health check failed — rolling back..."

                        PREV_PRODUCT=$(cat .previous_product 2>/dev/null || echo "")
                        PREV_USER=$(cat .previous_user       2>/dev/null || echo "")
                        PREV_MEDIA=$(cat .previous_media     2>/dev/null || echo "")

                        if [ -n "$PREV_PRODUCT" ] && \
                           [ -n "$PREV_USER" ]    && \
                           [ -n "$PREV_MEDIA" ]; then
                            COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                            PRODUCT_SERVICE_IMAGE="$PREV_PRODUCT" \
                            USER_SERVICE_IMAGE="$PREV_USER" \
                            MEDIA_SERVICE_IMAGE="$PREV_MEDIA" \
                            docker compose up -d --no-deps --force-recreate \
                                user-service product-service media-service
                            echo "Rollback complete."
                        else
                            echo "No previous image recorded — skipping rollback."
                        fi
                    '''
                }
            }
        }
    }

    post {
        success {
            script {
                def msg = env.CHANGE_ID
                    ? "PR #${env.CHANGE_ID} validation passed — ${env.BUILD_URL}"
                    : "Build #${env.BUILD_NUMBER} deployed successfully — ${env.BUILD_URL}"

                // SLACK_WEBHOOK — Jenkins "Secret text" credential
                withCredentials([string(credentialsId: 'SLACK_WEBHOOK', variable: 'WEBHOOK')]) {
                    sh """
                        curl -s -X POST -H 'Content-type: application/json' \
                        --data "{\"text\":\"${msg}\"}" \
                        "\${WEBHOOK}"
                    """
                }
            }
        }
        failure {
            // SLACK_WEBHOOK — Jenkins "Secret text" credential
            withCredentials([string(credentialsId: 'SLACK_WEBHOOK', variable: 'WEBHOOK')]) {
                sh """
                    curl -s -X POST -H 'Content-type: application/json' \
                    --data "{\"text\":\"Build #${env.BUILD_NUMBER} failed — ${env.BUILD_URL}\"}" \
                    "\${WEBHOOK}"
                """
            }
        }
        always {
            sh '''
                docker image ls "${PRODUCT_IMAGE_REPO}" \
                    --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" || true
                docker image ls "${USER_IMAGE_REPO}" \
                    --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" || true
                docker image ls "${MEDIA_IMAGE_REPO}" \
                    --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" || true
            '''
            cleanWs()
        }
    }
}