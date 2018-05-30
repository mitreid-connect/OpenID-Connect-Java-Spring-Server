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
				sh "mvn versions:set -DnewVersion=1.3.3.GRESHAM-SNAPSHOT"
				sh "mvn -N versions:update-child-modules"
				timeout(time: 10, unit: 'MINUTES') {
                	sh "mvn -B -V -U -T4 clean deploy -DaltSnapshotDeploymentRepository=snapshots::default::https://nexus.greshamtech.com/content/repositories/third-party-snapshots/"
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
