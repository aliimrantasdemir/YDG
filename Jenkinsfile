pipeline {
  agent any

  environment {
    APP_HOST_URL = "http://localhost:8082"
    APP_BASE_URL = "http://app:8081"
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

    stage('5.5-Wait App Ready') {
      steps {
        script {
          if (isUnix()) {
            sh """
              URL='${APP_HOST_URL}/login'
              for i in \$(seq 1 60); do
                code=\$(curl -s -o /dev/null -w "%{http_code}" "\$URL" || true)
                if [ "\$code" = "200" ] || [ "\$code" = "302" ]; then
                  echo "App ready: \$URL (\$code)"
                  exit 0
                fi
                sleep 2
              done
              echo "App not ready: \$URL"
              exit 1
            """
          } else {
            bat """
              powershell -NoProfile -Command "$u='${APP_HOST_URL}/login'; for(\$i=1;\$i -le 60;\$i++){ try{ \$r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 \$u; if(\$r.StatusCode -eq 200 -or \$r.StatusCode -eq 302){ Write-Host 'App ready:' \$u \$r.StatusCode; exit 0 } } catch{} Start-Sleep -Seconds 2 }; Write-Host 'App not ready:' \$u; exit 1"
            """
          }
        }
      }
    }

    stage('6-E2E') {
      steps {
        script {
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
          bat "docker compose down -v || exit /b 0"
        }
      }
    }
  }
}
