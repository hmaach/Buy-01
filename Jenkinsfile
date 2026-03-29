pipeline {
    agent any

    environment {
        REPO_URL             = 'https://github.com/rachid-serraf/Buy-01.git'
        COMPOSE_PROJECT_NAME = 'buy01'
        IMAGE_TAG            = "${BUILD_NUMBER}"
        HEALTH_RETRIES       = '30'
        HEALTH_INTERVAL      = '10'
        NOTIFY_EMAIL         = 'serrafrachiddev@gmail.com'

        // Derived image names — one source of truth per service
        PRODUCT_SERVICE_IMAGE = "product-service-image:${BUILD_NUMBER}"
        USER_SERVICE_IMAGE    = "user-service-image:${BUILD_NUMBER}"
        MEDIA_SERVICE_IMAGE   = "media-service-image:${BUILD_NUMBER}"
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

        // ── Run tests for all three services in parallel ──────────────────
        stage('Unit Tests') {
            parallel {
                stage('product-service') {
                    steps { dir('backend/product-service') { sh './mvnw clean test' } }
                }
                stage('user-service') {
                    steps { dir('backend/user-service') { sh './mvnw clean test' } }
                }
                stage('media-service') {
                    steps { dir('backend/media-service') { sh './mvnw clean test' } }
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
                        allowEmptyResults: false
                    )
                }
            }
        }

        // ── Build all images in parallel (main branch only) ───────────────
        stage('Build Images') {
            when { branch 'main' }
            parallel {
                stage('product-service') {
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
                    steps {
                        sh """
                            docker build \
                                -t "${MEDIA_SERVICE_IMAGE}" \
                                -t "media-service-image:latest" \
                                backend/media-service
                        """
                    }
                }
            }
        }

        // ── Lightweight sanity-check: confirm the JVM starts ─────────────
        stage('Smoke Test Images') {
            when { branch 'main' }
            steps {
                sh """
                    docker run --rm --entrypoint java "${PRODUCT_SERVICE_IMAGE}" -version
                    docker run --rm --entrypoint java "${USER_SERVICE_IMAGE}"    -version
                    docker run --rm --entrypoint java "${MEDIA_SERVICE_IMAGE}"   -version
                """
            }
        }

        // ── Deploy: snapshot → stop old → start new ────────────────────────
        stage('Deploy') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
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

                        snapshot_image product-service > .prev_product
                        snapshot_image user-service    > .prev_user
                        snapshot_image media-service   > .prev_media

                        echo "=== Previous images (empty = first deploy) ==="
                        cat .prev_product .prev_user .prev_media || true

                        # ── Stop & remove old containers ──────────────────────
                        #    This frees the static name AND the bound host ports.
                        #    --volumes is intentionally omitted — named volumes
                        #    (mongo data, uploads) are owned by Docker and survive.
                        for CTR in product-service user-service media-service; do
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

                        docker compose up -d --no-deps --force-recreate \
                            user-service product-service media-service

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

                        PREV_PRODUCT=$(cat .prev_product 2>/dev/null || true)
                        PREV_USER=$(cat .prev_user       2>/dev/null || true)
                        PREV_MEDIA=$(cat .prev_media     2>/dev/null || true)

                        if [ -n "$PREV_PRODUCT" ] && \
                           [ -n "$PREV_USER"    ] && \
                           [ -n "$PREV_MEDIA"   ]; then
                            PRODUCT_SERVICE_IMAGE="$PREV_PRODUCT" \
                            USER_SERVICE_IMAGE="$PREV_USER" \
                            MEDIA_SERVICE_IMAGE="$PREV_MEDIA" \
                            docker compose up -d --no-deps --force-recreate \
                                user-service product-service media-service
                            echo "Restore complete."
                        else
                            echo "WARNING: first deploy failed — no snapshot to restore from."
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

                    healthy product-service || exit 1
                    healthy user-service    || exit 1
                    healthy media-service   || exit 1
                    echo "All services healthy."
                '''
            }

            post {
                failure {
                    sh '''
                        echo "Health check failed — rolling back to previous images..."

                        PREV_PRODUCT=$(cat .prev_product 2>/dev/null || true)
                        PREV_USER=$(cat .prev_user       2>/dev/null || true)
                        PREV_MEDIA=$(cat .prev_media     2>/dev/null || true)

                        if [ -n "$PREV_PRODUCT" ] && \
                           [ -n "$PREV_USER"    ] && \
                           [ -n "$PREV_MEDIA"   ]; then
                            PRODUCT_SERVICE_IMAGE="$PREV_PRODUCT" \
                            USER_SERVICE_IMAGE="$PREV_USER" \
                            MEDIA_SERVICE_IMAGE="$PREV_MEDIA" \
                            docker compose up -d --no-deps --force-recreate \
                                user-service product-service media-service
                            echo "Rollback complete — previous version is live again."
                        else
                            echo "WARNING: no snapshot found — this was a first deploy."
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
                mail(to: env.NOTIFY_EMAIL,
                     subject: "Build #${env.BUILD_NUMBER} — Success",
                     body: msg)
            }
        }
        failure {
            mail(to:      env.NOTIFY_EMAIL,
                 subject: "Build #${env.BUILD_NUMBER} — Failed",
                 body:    "Build #${env.BUILD_NUMBER} failed — ${env.BUILD_URL}\n\nCheck the console for details.")
        }
        always {
            sh '''
                for repo in product-service-image user-service-image media-service-image; do
                    docker image ls "$repo" \
                        --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" || true
                done
            '''
            cleanWs()
        }
    }
}
