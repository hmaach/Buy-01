pipeline {
    agent any

    environment {
        REPO_URL             = 'https://github.com/rachid-serraf/Buy-01.git'
        COMPOSE_PROJECT_NAME = 'buy01'
        IMAGE_TAG            = "${BUILD_NUMBER}"
        HEALTH_RETRIES       = '30'
        HEALTH_INTERVAL      = '10'
        SLACK_CHANNEL        = '#project-buy-01'
        PRODUCT_CHANGED      = 'false'
        USER_CHANGED         = 'false'
        MEDIA_CHANGED        = 'false'
        FRONTEND_CHANGED     = 'false'
        DEPLOY_SERVICES      = ''

        // Derived image names — one source of truth per service
        PRODUCT_SERVICE_IMAGE = "product-service-image:${BUILD_NUMBER}"
        USER_SERVICE_IMAGE    = "user-service-image:${BUILD_NUMBER}"
        MEDIA_SERVICE_IMAGE   = "media-service-image:${BUILD_NUMBER}"
        FRONTEND_IMAGE        = "frontend-image:${BUILD_NUMBER}"
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

        stage('Detect Changes') {
            steps {
                script {
                    def emptyTree = sh(script: 'git hash-object -t tree /dev/null', returnStdout: true).trim()
                    def currentCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                    def hasParent = sh(script: 'git rev-parse --verify HEAD^ >/dev/null 2>&1', returnStatus: true) == 0
                    def baseCommit = emptyTree

                    if (env.CHANGE_TARGET) {
                        sh "git fetch --no-tags origin ${env.CHANGE_TARGET}"
                        baseCommit = sh(
                            script: "git merge-base HEAD origin/${env.CHANGE_TARGET}",
                            returnStdout: true
                        ).trim()
                    } else if (hasParent) {
                        baseCommit = sh(script: 'git rev-parse HEAD^', returnStdout: true).trim()
                    }

                    def changedFiles = sh(
                        script: "git diff --name-only ${baseCommit} ${currentCommit}",
                        returnStdout: true
                    ).trim().split('\n').findAll { it }

                    def changed = { String pathPrefix ->
                        changedFiles.any { it.startsWith(pathPrefix) }
                    }

                    env.PRODUCT_CHANGED = changed('backend/product-service/') ? 'true' : 'false'
                    env.USER_CHANGED = changed('backend/user-service/') ? 'true' : 'false'
                    env.MEDIA_CHANGED = changed('backend/media-service/') ? 'true' : 'false'
                    env.FRONTEND_CHANGED = changed('frontend/') ? 'true' : 'false'

                    env.DEPLOY_SERVICES = [
                        env.USER_CHANGED == 'true' ? 'user-service' : null,
                        env.PRODUCT_CHANGED == 'true' ? 'product-service' : null,
                        env.MEDIA_CHANGED == 'true' ? 'media-service' : null,
                        env.FRONTEND_CHANGED == 'true' ? 'frontend' : null,
                    ].findAll { it }.join(' ')

                    echo "Detecting changes from ${baseCommit} to ${currentCommit}"
                    echo "Changed files: ${changedFiles ?: ['<none>']}"
                    echo """
                        Changed services:
                        - product-service: ${env.PRODUCT_CHANGED}
                        - user-service: ${env.USER_CHANGED}
                        - media-service: ${env.MEDIA_CHANGED}
                        - frontend: ${env.FRONTEND_CHANGED}
                        - deploy targets: ${env.DEPLOY_SERVICES ?: '<none>'}
                    """.stripIndent()
                }
            }
        }

        // ── Run tests for backend services and frontend in parallel ───────
        stage('Unit Tests') {
            parallel {
                stage('product-service') {
                    when { expression { env.PRODUCT_CHANGED == 'true' } }
                    steps { dir('backend/product-service') { sh './mvnw clean test' } }
                }
                stage('user-service') {
                    when { expression { env.USER_CHANGED == 'true' } }
                    steps { dir('backend/user-service') { sh './mvnw clean test' } }
                }
                stage('media-service') {
                    when { expression { env.MEDIA_CHANGED == 'true' } }
                    steps { dir('backend/media-service') { sh './mvnw clean test' } }
                }
                stage('frontend') {
                    when { expression { env.FRONTEND_CHANGED == 'true' } }
                    steps {
                        dir('frontend') {
                            sh 'npm ci'
                            sh 'npm run test:ci'
                        }
                    }
                }
            }
            post {
                always {
                    junit(
                        testResults: '''
                            backend/product-service/target/surefire-reports/*.xml,
                            backend/media-service/target/surefire-reports/*.xml,
                            backend/user-service/target/surefire-reports/*.xml
                        ''',
                        allowEmptyResults: true
                    )
                }
            }
        }

        // ── Build all images in parallel (main branch only) ───────────────
        stage('Build Images') {
            when {
                allOf {
                    branch 'main'
                    expression { env.DEPLOY_SERVICES?.trim() }
                }
            }
            parallel {
                stage('product-service') {
                    when { expression { env.PRODUCT_CHANGED == 'true' } }
                    steps {
                        sh """
                            docker build \
                                -t "${PRODUCT_SERVICE_IMAGE}" \
                                -t "product-service-image:latest" \
                                backend/product-service
                        """
                    }
                }
                stage('user-service') {
                    when { expression { env.USER_CHANGED == 'true' } }
                    steps {
                        sh """
                            docker build \
                                -t "${USER_SERVICE_IMAGE}" \
                                -t "user-service-image:latest" \
                                backend/user-service
                        """
                    }
                }
                stage('media-service') {
                    when { expression { env.MEDIA_CHANGED == 'true' } }
                    steps {
                        sh """
                            docker build \
                                -t "${MEDIA_SERVICE_IMAGE}" \
                                -t "media-service-image:latest" \
                                backend/media-service
                        """
                    }
                }
                stage('frontend') {
                    when { expression { env.FRONTEND_CHANGED == 'true' } }
                    steps {
                        sh """
                            docker build \
                                -t "${FRONTEND_IMAGE}" \
                                -t "frontend-image:latest" \
                                frontend
                        """
                    }
                }
            }
        }

        // ── Lightweight sanity-check: confirm images start cleanly ───────
        stage('Smoke Test Images') {
            when {
                allOf {
                    branch 'main'
                    expression { env.DEPLOY_SERVICES?.trim() }
                }
            }
            parallel {
                stage('product-service') {
                    when { expression { env.PRODUCT_CHANGED == 'true' } }
                    steps { sh 'docker run --rm --entrypoint java "${PRODUCT_SERVICE_IMAGE}" -version' }
                }
                stage('user-service') {
                    when { expression { env.USER_CHANGED == 'true' } }
                    steps { sh 'docker run --rm --entrypoint java "${USER_SERVICE_IMAGE}" -version' }
                }
                stage('media-service') {
                    when { expression { env.MEDIA_CHANGED == 'true' } }
                    steps { sh 'docker run --rm --entrypoint java "${MEDIA_SERVICE_IMAGE}" -version' }
                }
                stage('frontend') {
                    when { expression { env.FRONTEND_CHANGED == 'true' } }
                    steps { sh 'docker run --rm --entrypoint nginx "${FRONTEND_IMAGE}" -t' }
                }
            }
        }

        // ── Deploy: snapshot → stop old → start new ────────────────────────
        stage('Deploy') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                    expression { env.DEPLOY_SERVICES?.trim() }
                }
            }

            steps {
                withCredentials([file(credentialsId: 'APP_ENV_FILE', variable: 'ENV_FILE')]) {
                    sh '''
                        # ── Validate env file ────────────────────────────────
                        cp "${ENV_FILE}" .env
                        test -s .env
                        grep -q '^SPRING_PROFILES_ACTIVE=' .env

                        # ── Snapshot running image per container name ─────────
                        #    docker inspect returns the image tag the container
                        #    was started from.  Written to .prev_* files so both
                        #    the post{failure} block here and the Health Check
                        #    stage can read them for rollback.
                        snapshot_image() {
                            docker inspect --format '{{.Config.Image}}' "$1" 2>/dev/null || true
                        }

                        prev_file() {
                            case "$1" in
                                product-service) echo .prev_product ;;
                                user-service)    echo .prev_user ;;
                                media-service)   echo .prev_media ;;
                                frontend)        echo .prev_frontend ;;
                            esac
                        }

                        echo "=== Previous images for changed services ==="
                        for CTR in ${DEPLOY_SERVICES}; do
                            FILE=$(prev_file "$CTR")
                            snapshot_image "$CTR" > "$FILE"
                            printf '%s -> ' "$CTR"
                            cat "$FILE" || true
                        done

                        # ── Stop & remove old containers ──────────────────────
                        #    This frees the static name AND the bound host ports.
                        #    --volumes is intentionally omitted — named volumes
                        #    (mongo data, uploads) are owned by Docker and survive.
                        for CTR in ${DEPLOY_SERVICES}; do
                            if docker inspect "$CTR" > /dev/null 2>&1; then
                                echo "Stopping $CTR..."
                                docker stop "$CTR"
                                docker rm   "$CTR"
                            else
                                echo "$CTR not running — skipping stop."
                            fi
                        done

                        # ── Start new containers ──────────────────────────────
                        #    compose up re-creates each service container under the
                        #    same static name, mounting the same named volumes.
                        export PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}"
                        export USER_SERVICE_IMAGE="${USER_SERVICE_IMAGE}"
                        export MEDIA_SERVICE_IMAGE="${MEDIA_SERVICE_IMAGE}"
                        export FRONTEND_IMAGE="${FRONTEND_IMAGE}"

                        docker compose up -d --no-deps --force-recreate \
                            ${DEPLOY_SERVICES}

                        echo "New containers started — health check follows."
                    '''
                }
            }

            post {
                failure {
                    // Fires if compose up fails after old containers were already removed.
                    // Restores previous images so the service comes back up.
                    sh '''
                        echo "Deploy failed — restoring previous images..."

                        prev_file() {
                            case "$1" in
                                product-service) echo .prev_product ;;
                                user-service)    echo .prev_user ;;
                                media-service)   echo .prev_media ;;
                                frontend)        echo .prev_frontend ;;
                            esac
                        }

                        restore_image_var() {
                            case "$1" in
                                product-service) export PRODUCT_SERVICE_IMAGE="$2" ;;
                                user-service)    export USER_SERVICE_IMAGE="$2" ;;
                                media-service)   export MEDIA_SERVICE_IMAGE="$2" ;;
                                frontend)        export FRONTEND_IMAGE="$2" ;;
                            esac
                        }

                        RESTORE_SERVICES=""

                        for SVC in ${DEPLOY_SERVICES}; do
                            PREV_IMAGE=$(cat "$(prev_file "$SVC")" 2>/dev/null || true)
                            if [ -n "$PREV_IMAGE" ]; then
                                restore_image_var "$SVC" "$PREV_IMAGE"
                                RESTORE_SERVICES="$RESTORE_SERVICES $SVC"
                            fi
                        done

                        if [ -n "$RESTORE_SERVICES" ]; then
                            docker compose up -d --no-deps --force-recreate $RESTORE_SERVICES
                            echo "Restore complete."
                        else
                            echo "WARNING: no snapshots found for changed services."
                        fi
                    '''
                }
            }
        }

        // ── Health Check: poll container health → rollback if unhealthy ───────
        stage('Health Check') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                    expression { env.DEPLOY_SERVICES?.trim() }
                }
            }

            steps {
                sh '''
                    healthy() {
                        SVC=$1
                        TRIES=${HEALTH_RETRIES}
                        until [ "$(docker inspect --format '{{.State.Health.Status}}' \
                                    "$SVC" 2>/dev/null)" = "healthy" ]; do
                            TRIES=$((TRIES - 1))
                            [ "$TRIES" -le 0 ] && \
                                { echo "ERROR: $SVC never became healthy."; return 1; }
                            echo "Waiting for $SVC... ($TRIES retries left)"
                            sleep "${HEALTH_INTERVAL}"
                        done
                        echo "$SVC is healthy."
                    }

                    for SVC in ${DEPLOY_SERVICES}; do
                        healthy "$SVC" || exit 1
                    done
                    echo "All changed services are healthy."
                '''
            }

            post {
                failure {
                    sh '''
                        echo "Health check failed — rolling back to previous images..."

                        prev_file() {
                            case "$1" in
                                product-service) echo .prev_product ;;
                                user-service)    echo .prev_user ;;
                                media-service)   echo .prev_media ;;
                                frontend)        echo .prev_frontend ;;
                            esac
                        }

                        restore_image_var() {
                            case "$1" in
                                product-service) export PRODUCT_SERVICE_IMAGE="$2" ;;
                                user-service)    export USER_SERVICE_IMAGE="$2" ;;
                                media-service)   export MEDIA_SERVICE_IMAGE="$2" ;;
                                frontend)        export FRONTEND_IMAGE="$2" ;;
                            esac
                        }

                        RESTORE_SERVICES=""

                        for SVC in ${DEPLOY_SERVICES}; do
                            PREV_IMAGE=$(cat "$(prev_file "$SVC")" 2>/dev/null || true)
                            if [ -n "$PREV_IMAGE" ]; then
                                restore_image_var "$SVC" "$PREV_IMAGE"
                                RESTORE_SERVICES="$RESTORE_SERVICES $SVC"
                            fi
                        done

                        if [ -n "$RESTORE_SERVICES" ]; then
                            docker compose up -d --no-deps --force-recreate $RESTORE_SERVICES
                            echo "Rollback complete — previous version is live again."
                        else
                            echo "WARNING: no snapshots found for changed services."
                            echo "Manual intervention required."
                        fi
                    '''
                }
            }
        }

    }

    // ── Pipeline-level notifications & cleanup ────────────────────────────
    post {
        success {
            script {
                def msg = env.CHANGE_ID
                    ? "PR #${env.CHANGE_ID} validation passed — ${env.BUILD_URL}"
                    : "Build #${env.BUILD_NUMBER} deployed successfully — ${env.BUILD_URL}"
                slackSend(channel: env.SLACK_CHANNEL,
                          color: 'good',
                          message: "SUCCESS: ${msg}")
            }
        }
        failure {
            slackSend(channel: env.SLACK_CHANNEL,
                      color: 'danger',
                      message: "FAILED: Build #${env.BUILD_NUMBER} failed — ${env.BUILD_URL}\nCheck the console for details.")
        }
        always {
            sh '''
                for repo in product-service-image user-service-image media-service-image frontend-image; do
                    docker image ls "$repo" \
                        --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" || true
                done
            '''
            cleanWs()
        }
    }
}
