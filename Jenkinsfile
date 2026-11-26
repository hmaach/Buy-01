// CI/CD pipeline for Buy-01 (mr-jenk). Flow and audit mapping: docs/ci-pipeline.md
//
// Every push to main, dev or a pull request: build and test the backend and the frontend,
// each in a throwaway container on the agent, and publish the JUnit reports.
// On main only: build arm64 images tagged with the commit, deploy them with compose,
// smoke test, and redeploy the previous tag automatically if the deploy or the smoke test fails.
// Every build ends with an email that says what was built, tested and deployed.

pipeline {
    // The SSH agent on the VM (vm-agent). The controller has no executors.
    agent { label 'docker' }

    parameters {
        booleanParam(name: 'DEPLOY', defaultValue: true,
                description: 'Deploy after a green build. Only the main branch deploys.')
        booleanParam(name: 'SIMULATE_SMOKE_FAILURE', defaultValue: false,
                description: 'Make the smoke test fail on purpose, to show the automatic rollback.')
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        DEPLOY_BRANCH = 'main'
        // The VM is ARM64. Set explicitly so a build on another machine cannot ship the wrong architecture.
        DOCKER_DEFAULT_PLATFORM = 'linux/arm64'
        IMAGE_TAG = sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()
        COMMIT_TITLE = sh(script: 'git log -1 --pretty=%s', returnStdout: true).trim()
    }

    stages {
        stage('Backend: build and test') {
            agent {
                docker {
                    image 'maven:3.9.16-eclipse-temurin-21'
                    // Maven cache kept on the agent between builds. Memory capped to protect the live app.
                    args '-v /home/jenkins-agent/.m2:/home/jenkins-agent/.m2 --memory=1536m'
                    reuseNode true
                }
            }
            environment {
                MAVEN_OPTS = '-Xmx512m -Duser.home=/home/jenkins-agent'
            }
            steps {
                dir('backend') {
                    sh 'mvn -B clean verify'
                }
            }
            post {
                always {
                    junit testResults: 'backend/**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Frontend: test') {
            agent {
                // Node 22 with Chromium, from infra/jenkins/agents/node/Dockerfile
                dockerfile {
                    dir 'infra/jenkins/agents/node'
                    args '-v /home/jenkins-agent/.npm:/home/jenkins-agent/.npm --memory=1536m'
                    reuseNode true
                }
            }
            environment {
                HOME = '/tmp'
                npm_config_cache = '/home/jenkins-agent/.npm'
                NG_CLI_ANALYTICS = 'false'
            }
            steps {
                dir('frontend') {
                    sh 'npm ci --no-audit --no-fund'
                    sh 'npx ng test --watch=false --browsers=ChromeHeadlessCI'
                }
            }
            post {
                always {
                    junit testResults: 'frontend/test-results/karma/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Build images') {
            when { expression { isDeployBuild() } }
            steps {
                withCredentials([file(credentialsId: 'buy01-app-env', variable: 'APP_ENV_FILE')]) {
                    sh 'scripts/ci/build-images.sh'
                }
            }
        }

        stage('Deploy') {
            when { expression { isDeployBuild() } }
            steps {
                script {
                    env.PREVIOUS_TAG = sh(script: 'scripts/ci/running-tag.sh', returnStdout: true).trim()
                    echo "Running before this deploy: ${env.PREVIOUS_TAG ?: 'nothing deployed by this pipeline yet'}"
                }
                // A failed deploy must not stop the pipeline: the Rollback stage still has to run.
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    withCredentials([file(credentialsId: 'buy01-app-env', variable: 'APP_ENV_FILE')]) {
                        sh 'scripts/ci/deploy.sh "$IMAGE_TAG"'
                    }
                    script { env.DEPLOY_STATUS = 'deployed' }
                }
            }
        }

        stage('Smoke test') {
            when { expression { env.DEPLOY_STATUS == 'deployed' } }
            environment {
                SMOKE_FORCE_FAIL = "${params.SIMULATE_SMOKE_FAILURE}"
            }
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'scripts/ci/smoke-test.sh'
                    script { env.DEPLOY_STATUS = 'healthy' }
                }
            }
        }

        stage('Rollback') {
            when { expression { isDeployBuild() && env.DEPLOY_STATUS != 'healthy' } }
            steps {
                script {
                    if (!env.PREVIOUS_TAG) {
                        env.ROLLBACK_STATUS = 'impossible: no earlier version deployed by this pipeline'
                        error('Nothing to roll back to: this was the first deploy by the pipeline.')
                    }
                    env.ROLLBACK_STATUS = 'failed'
                }
                withCredentials([file(credentialsId: 'buy01-app-env', variable: 'APP_ENV_FILE')]) {
                    sh 'scripts/ci/deploy.sh "$PREVIOUS_TAG"'
                }
                sh 'SMOKE_FORCE_FAIL=false scripts/ci/smoke-test.sh'
                script { env.ROLLBACK_STATUS = "done, ${env.PREVIOUS_TAG} is live again" }
            }
        }

        stage('Clean old images') {
            when { expression { env.DEPLOY_STATUS == 'healthy' } }
            steps {
                sh 'scripts/ci/cleanup-images.sh'
            }
        }

        stage('Backend: code analysis') {
            agent {
                docker {
                    image 'maven:3.9.16-eclipse-temurin-21'
                    args '-v /home/jenkins-agent/.m2:/home/jenkins-agent/.m2 -v /home/jenkins-agent/.sonar:/home/jenkins-agent/.sonar --memory=1536m'
                    reuseNode true
                }
            }
            environment {
                MAVEN_OPTS = '-Xmx512m -Duser.home=/home/jenkins-agent'
                SONAR_USER_HOME = '/home/jenkins-agent/.sonar'
                XDG_CONFIG_HOME = '/tmp/.config'
            }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    dir('backend') {
                        sh 'mvn -B compile sonar:sonar -Dsonar.projectVersion="$IMAGE_TAG"'
                    }
                }
            }
        }
    }

    post {
        always {
            sendBuildEmail()
        }
        cleanup {
            cleanWs()
        }
    }
}

boolean isDeployBuild() {
    return env.BRANCH_NAME == env.DEPLOY_BRANCH && params.DEPLOY != false
}

String deploymentSummary() {
    if (!isDeployBuild()) {
        return "none (only ${env.DEPLOY_BRANCH} deploys, DEPLOY=${params.DEPLOY})"
    }
    if (env.DEPLOY_STATUS == 'healthy') {
        return "${env.IMAGE_TAG} deployed, smoke test passed"
    }
    if (env.ROLLBACK_STATUS) {
        return "${env.IMAGE_TAG} failed. Rollback ${env.ROLLBACK_STATUS}"
    }
    return 'not reached, the build or the tests failed'
}

void sendBuildEmail() {
    String result = currentBuild.currentResult
    // ${TEST_COUNTS,...} are email-ext tokens, filled from the published JUnit reports.
    String body = """Result: ${result}
Branch: ${env.BRANCH_NAME}
Commit: ${env.IMAGE_TAG} ${env.COMMIT_TITLE}
Tests: \${TEST_COUNTS,var="total"} run, \${TEST_COUNTS,var="fail"} failed
Deployment: ${deploymentSummary()}
Duration: ${currentBuild.durationString.replace(' and counting', '')}

Console: ${env.BUILD_URL}console
Test report: ${env.BUILD_URL}testReport/
"""
    emailext(
        to: env.NOTIFY_EMAIL,
        subject: "[Buy-01] ${result}: ${env.BRANCH_NAME} #${env.BUILD_NUMBER} (${env.IMAGE_TAG})",
        body: body,
        attachLog: result != 'SUCCESS',
        compressLog: true
    )
}
