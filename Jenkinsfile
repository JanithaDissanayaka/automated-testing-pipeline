pipeline {

    agent any

    tools {
        maven 'Maven-3.9'
    }

    environment {

        APP_NAME = "demo-app"

        DOCKER_IMAGE = "demo-app:${BUILD_NUMBER}"

        DOCKER_NETWORK = "ci-network"

        CONTAINER_PORT = "8081"

        STAGING_PORT = "8081"

        PROD_PORT = "8080"

        DB_HOST = "postgres-db"

        DB_PORT = "5432"

        DB_NAME = "automated_testing"

        DB_USER = "appuser"

        DB_PASSWORD = "app123"
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
                    mvn test \
                    -Dtest=ProductFunctionalTest \
                    -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
                    -Dspring.datasource.username=${DB_USER} \
                    -Dspring.datasource.password=${DB_PASSWORD} \
                    -Dspring.jpa.hibernate.ddl-auto=update \
                    -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
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
                    mvn test \
                    -Pintegration-test \
                    -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
                    -Dspring.datasource.username=${DB_USER} \
                    -Dspring.datasource.password=${DB_PASSWORD} \
                    -Dspring.jpa.hibernate.ddl-auto=update \
                    -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
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
                    mvn test \
                    -Pregression-test \
                    -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
                    -Dspring.datasource.username=${DB_USER} \
                    -Dspring.datasource.password=${DB_PASSWORD} \
                    -Dspring.jpa.hibernate.ddl-auto=update \
                    -Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
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
                        --network ${DOCKER_NETWORK} \
                        -p ${STAGING_PORT}:${CONTAINER_PORT} \
                        -e SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
                        -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
                        -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
                        -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                        ${DOCKER_IMAGE}
                '''
            }

            post {
                always {
                    sh '''
                        echo "Waiting for staging application..."

                        for i in $(seq 1 30); do

                            if curl -sf http://localhost:${STAGING_PORT}/api/products > /dev/null; then
                                echo "Staging application is ready."
                                exit 0
                            fi

                            echo "Waiting... attempt $i"
                            sleep 2

                        done

                        echo "Staging application failed to start."
                        docker logs ${APP_NAME}
                        exit 1
                    '''
                }
            }
        }


        stage('Acceptance Testing') {
            steps {
                echo 'Running acceptance tests...'

                sh '''
                    mvn test \
                    -Pacceptance-test \
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
                        --network ${DOCKER_NETWORK} \
                        -p ${PROD_PORT}:${CONTAINER_PORT} \
                        -e SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
                        -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
                        -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
                        -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                        ${DOCKER_IMAGE}
                '''
            }

            post {
                always {
                    sh '''
                        echo "Waiting for production application..."

                        for i in $(seq 1 30); do

                            if curl -sf http://localhost:${PROD_PORT}/api/products > /dev/null; then
                                echo "Production application is ready."
                                exit 0
                            fi

                            echo "Waiting... attempt $i"
                            sleep 2

                        done

                        echo "Production application failed to start."
                        docker logs ${APP_NAME}-production
                        exit 1
                    '''
                }
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