pipeline {
  agent any

  environment {
    APP_BASE_URL = "http://localhost:8082"
    SELENIUM_URL = "http://localhost:4444/wd/hub"
  }

  stages {

    stage('1-Checkout') {
      steps {
        checkout scm
      }
    }

    stage('2-Build') {
      steps {
        script {
          if (isUnix()) {
            sh "./mvnw -q -DskipTests package"
          } else {
            bat "mvnw.cmd -q -DskipTests package"
          }
        }
      }
    }

    stage('3-Unit Tests') {
      steps {
        script {
          if (isUnix()) {
            sh "./mvnw -q -pl backend test"
          } else {
            bat "mvnw.cmd -q -pl backend test"
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: "backend/target/surefire-reports/*.xml"
        }
      }
    }

    stage('4-Integration Tests') {
      steps {
        script {
          if (isUnix()) {
            sh "./mvnw -q -pl backend verify"
          } else {
            bat "mvnw.cmd -q -pl backend verify"
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: "backend/target/failsafe-reports/*.xml"
        }
      }
    }

    stage('5-Run on Docker') {
      steps {
        script {
          if (isUnix()) {
            sh "docker compose up -d --build"
          } else {
            bat "docker compose up -d --build"
          }
        }
      }
    }

    stage('6-E2E') {
      steps {
        script {
          // E2E testlerinin baseUrl/seleniumUrl alması için
          if (isUnix()) {
            sh "./mvnw -q -pl e2e-tests -DbaseUrl=${APP_BASE_URL} -DseleniumUrl=${SELENIUM_URL} test"
          } else {
            bat "mvnw.cmd -q -pl e2e-tests -DbaseUrl=%APP_BASE_URL% -DseleniumUrl=%SELENIUM_URL% test"
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: "e2e-tests/target/surefire-reports/*.xml"
        }
      }
    }
  }

  post {
    always {
      script {
        if (isUnix()) {
          sh "docker compose down -v || true"
        } else {
          bat "docker compose down -v"
        }
      }
    }
  }
}
