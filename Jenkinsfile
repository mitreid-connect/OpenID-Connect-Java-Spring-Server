#!/usr/bin/env groovy

pipeline {

  agent { label 'java11' }

  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '5'))
  }

  triggers { cron('@daily') }

  stages {

    stage('deploy') {
      steps {
        sh "mvn -U -B clean package deploy"
      }
    }
    
    stage('result'){
      steps {
        script { 
          currentBuild.result = 'SUCCESS' 
        }
      }
    }
  }
}
