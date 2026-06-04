pipeline {
    agent any

    environment {
        DOCKER_USER = 'anischamekh'
        DOCKER_REGISTRY = 'docker.io'
    }

    tools {
        jdk 'jdk17'
        maven 'Maven-3.9'
        nodejs 'nodejs22'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test (Maven)') {
            steps {
                dir('microservices') {
                    sh 'mvn -B clean verify'
                }
            }
        }

        stage('SonarQube') {
            steps {
                withSonarQubeEnv('SonarCloud') {
                    dir('microservices') {
                        sh '''
                            mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                              -Dsonar.organization=anischamekh \
                              -Dsonar.projectKey=anischamekh_MyStartUp_PFA \
                              -Dsonar.coverage.jacoco.xmlReportPaths=common-lib/target/site/jacoco/jacoco.xml,auth-service/target/site/jacoco/jacoco.xml,hrm-service/target/site/jacoco/jacoco.xml,project-service/target/site/jacoco/jacoco.xml,chatbot-service/target/site/jacoco/jacoco.xml,api-gateway/target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build --if-present'
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                  docker compose build auth-service hrm-service project-service chatbot-service api-gateway
                  for svc in auth-service hrm-service project-service chatbot-service api-gateway; do
                    docker tag mystartup_pfa-${svc}:latest ${DOCKER_USER}/mystartup-${svc}:latest
                    docker push ${DOCKER_USER}/mystartup-${svc}:latest
                  done
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                branch 'main'
            }

            steps {

                sh 'kubectl apply -f k8s/configmap.yaml'

                sh 'kubectl apply -f k8s/secrets.yaml'

                sh 'kubectl apply -f k8s/postgres-deployment.yaml'

                sh 'kubectl apply -f k8s/redis-deployment.yaml'

                sh 'kubectl apply -f k8s/zookeeper-deployment.yaml'

                sh 'kubectl apply -f k8s/kafka-deployment.yaml'

                sh 'kubectl apply -f k8s/auth-service-deployment.yaml'

                sh 'kubectl apply -f k8s/hrm-service-deployment.yaml'

                sh 'kubectl apply -f k8s/project-service-deployment.yaml'

                sh 'kubectl apply -f k8s/chatbot-service-deployment.yaml'

                sh 'kubectl apply -f k8s/api-gateway-deployment.yaml'

                sh 'kubectl apply -f k8s/ingress.yaml'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'microservices/**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'microservices/**/target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}