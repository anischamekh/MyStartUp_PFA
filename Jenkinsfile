pipeline {
    agent any

    environment {
        DOCKER_USER = 'anischamekh'
        DOCKER_REGISTRY = 'docker.io'
    }

    tools {
        jdk 'jdk17'
        maven 'Maven-3.9'
        nodejs 'nodejs20'
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
                        // Le token SONAR_TOKEN est automatiquement injecté par withSonarQubeEnv
                        // Il ne faut PAS le passer en argument -Dsonar.token
                        // Remplacer "votre-organisation" par votre nom d'organisation SonarCloud
                        sh '''
                            mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                              -Dsonar.organization=anischamekh \
                              -Dsonar.projectKey=anischamekh_MyStartUp_PFA \
                              -Dsonar.coverage.jacoco.xmlReportPaths=common-lib/target/site/jacoco/jacoco.xml,auth-service/target/site/jacoco/jacoco.xml,hrm-service/target/site/jacoco/jacoco.xml,project-service/target/site/jacoco/jacoco.xml,chatbot-service/target/site/jacoco/jacoco.xml
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
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'microservices/**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'microservices/**/target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}