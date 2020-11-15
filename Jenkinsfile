pipeline {
    agent any
    environment{
          PATH = "/usr/bin/mvn:$PATH"
        }
    stages {
            stage("clone code") {
                steps {
                  git credentialsId:'git_credentials' url:'https://github.com/Mogaka1994/cellulant.git'
                }
            }
        stage("Build code") {
            steps {
                sh "mvn clean install"
            }
        }
        stage("Test Internal Tests") {
            steps {
                echo 'Testing..'
                sh "mvn clean test"
            }
        }
        stage("Package to Deploy") {
            steps {
                sh "mvn clean package"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "scp /home/moha/.jenkins/workspace/Build/target/chama-0.0.1-SNAPSHOT.jar moha@192.168.100.28:/home/moha/.jenkins/workspace/Build/target/"
                sh "scp /home/moha/.jenkins/workspace/Build/target/application.yml moha@192.168.100.28:/home/moha/.jenkins/workspace/Build/target/"
                sh "nohup java -jar moha@192.168.100.28:/home/moha/.jenkins/workspace/Build/target/chama-0.0.1-SNAPSHOT.jar"
            }
        }
    }
}