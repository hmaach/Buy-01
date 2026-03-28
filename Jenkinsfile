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

        // ── Stage 1: spin up NEW containers alongside the OLD ones ──────────
        //    Data is safe because every service mounts a *named* Docker volume.
        //    Named volumes are owned by Docker, not by a container — removing
        //    the old container never touches the volume data.  The new container
        //    mounts the exact same volume name, so it picks up all existing data
        //    on first start.  The old container keeps running and serving traffic
        //    until the new one passes its health check (Stage 2).
        stage('Deploy (blue→green)') {
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

                        # ── Helper: image currently running in a compose service ──
                        running_image() {
                            CID=$(COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                                  docker compose ps -q "$1" 2>/dev/null)
                            [ -n "$CID" ] && \
                                docker inspect --format '{{.Config.Image}}' "$CID" 2>/dev/null \
                                || true
                        }

                        # ── Snapshot OLD images (needed for rollback) ────────
                        #    Written to the workspace so the Health Check stage
                        #    and the post{failure} block can both read them.
                        running_image product-service > .prev_product
                        running_image user-service    > .prev_user
                        running_image media-service   > .prev_media
                        echo "=== Previous images ==="
                        cat .prev_product .prev_user .prev_media

                        # ── Start GREEN containers (suffix _new) ─────────────
                        #    They share the same named volumes as the blue stack.
                        #    --no-deps keeps gateway/db containers untouched.
                        export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}_new"
                        export PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}"
                        export USER_SERVICE_IMAGE="${USER_SERVICE_IMAGE}"
                        export MEDIA_SERVICE_IMAGE="${MEDIA_SERVICE_IMAGE}"

                        docker compose up -d --no-deps --force-recreate \
                            user-service product-service media-service

                        echo "Green containers started — old (blue) still serving traffic."
                    '''
                }
            }
        }

        // ── Stage 2: health-check GREEN, then cut over atomically ────────
        //    Only after all three green containers report "healthy" do we
        //    stop the blue containers.  If anything fails here, post{failure}
        //    removes the green containers — blue never stopped, so there is no
        //    downtime and no data loss.
        stage('Health Check & Cutover') {
            when {
                allOf {
                    branch 'main'
                    not { changeRequest() }
                }
            }

            steps {
                sh '''
                    # ── Helper: wait for a container in the GREEN project ────
                    green_cid() {
                        COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}_new" \
                            docker compose ps -q "$1" 2>/dev/null
                    }

                    healthy() {
                        SVC=$1; TRIES=${HEALTH_RETRIES}
                        until [ -n "$(green_cid "$SVC")" ] && \
                              [ "$(docker inspect \
                                    --format '{{.State.Health.Status}}' \
                                    "$(green_cid "$SVC")" 2>/dev/null)" = "healthy" ]; do
                            TRIES=$((TRIES - 1))
                            [ "$TRIES" -le 0 ] && \
                                { echo "ERROR: $SVC (green) never became healthy."; return 1; }
                            echo "Waiting for $SVC (green)... ($TRIES retries left)"
                            sleep "${HEALTH_INTERVAL}"
                        done
                        echo "$SVC (green) is healthy."
                    }

                    # ── Wait for all green services ──────────────────────────
                    healthy product-service || exit 1
                    healthy user-service    || exit 1
                    healthy media-service   || exit 1

                    # ── Atomic cutover: stop BLUE, promote GREEN ─────────────
                    #    1. Stop & remove old (blue) containers.
                    #       --volumes is intentionally OMITTED — named volumes
                    #       must not be deleted here.
                    echo "All green services healthy — cutting over..."

                    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                        docker compose stop  user-service product-service media-service
                    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                        docker compose rm -f user-service product-service media-service

                    #    2. Re-launch under the canonical project name so that
                    #       every downstream tool (monitoring, log shippers, etc.)
                    #       sees the normal container names again.
                    export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}"
                    export PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}"
                    export USER_SERVICE_IMAGE="${USER_SERVICE_IMAGE}"
                    export MEDIA_SERVICE_IMAGE="${MEDIA_SERVICE_IMAGE}"

                    docker compose up -d --no-deps --force-recreate \
                        user-service product-service media-service

                    #    3. Tear down the temporary _new project now that the
                    #       canonical project owns the containers.
                    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}_new" \
                        docker compose rm -f user-service product-service media-service

                    echo "Cutover complete — green is now production."
                '''
            }

            post {
                failure {
                    sh '''
                        echo "Health check or cutover failed — rolling back..."

                        # ── Remove any green containers that may still be up ─
                        COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}_new" \
                            docker compose rm -sf \
                                user-service product-service media-service \
                            2>/dev/null || true

                        # ── Restore blue containers from the image snapshot ───
                        #    If blue containers were already stopped during a
                        #    partially-completed cutover, this brings them back.
                        PREV_PRODUCT=$(cat .prev_product 2>/dev/null || true)
                        PREV_USER=$(cat .prev_user       2>/dev/null || true)
                        PREV_MEDIA=$(cat .prev_media     2>/dev/null || true)

                        if [ -n "$PREV_PRODUCT" ] && \
                           [ -n "$PREV_USER"    ] && \
                           [ -n "$PREV_MEDIA"   ]; then

                            COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                            PRODUCT_SERVICE_IMAGE="$PREV_PRODUCT" \
                            USER_SERVICE_IMAGE="$PREV_USER" \
                            MEDIA_SERVICE_IMAGE="$PREV_MEDIA" \
                            docker compose up -d --no-deps --force-recreate \
                                user-service product-service media-service

                            echo "Rollback complete — previous version is live again."
                        else
                            echo "WARNING: no previous image snapshot found — manual intervention required."
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