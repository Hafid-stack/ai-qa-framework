pipeline {
    agent any
    environment {
        CI = 'true'
        CHROME_BIN = '/usr/bin/chromium'
        CHROMEDRIVER_BIN = '/usr/bin/chromedriver'
    }

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/surefire-reports/**', allowEmptyArchive: true
        }
    }
}
