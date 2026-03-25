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
                // This command tells the host's Docker engine to build your services
                sh 'docker compose build product-service'
                sh 'docker compose up -d product-service'
            }
        }
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