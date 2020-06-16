pipeline {
	agent any
	tools {
		maven 'Maven 3.6.3'
		jdk 'Corretto 8.252'
	}
	environment {
		VERSION = ''
		NEW_VERSION = ''
	}
	parameters {
		booleanParam(name: 'RELEASE', defaultValue: false, description: 'Release a new version of the open id connect component')
	}
	options {
		// Only keep 10 builds in total
		buildDiscarder(logRotator(numToKeepStr:'10', daysToKeepStr:'2'))

		// Display timestamps
		timestamps()

		// Prevent concurrent builds
		disableConcurrentBuilds()
	}
	stages {
//		stage ('Discover Version Number') {
//			steps {
//				script {
//					def pom = readMavenPom file: 'pom.xml'
//					def currentVersion = pom.getVersion()
//
//					VERSION = currentVersion.substring(0, currentVersion.indexOf('-SNAPSHOT'))
//
//					def parts = VERSION.tokenize('-')
//					def currentGreshamVersion = parts[1].toInteger()
//
//					parts.remove(1)
//					NEW_VERSION = parts.join('-') + '-' + (currentGreshamVersion + 1)
//				}
//			}
//		}
		stage ('1.3.3 Release Build') {
			when {
				expression {
					return BRANCH_NAME == "1.3.x" && params.RELEASE
				}
			}
			steps {
				sh "mvn versions:set -B -DnewVersion=$VERSION"
				sh "mvn -N -B versions:update-child-modules"
				script {
					sh "git commit --all --message 'Creating Release $VERSION'"
					sh "git tag --annotate v$VERSION --message 'Creating Release $VERSION'"
					sh "git push origin HEAD:${BRANCH_NAME} --tags"
				}
				timeout(time: 10, unit: 'MINUTES') {
					withMaven(options: [junitPublisher(disabled: true)]) {
						sh "mvn -B -V -U -T4 clean deploy -DaltReleaseDeploymentRepository=releases::default::https://nexus.greshamtech.com/repository/thirdparty-maven-releases/"
					}
				}
			}
			post {
				success {
					junit '**/target/surefire-reports/**/*.xml'
				}
			}
		}
		stage ('1.3.3 Snapshot Build') {
			when {
				expression {
					return BRANCH_NAME == "1.3.x" && !params.RELEASE
				}
			}
			steps {
				timeout(time: 10, unit: 'MINUTES') {
					withMaven(options: [junitPublisher(disabled: true)]) {
						sh "mvn -B -V -U -T4 clean deploy -DaltSnapshotDeploymentRepository=snapshots::default::https://nexus.greshamtech.com/repository/thirdparty-maven-snapshots/ -DaltReleaseDeploymentRepository=releases::default::https://nexus.greshamtech.com/repository/thirdparty-maven-releases/"
					}
				}
			}
			post {
				success {
					junit '**/target/surefire-reports/**/*.xml'
				}
			}
		}
		stage ('Feature Branch Build') {
			when {
				not {
				   branch "1.3.x"
				}
			}
			steps {
				timeout(time: 10, unit: 'MINUTES') {
					withMaven(options: [junitPublisher(disabled: true)]) {
						sh "mvn versions:set -B -DnewVersion=${env.BRANCH_NAME}.GRESHAM-SNAPSHOT"
						sh "mvn -N -B versions:update-child-modules"
						sh "mvn -B -V -U -T4 clean deploy -DaltSnapshotDeploymentRepository=snapshots::default::https://nexus.greshamtech.com/repository/thirdparty-maven-snapshots/"
					}
				}
			}
			post {
				success {
					junit '**/target/surefire-reports/**/*.xml'
				}
			}
		}
		stage ('Bump Development Version') {
			when {
				expression {
					return BRANCH_NAME == '1.3.x' && params.RELEASE
				}
			}
			steps {
				script {
					sh "mvn versions:set -DnewVersion=${NEW_VERSION}-SNAPSHOT --batch-mode"
					sh "git commit --all --message 'New Development Version $NEW_VERSION-SNAPSHOT'"
					sh "git push origin HEAD:${BRANCH_NAME}"
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
