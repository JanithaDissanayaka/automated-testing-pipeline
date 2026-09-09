pipeline {

    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {

        APP_NAME = "demo-app"

        DOCKER_IMAGE = "demo-app:${BUILD_NUMBER}"

        DOCKER_NETWORK = "ci-network"

        STAGING_PORT = "8081"

        PROD_PORT = "8080"

        CONTAINER_PORT = "8081"
    }

    stages {

        stage('Checkout') {

            steps {

                echo '=========================================='
                echo 'Checking out source code...'
                echo '=========================================='

                checkout scm
            }
        }


        stage('Build') {

            steps {

                echo '=========================================='
                echo 'Building application...'
                echo '=========================================='

                sh 'mvn -version'

                sh '''
                    mvn clean package -DskipTests
                '''
            }
        }


        stage('Functional Testing') {

            steps {

                echo '=========================================='
                echo 'Running functional tests...'
                echo '=========================================='

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

                echo '=========================================='
                echo 'Running integration tests...'
                echo '=========================================='

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

                echo '=========================================='
                echo 'Running regression tests...'
                echo '=========================================='

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

                echo '=========================================='
                echo 'Running Trivy security scan...'
                echo '=========================================='

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

                echo '=========================================='
                echo 'Building Docker image...'
                echo '=========================================='

                sh '''
                    docker build \
                        -t ${DOCKER_IMAGE} \
                        .
                '''

                sh '''
                    docker images ${DOCKER_IMAGE}
                '''
            }
        }


        stage('Deploy to Staging') {

            steps {

                echo '=========================================='
                echo 'Deploying application to staging...'
                echo '=========================================='

                sh '''
                    docker rm -f ${APP_NAME} || true

                    docker run -d \
                        --name ${APP_NAME} \
                        --network ${DOCKER_NETWORK} \
                        -p ${STAGING_PORT}:${CONTAINER_PORT} \
                        -e SPRING_PROFILES_ACTIVE=docker \
                        ${DOCKER_IMAGE}
                '''

                sh '''
                    echo "Waiting for application to start..."

                    for i in {1..30}; do

                        if curl -sf http://localhost:${STAGING_PORT}/api/products > /dev/null; then
                            echo "Application is ready!"
                            exit 0
                        fi

                        echo "Application not ready yet... attempt $i/30"

                        sleep 2
                    done

                    echo "Application failed to start."

                    echo "========== Container Status =========="

                    docker ps -a

                    echo "========== Application Logs =========="

                    docker logs ${APP_NAME}

                    exit 1
                '''
            }
        }


        stage('Acceptance Testing') {

            steps {

                echo '=========================================='
                echo 'Running acceptance tests...'
                echo '=========================================='

                sh '''
                    mvn test \
                        -Dtest=ProductAcceptanceTest \
                        -DbaseUrl=http://${APP_NAME}:${CONTAINER_PORT}
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

                echo '=========================================='
                echo 'Running k6 load test...'
                echo '=========================================='

                sh '''
                    k6 run tests/load-test.js
                '''
            }
        }


        stage('Approve Production') {

            steps {

                echo '=========================================='
                echo 'Production deployment approval'
                echo '=========================================='

                input(
                    message: 'All tests passed. Deploy to production?',
                    ok: 'Deploy'
                )
            }
        }


        stage('Deploy to Production') {

            steps {

                echo '=========================================='
                echo 'Deploying application to production...'
                echo '=========================================='

                sh '''
                    docker rm -f ${APP_NAME}-production || true

                    docker run -d \
                        --name ${APP_NAME}-production \
                        --network ${DOCKER_NETWORK} \
                        -p ${PROD_PORT}:${CONTAINER_PORT} \
                        -e SPRING_PROFILES_ACTIVE=docker \
                        ${DOCKER_IMAGE}
                '''

                sh '''
                    echo "Waiting for production application..."

                    for i in {1..30}; do

                        if curl -sf http://localhost:${PROD_PORT}/api/products > /dev/null; then
                            echo "Production application is ready!"
                            exit 0
                        fi

                        echo "Production not ready yet... attempt $i/30"

                        sleep 2
                    done

                    echo "Production application failed to start."

                    echo "========== Container Status =========="

                    docker ps -a

                    echo "========== Production Logs =========="

                    docker logs ${APP_NAME}-production

                    exit 1
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

                 Application: demo-app

                 Staging:    http://localhost:8081
                 Production: http://localhost:8080

                 Database:   PostgreSQL
                 Network:    ci-network

            ==========================================
            '''
        }


        failure {

            echo '''
            ==========================================
                 CI/CD PIPELINE FAILED
            ==========================================

                 Check the failed stage above.

            ==========================================
            '''
        }


        always {

            echo 'Pipeline execution completed.'
        }
    }
}