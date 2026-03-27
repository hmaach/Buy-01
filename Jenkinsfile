pipeline {
    agent any

    environment {
        REPO_URL = 'https://github.com/rachid-serraf/Buy-01.git'
        IMAGE_REPO = 'product-service-image'
        IMAGE_TAG = "${BUILD_NUMBER}"
        PRODUCT_SERVICE_IMAGE = "${IMAGE_REPO}:${IMAGE_TAG}"
        COMPOSE_PROJECT_NAME = 'buy01'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                echo "Cloning ${REPO_URL}..."
                checkout scm
            }
        }

        stage('Prepare Environment') {
            steps {
                sh '''
                    if [ -f .env ]; then
                      echo "Using existing .env from workspace"
                    elif [ -f .env.example ]; then
                      echo "Creating .env from .env.example"
                      cp .env.example .env
                    else
                      echo "Missing .env and .env.example"
                      exit 1
                    fi

                    test -s .env
                    grep -q '^SPRING_PROFILES_ACTIVE=' .env
                '''
            }
        }

        stage('Unit Tests') {
            steps {
                dir('backend/product-service') {
                    sh './mvnw clean test'
                }
                dir('backend/media-service') {
                    sh './mvnw clean test'
                }
                dir('backend/user-service') {
                    sh './mvnw clean test'
                }
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

        stage('Build Image') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker build -t "${PRODUCT_SERVICE_IMAGE}" backend/product-service'
                sh 'docker tag "${PRODUCT_SERVICE_IMAGE}" "${IMAGE_REPO}:latest"'
            }
        }

        stage('Smoke Test Image') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker run --rm --entrypoint java "${PRODUCT_SERVICE_IMAGE}" -version'
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
                echo "Deploying ${PRODUCT_SERVICE_IMAGE}..."
                sh '''
                    # One-time migration cleanup: remove the old fixed-name container if it still exists.
                    docker rm -f product-service >/dev/null 2>&1 || true

                    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME}" \
                    PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}" \
                    docker compose up -d --no-deps --force-recreate product-service
                '''
            }
        }
    }

    post {
        success {
            script {
                if (env.CHANGE_ID) {
                    echo "Pull request validation completed successfully."
                } else if (env.BRANCH_NAME == 'main') {
                    echo "Main branch deployment completed for ${PRODUCT_SERVICE_IMAGE}"
                } else {
                    echo "Branch validation completed successfully."
                }
            }
        }
        failure {
            echo "Something went wrong. Check the Console Output in Jenkins."
        }
        always {
            sh 'docker image ls "${IMAGE_REPO}" --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}" || true'
            cleanWs()
        }
    }
}
