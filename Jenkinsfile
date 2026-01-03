pipeline {
  agent any

  environment {
    MVN = "./mvnw"
    APP_BASE_URL = "http://localhost:8082"
    SELENIUM_URL = "http://localhost:4444/wd/hub"
  }

  stages {
    stage('1-Checkout') { steps { checkout scm } }
    stage('2-Build') { steps { script { if (isUnix()) sh "${MVN} -q -DskipTests package" else bat "${MVN} -q -DskipTests package" } } }
    stage('3-Unit Tests') { steps { script { if (isUnix()) sh "${MVN} -q -pl backend test" else bat "${MVN} -q -pl backend test" } } post { always { junit 'backend/target/surefire-reports/*.xml' } } }
    stage('4-Integration Tests') { steps { script { if (isUnix()) sh "${MVN} -q -pl backend verify" else bat "${MVN} -q -pl backend verify" } } post { always { junit 'backend/target/failsafe-reports/*.xml' } } }
    stage('5-Run on Docker') { steps { script { if (isUnix()) sh "docker compose up -d --build" else bat "docker compose up -d --build" } } }
    stage('6-E2E') { steps { script { if (isUnix()) sh "${MVN} -q -pl e2e-tests -DskipE2E=false test" else bat "${MVN} -q -pl e2e-tests -DskipE2E=false test" } } post { always { junit 'e2e-tests/target/surefire-reports/*.xml' } } }
  }

  post {
    always { script { if (isUnix()) sh "docker compose down -v || true" else bat "docker compose down -v" } }
  }
}
