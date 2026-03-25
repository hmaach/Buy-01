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
        ansiColor('xterm')
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
                withCredentials([file(credentialsId: 'buy01-env-file', variable: 'ENV_FILE')]) {
                    sh 'cp "$ENV_FILE" .env'
                }
                sh '''
                    test -s .env
                    grep -q '^JWT_PUBLIC_KEY=' .env
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
