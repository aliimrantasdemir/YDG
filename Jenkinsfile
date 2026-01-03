pipeline {
  agent any

  options {
    skipDefaultCheckout(true)
  }

  environment {
    // Host (Jenkins Windows) tarafından kontrol edeceğimiz URL
    APP_HOST_URL = 'http://localhost:8082'

    // Selenium container içinden app servisine erişilecek URL
    APP_BASE_URL = 'http://app:8081'

    // Host'tan Selenium'a erişim (docker port mapping)
    SELENIUM_URL = 'http://localhost:4445/wd/hub'
  }

  stages {

    stage('1-Checkout') {
      steps {
        checkout scm
      }
    }

    // ✅ E2E burada kesinlikle çalışmayacak: sadece backend modülünü build ediyoruz
    stage('2-Build (backend only)') {
      steps {
        script {
          if (isUnix()) {
            sh "./mvnw -q -pl backend -am -DskipTests package"
          } else {
            bat "mvnw.cmd -q -pl backend -am -DskipTests package"
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
          junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
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
          junit allowEmptyResults: true, testResults: 'backend/target/failsafe-reports/*.xml'
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

    // ✅ Docker compose sonrası uygulama gerçekten ayağa kalktı mı?
    stage('5.5-Wait App Ready') {
      steps {
        script {
          if (isUnix()) {
            sh """
              set +e
              for i in \$(seq 1 30); do
                curl -fsS ${APP_HOST_URL}/login >/dev/null && exit 0
                sleep 2
              done
              exit 1
            """
          } else {
            bat """
              @echo off
              powershell -NoProfile -Command "\$u='${APP_HOST_URL}/login'; for(\$i=1;\$i -le 30;\$i++){ try{ \$r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 \$u; if(\$r.StatusCode -eq 200 -or \$r.StatusCode -eq 302){ exit 0 } } catch{} Start-Sleep -Seconds 2 }; exit 1"
            """
          }
        }
      }
    }

    stage('6-E2E') {
      steps {
        script {
          // ✅ E2E testleri selenium container'a bağlanır, baseUrl container içinden app:8081 olmalı
          if (isUnix()) {
            sh "./mvnw -q -pl e2e-tests -DbaseUrl=${APP_BASE_URL} -DseleniumUrl=${SELENIUM_URL} test"
          } else {
            bat "mvnw.cmd -q -pl e2e-tests -DbaseUrl=%APP_BASE_URL% -DseleniumUrl=%SELENIUM_URL% test"
          }
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'e2e-tests/target/surefire-reports/*.xml'
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
          bat "docker compose down -v || exit /b 0"
        }
      }
    }
  }
}
