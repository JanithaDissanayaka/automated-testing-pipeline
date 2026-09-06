pipeline {

    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {

        APP_NAME = "demo-app"

        DOCKER_IMAGE = "demo-app:${BUILD_NUMBER}"

        STAGING_PORT = "8081"

        PROD_PORT = "8080"
    }

    stages {

        stage('Checkout') {

            steps {

                echo 'Checking out source code...'

                checkout scm
            }
        }


        stage('Build') {

            steps {

                echo 'Building application...'

                sh 'mvn -version'

                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }


        stage('Functional Testing') {

            steps {

                echo 'Running functional tests...'

                sh '''
                    mvn test -Dtest=ProductFunctionalTest
                '''
            }

            post {

                always {

                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }


        stage('Integration Testing') {

            steps {

                echo 'Running integration tests...'

                sh '''
                    mvn test -Dtest=ProductIntegrationTest
                '''
            }

            post {

                always {

                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }


        stage('Regression Testing') {

            steps {

                echo 'Running regression tests...'

                sh '''
                    mvn test -Dtest=ProductRegressionTest
                '''
            }

            post {

                always {

                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }


        stage('Security Testing') {

            steps {

                echo 'Running Trivy security scan...'

                sh '''
                    trivy fs \
                    --severity HIGH,CRITICAL \
                    --exit-code 1 \
                    .
                '''
            }
        }


        stage('Docker Build') {

            steps {

                echo 'Building Docker image...'

                sh '''
                    docker build \
                    -t ${DOCKER_IMAGE} .
                '''
            }
        }


        stage('Deploy to Staging') {

            steps {

                echo 'Deploying application to staging...'

                sh '''
                    docker rm -f ${APP_NAME} || true

                    docker run -d \
                        --name ${APP_NAME} \
                        --network ci-network \
                        -p ${STAGING_PORT}:8080 \
                        -e SPRING_PROFILES_ACTIVE=docker \
                        ${DOCKER_IMAGE}
                '''

                sh '''
                    echo "Waiting for application to start..."

                    sleep 15

                    docker ps
                '''
            }
        }


        stage('Acceptance Testing') {

            steps {

                echo 'Running acceptance tests...'

                sh '''
                    mvn test \
                    -Dtest=ProductAcceptanceTest \
                    -DbaseUrl=http://${APP_NAME}:8080
                '''
            }

            post {

                always {

                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }


        stage('Load Testing') {

            steps {

                echo 'Running k6 load test...'

                sh '''
                    k6 run tests/load-test.js
                '''
            }
        }


        stage('Approve Production') {

            steps {

                input(
                    message: 'All tests passed. Deploy to production?',
                    ok: 'Deploy'
                )
            }
        }


        stage('Deploy to Production') {

            steps {

                echo 'Deploying application to production...'

                sh '''
                    docker rm -f ${APP_NAME}-production || true

                    docker run -d \
                        --name ${APP_NAME}-production \
                        --network ci-network \
                        -p ${PROD_PORT}:8080 \
                        -e SPRING_PROFILES_ACTIVE=docker \
                        ${DOCKER_IMAGE}
                '''
            }
        }
    }


    post {

        success {

            echo '''
            ==========================================
                 CI/CD PIPELINE SUCCESSFUL
            ==========================================
            '''
        }

        failure {

            echo '''
            ==========================================
                 CI/CD PIPELINE FAILED
            ==========================================
            '''
        }

        always {

            echo 'Pipeline execution completed.'
        }
    }
}