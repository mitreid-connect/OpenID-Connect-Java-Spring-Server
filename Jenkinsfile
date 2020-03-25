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
		stage ('1.3.3 Build') {
            when {
                branch "1.3.x"
            }
            steps {
                sh "mvn versions:set -DnewVersion=1.3.3.GRESHAM-19"
                sh "mvn -N versions:update-child-modules"

                timeout(time: 10, unit: 'MINUTES') {
                    sh "mvn -B -V -U -T4 clean deploy -DaltReleaseDeploymentRepository=releases::default::https://nexus.greshamtech.com/content/repositories/thirdparty/"
                }
            }
            post {
                always{
                       archiveArtifacts caseSensitive: false, onlyIfSuccessful: true, allowEmptyArchive: true, artifacts: 'openid-connect-server-webapp/target/*.war'
                }
            }
        }
		stage ('Build') {
			when {
                not {
                   branch "1.3.x"
                }
            }
			steps {
				sh "mvn versions:set -DnewVersion=${env.BRANCH_NAME}.GRESHAM-SNAPSHOT"
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
