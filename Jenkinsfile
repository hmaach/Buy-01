pipeline {
    agent any

    environment {
        REPO_URL = 'https://github.com/rachid-serraf/Buy-01.git'
        SERVICE_NAME = 'product-service'
        IMAGE_REPO = 'product-service-image'
        IMAGE_TAG = "${BUILD_NUMBER}"
        PRODUCT_SERVICE_IMAGE = "${IMAGE_REPO}:${IMAGE_TAG}"
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
                git branch: 'main', url: "${REPO_URL}"
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

        stage('Unit Test') {
            steps {
                dir('backend/product-service') {
                    sh './mvnw clean test'
                }
            }
            post {
                always {
                    junit 'backend/product-service/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Image') {
            steps {
                sh 'docker build -t "${PRODUCT_SERVICE_IMAGE}" backend/product-service'
                sh 'docker tag "${PRODUCT_SERVICE_IMAGE}" "${IMAGE_REPO}:latest"'
            }
        }

        stage('Smoke Test Image') {
            steps {
                sh 'docker run --rm --entrypoint java "${PRODUCT_SERVICE_IMAGE}" -version'
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying ${PRODUCT_SERVICE_IMAGE}..."
                sh 'PRODUCT_SERVICE_IMAGE="${PRODUCT_SERVICE_IMAGE}" docker compose up -d --no-deps --force-recreate product-service'
            }
        }
    }

    post {
        success {
            echo "Deployment completed for ${PRODUCT_SERVICE_IMAGE}"
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
