pipeline {
    agent any

    environment {
        // Use the public URL of your repo
        REPO_URL = 'https://github.com/rachid-serraf/Buy-01.git'
    }

    stages {
        stage('Cleanup') {
            steps {
                echo "Cleaning up workspace..."
                cleanWs()
            }
        }

        stage('Clone') {
            steps {
                echo "Cloning ${REPO_URL}..."
                // Simple git clone for public repos
                git branch: 'main', url: "${REPO_URL}"
            }
        }

        stage('Build') {
            steps {
                echo "Starting Docker Compose Build..."
                // Build the image without starting it
                sh 'make env'
                sh 'docker compose build product-service'
            }
        }

        // stage('Integration Test') {
        //     steps {
        //         echo "Running logic tests against the new build..."
        //         // We run a temporary container just to execute tests
        //         // '--rm' ensures the container is deleted immediately after testing
        //         sh '''
        //             docker run --rm \
        //             --network ecommerce-network \
        //             -e SPRING_PROFILES_ACTIVE=test \
        //             product-service:latest \
        //             ./mvnw test
        //         '''
        //     }
        // }

        // stage('Safe Deploy') {
        //     // This stage ONLY runs if the 'Integration Test' stage finished successfully
        //     steps {
        //         echo "Tests passed! Updating production container..."
        //         sh 'docker compose up -d --no-deps product-service'
        //     }
        // }
    }

    post {
        success {
            echo "Successfully cloned and built the images!"
        }
        failure {
            echo "Something went wrong. Check the Console Output in Jenkins."
        }
    }
}