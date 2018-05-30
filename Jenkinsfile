//
// CTC Auth Jenkins Pipeline
//
pipeline {
    agent any
	tools {
		maven 'Maven 3.3.9'
		jdk 'Java 8'
	}
	options {
		// Only keep 10 builds in total
		buildDiscarder(logRotator(numToKeepStr:'10', daysToKeepStr:'2'))

		// Display timestamps
		timestamps()

		// Prevent concurrent builds
		disableConcurrentBuilds();
	}
	stages {
		stage ('Build') {
			steps {
				timeout(time: 20, unit: 'MINUTES') {
					sh 'mvn versions:set -DnewVersion=1.3.3-${env.BUILD_NUMBER}'
				}
				timeout(time: 20, unit: 'MINUTES') {
                	sh 'mvn -B -V -U -T4 clean deploy -DaltDeploymentRepository=releases::default::https://nexus.greshamtech.com/content/repositories/thirdparty'
                }
			}
			post {
		        always{
                    archiveArtifacts caseSensitive: false, onlyIfSuccessful: true, allowEmptyArchive: true, artifacts: 'openid-connect-server-webapp/target/*.war'
		        }
		        success {
                    junit '**/target/surefire-reports/**/*.xml'
                }
			}
		}
	}
	post {
        always {
            deleteDir()
        }
    }
}
