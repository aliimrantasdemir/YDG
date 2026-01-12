pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    REPO_URL = 'https://github.com/aliimrantasdemir/YDG.git'
    BRANCH   = 'fix/jenkins-e2e'
    BASE_URL = 'http://app.local:8081'
    SELENIUM_REMOTE_URL = 'http://localhost:4445/wd/hub'
    DB_PATH = "${WORKSPACE}\\data\\lostfound.db"
  }

  stages {

    stage('Cleanup (previous)') {
      steps {
        bat 'cmd /c "docker compose down -v --remove-orphans || exit /b 0"'
      }
    }

    stage('Checkout') {
      steps {
        bat 'echo WORKSPACE=%CD%'
        script {
          deleteDir()
          git branch: env.BRANCH, url: env.REPO_URL
        }
        bat 'git rev-parse --short HEAD'
      }
    }

    stage('Docker erişimi var mı?') {
      steps {
        bat 'docker version'
        bat 'docker compose version'
      }
    }

    stage('Build backend jar (mvn package)') {
      steps {
        bat '''
          .\\mvnw.cmd -pl backend -am -DskipTests package
          if not exist backend\\target\\backend-1.0.0.jar (
            echo ERROR: backend jar not found!
            dir backend\\target
            exit /b 1
          )
          dir backend\\target\\backend-1.0.0.jar
        '''
      }
    }

    stage('Unit Tests (backend)') {
      steps {
        bat '''
          .\\mvnw.cmd -pl backend -am test
        '''
      }
    }

    stage('Integration Tests (backend)') {
      steps {
        bat '''
          .\\mvnw.cmd -pl backend -am verify
        '''
      }
    }

    stage('Compose Build (app)') {
      steps {
        bat 'docker compose build app'
      }
    }

    stage('Compose UP (app + selenium)') {
      steps {
        bat 'if not exist data mkdir data'
        bat 'docker compose up -d app selenium'
        bat 'docker compose ps'
        bat 'docker compose logs --no-color --tail=80 app'
        bat 'docker compose logs --no-color --tail=80 selenium'
      }
    }

    stage('App hazır mı? (wait)') {
      steps {
        bat '''
          docker compose exec -T selenium sh -lc "set -e; \
            for i in $(seq 1 60); do \
              status=$(curl -sI http://app.local:8081/login | head -n 1 || true); \
              echo status=$status; \
              echo $status | grep -Eqi '( 200 | 302 )' && echo READY && exit 0; \
              echo waiting-$i; sleep 2; \
            done; \
            echo NOT_READY; exit 1"
        '''

        bat '''
          powershell -NoProfile -Command ^
            "$ProgressPreference='SilentlyContinue';" ^
            "try { (Invoke-WebRequest -UseBasicParsing -TimeoutSec 3 http://localhost:8082/login) | Out-Null; Write-Host 'HOST_READY' } catch { Write-Host 'HOST_NOT_READY' }"
        '''
      }
    }

    stage('E2E Tests (1-2-3)') {
      steps {
        bat '''
          .\\mvnw.cmd -pl e2e-tests clean test ^
            "-Dtest=Scenario01_*,Scenario02_*,Scenario03_*" ^
            "-DbaseUrl=%BASE_URL%" ^
            "-DseleniumRemoteUrl=%SELENIUM_REMOTE_URL%" ^
            "-DdbPath=%DB_PATH%"
        '''
      }
    }
  }

  post {
    always {
      bat 'docker compose logs --no-color > docker-logs.txt'

      junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
      junit allowEmptyResults: true, testResults: 'backend/target/failsafe-reports/*.xml'
      junit allowEmptyResults: true, testResults: 'e2e-tests/target/surefire-reports/*.xml'

      archiveArtifacts artifacts: 'docker-logs.txt', allowEmptyArchive: true
      archiveArtifacts artifacts: 'e2e-tests/target/surefire-reports/**', allowEmptyArchive: true
      archiveArtifacts artifacts: 'e2e-tests/target/e2e-artifacts/**', allowEmptyArchive: true

      bat 'cmd /c "docker compose down -v --remove-orphans || exit /b 0"'
    }
  }
}
