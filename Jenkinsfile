pipeline {
  agent any
  options { timestamps() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Compose Up') {
      steps {
        sh '''
          docker compose up -d --build
          docker compose ps
        '''
      }
    }

    stage('Resolve App IP') {
      steps {
        sh '''
          APP_ID=$(docker compose ps -q app)
          APP_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$APP_ID")
          echo "APP_ID=$APP_ID"
          echo "APP_IP=$APP_IP"
          echo "$APP_IP" > .app_ip
        '''
      }
    }

    stage('Smoke Check from Selenium') {
      steps {
        sh '''
          APP_IP=$(cat .app_ip)
          # selenium container içinden app'e erişim testi (senin yaptığın curl)
          docker compose exec -T selenium sh -lc "curl -fsS -I http://$APP_IP:8081/login >/dev/null"
        '''
      }
    }

    stage('Run E2E 1-2-3') {
      steps {
        sh '''
          chmod +x mvnw || true
          APP_IP=$(cat .app_ip)

          DB_PATH="$WORKSPACE/data/lostfound.db"
          echo "DB_PATH=$DB_PATH"
          ls -lah "$WORKSPACE/data" || true

          ./mvnw -pl e2e-tests clean test \
            "-Dtest=Scenario01_*,Scenario02_*,Scenario03_*" \
            "-DbaseUrl=http://$APP_IP:8081" \
            "-DseleniumRemoteUrl=http://localhost:4445/wd/hub" \
            "-DdbPath=$DB_PATH"
        '''
      }
    }

    stage('Test Reports') {
      steps {
        junit 'e2e-tests/target/surefire-reports/*.xml'
        archiveArtifacts artifacts: 'e2e-tests/target/**', allowEmptyArchive: true
      }
    }
  }

  post {
    always {
      sh 'docker compose down -v || true'
    }
  }
}
