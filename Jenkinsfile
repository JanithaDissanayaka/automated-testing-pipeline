pipeline {

    agent any

    tools{
        maven 'Maven-3.9'
    }

    environment {

        APP_NAME = "demo-app"

        DOCKER_IMAGE =
            "demo-app:${BUILD_NUMBER}"

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
                sh 'mvn clean package'
            }
        }


        stage('Functional Testing') {

            steps {

                echo 'Running functional tests...'

                sh '''
                    ./mvnw test \
                    -Dtest=ProductFunctionalTest
                '''
            }

            post {

                always {

                    junit(
                        testResults:
                        'target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }


        stage('Integration Testing') {

            steps {

                echo 'Starting PostgreSQL...'

                sh '''
                    docker compose up -d postgres

                    sleep 10
                '''

                echo 'Running integration tests...'

                sh '''
                    ./mvnw test \
                    -Dtest=ProductIntegrationTest
                '''
            }
        }


        stage('Regression Testing') {

            steps {

                echo 'Running regression tests...'

                sh '''
                    ./mvnw test \
                    -Dtest=ProductRegressionTest
                '''
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

                echo 'Deploying to staging...'

                sh '''
                    docker stop ${APP_NAME}-staging || true

                    docker rm ${APP_NAME}-staging || true

                    docker run -d \
                        --name ${APP_NAME}-staging \
                        -p ${STAGING_PORT}:8080 \
                        -e DB_URL=jdbc:postgresql://host.docker.internal:5432/demo \
                        -e DB_USERNAME=postgres \
                        -e DB_PASSWORD=postgres \
                        ${DOCKER_IMAGE}
                '''

                sh '''
                    sleep 15
                '''
            }
        }


        stage('Acceptance Testing') {

            steps {

                echo 'Running acceptance tests...'

                sh '''
                    ./mvnw test \
                    -Dtest=ProductAcceptanceTest
                '''
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
                    message:
                    'All tests passed. Deploy to production?',
                    ok: 'Deploy'
                )
            }
        }


        stage('Deploy to Production') {

            steps {

                echo 'Deploying to production...'

                sh '''
                    docker stop ${APP_NAME}-production || true

                    docker rm ${APP_NAME}-production || true

                    docker run -d \
                        --name ${APP_NAME}-production \
                        -p ${PROD_PORT}:8080 \
                        -e DB_URL=jdbc:postgresql://host.docker.internal:5432/demo \
                        -e DB_USERNAME=postgres \
                        -e DB_PASSWORD=postgres \
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