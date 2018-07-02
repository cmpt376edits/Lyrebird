pipeline {
    agent any

    environment {
        DISPLAY = ':99'
        SLEEP_T = '3'
        MAVEN_OPTS = '-XX:+TieredCompilation -XX:TieredStopAtLevel=1'
    }

    stages {
        stage('Environment') {
            steps {
                sh 'nohup Xvfb $DISPLAY -screen 0 1024x768x24 & sleep $SLEEP_T'
                sh 'touch .stalonetrayrc && nohup stalonetray &'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn clean test -Djava.awt.headless=false'
            }
        }
        stage('Code quality') {
            steps {
                sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar -Dsonar.host.url=https://sonar.tristan.moe'
            }
        }
        stage('PackageInstall') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        stage('Archive artifacts') {
            steps {
                archiveArtifacts 'lyrebird/target/lyrebird*.jar'
            }
        }
    }
    post('Cleanup') {
        always {
            sh 'kill -15 $(pgrep stalonetray) && rm .stalonetrayrc && kill -15 $(pgrep Xvfb)'
        }
    }
}
