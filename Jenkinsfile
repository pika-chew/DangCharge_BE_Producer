// ============================================================
// DangCharge BE Producer CI Pipeline
//
// 실행 범위
//   - 작업 브랜치 → develop PR : Test → Build
//   - develop 머지             : Test → Build
//   - develop → main PR        : 생략 (develop 머지 때 이미 검증한 코드라 바뀌는 게 없음)
//                                체크아웃도 하지 않고 바로 성공, Slack 알림도 보내지 않음
//   - main 머지                : Test → Build → Docker Build/Push → GitOps 업데이트
//
// [중요]
// 어떤 브랜치/PR에서 빌드가 시작될지는 이 파일이 아니라 Jenkins Multibranch Job의
// Branch Sources 설정(브랜치·PR 탐색 범위)이 정한다. 이 파일은 시작된 빌드 안에서
// main일 때만 이미지/GitOps 단계를 돌리도록 when 조건으로 나눈다.
//
// 필요 플러그인: Pipeline, GitHub Branch Source, Docker Pipeline,
//               Credentials Binding, JUnit, Workspace Cleanup, Timestamper
// 필요 Credentials:
//   - github-cred       : Username + PAT (repo, write:packages) — GHCR push, GitOps 레포 push
//   - slack-webhook-url : Secret text, Slack Incoming Webhook URL (없으면 알림만 건너뜀)
// ============================================================

// Slack Incoming Webhook으로 알림. 실패해도 빌드 결과에는 영향 주지 않음
def notifySlack(String color, String text) {
  try {
    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK_URL')]) {
      def payload = groovy.json.JsonOutput.toJson([attachments: [[color: color, text: text]]])
      withEnv(["SLACK_PAYLOAD=${payload}"]) {
        sh 'curl -fsS -X POST -H "Content-Type: application/json" --data "$SLACK_PAYLOAD" "$SLACK_WEBHOOK_URL" > /dev/null'
      }
    }
  } catch (err) {
    echo "Slack 알림 실패 (빌드 결과에는 영향 없음): ${err}"
  }
}

// develop → main PR 빌드인지. 이 PR은 바로 머지하는 경우가 많아서, 체크아웃을 하면
// 머지 직후 GitHub이 PR 머지 ref를 지워 체크아웃이 실패하고 실패 알림이 간다
def isReleasePr() {
  return env.CHANGE_BRANCH == 'develop' && env.CHANGE_TARGET == 'main'
}

pipeline {
  agent any

  options {
    disableConcurrentBuilds()                    // 같은 브랜치 빌드가 겹치지 않게 (GitOps push 충돌 방지)
    timeout(time: 30, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '20'))
    timestamps()
    skipDefaultCheckout()                        // 체크아웃은 Prepare에서 (develop → main PR은 체크아웃하지 않음)
  }

  environment {
    APP_NAME       = 'be-producer'      // 레포마다 다르게 (BE Consumer는 'be-consumer')

    // Gradle 캐시 볼륨을 main과 나머지로 분리. PR 코드가 캐시에 남긴 init 스크립트 등이
    // main 빌드(배포 이미지에 들어갈 JAR)에 섞이지 않도록 한다
    GRADLE_CACHE   = "${env.BRANCH_NAME == 'main' ? 'ci-gradle-cache-main' : 'ci-gradle-cache-ci'}"

    // ===== 테스트용 MySQL (빌드마다 띄웠다가 지움, CI 전용이라 비밀번호는 비밀 아님) =====
    DB_USERNAME    = 'ci'
    DB_PASSWORD    = 'ci-password'
    DB_SCHEMA      = 'src/main/resources/db/ev_charging_schema.sql'  // ddl-auto: validate가 이 스키마와 엔티티를 비교

    // ===== main 단계용 =====
    REGISTRY       = 'ghcr.io'
    IMAGE_REPO     = "${REGISTRY}/pika-chew/dangcharge-${APP_NAME}"  // GHCR 이미지 이름은 소문자만 가능
    GITOPS_REPO    = 'github.com/pika-chew/DangCharge_GitOps.git'    // https:// 없이 작성
    GITOPS_BRANCH  = 'main'                                          // 일단은 main 브랜치로 push, 사람이 확인하는 단계를 두고 싶으면 PR 방식으로 변경 가능
    GITOPS_FILE    = 'manifests/workloads/command-api/kustomization.yaml'  // 이미지를 바꿀 Kustomize 파일
    KUSTOMIZE_IMAGE = 'command-api'                                  // 그 파일 images 항목의 name
    GITHUB_CRED    = 'github-cred'
  }

  stages {
    // ---------- develop → main PR을 뺀 모든 이벤트에서 실행 ----------
    stage('Prepare') {
      when {
        not { changeRequest branch: 'develop', target: 'main' }   // 두 조건 모두 맞을 때만 = develop → main PR
      }
      steps {
        checkout scm

        script {
          // 이미지 태그 = 커밋 SHA 앞 7자리
          env.IMAGE_TAG = sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()

          // 어떤 경우로 실행됐는지 빌드 목록에 표시 (PR 빌드면 CHANGE_* 변수가 채워짐)
          if (env.CHANGE_ID) {
            env.BUILD_KIND = "PR #${env.CHANGE_ID}: ${env.CHANGE_BRANCH} → ${env.CHANGE_TARGET}"
          } else {
            env.BUILD_KIND = "Push/Merge: ${env.BRANCH_NAME}"
          }
          currentBuild.description = "${env.BUILD_KIND} (${env.IMAGE_TAG})"

          // 이 빌드 전용 테스트 DB 컨테이너 이름 (브랜치 이름의 %, / 등 컨테이너 이름에 못 쓰는 문자는 -로)
          env.DB_CONTAINER = "ci-mysql-${env.BUILD_TAG}".replaceAll('[^a-zA-Z0-9_.-]', '-')
        }
        echo "${env.BUILD_KIND} / Image tag: ${env.IMAGE_TAG}"

        // 빌드 컨테이너용 네트워크 (MTU 1450). 기본 브리지(MTU 1500)는 VM NIC(1450)보다 커서
        // 외부 HTTPS 핸드셰이크가 멈춘다. 이미 있으면 그대로 쓰고, 다른 빌드가 동시에 만들어도 통과
        sh '''
          docker network inspect ci-net-mtu1450 >/dev/null 2>&1 \
            || docker network create --opt com.docker.network.driver.mtu=1450 ci-net-mtu1450 \
            || docker network inspect ci-net-mtu1450 >/dev/null
        '''

        // Gradle 캐시 볼륨 (Gradle 배포판·의존성을 빌드마다 다시 받지 않도록 같은 볼륨을 쓰는 빌드끼리 공유)
        // 새 볼륨은 root 소유라 빌드 컨테이너(jenkins 사용자)가 쓸 수 있게 소유자를 맞춘다
        sh '''
          docker volume create "$GRADLE_CACHE" >/dev/null
          docker run --rm -u 0 -v "$GRADLE_CACHE:/cache" eclipse-temurin:17-jdk chown "$(id -u):$(id -g)" /cache
        '''
      }
    }

    // 테스트할 때 쓸 임시 MySQL을 띄우는 단계
    stage('Test DB') {    
      when {
        not { changeRequest branch: 'develop', target: 'main' }   // 두 조건 모두 맞을 때만 = develop → main PR
      }
      steps {
        sh '''
          [ -f "$DB_SCHEMA" ] || { echo "스키마 파일 없음: $DB_SCHEMA"; exit 1; }

          docker run -d --name "$DB_CONTAINER" --network ci-net-mtu1450 \
            -e MYSQL_ROOT_PASSWORD="$DB_PASSWORD" \
            -e MYSQL_DATABASE=dangcharge \
            -e MYSQL_USER="$DB_USERNAME" \
            -e MYSQL_PASSWORD="$DB_PASSWORD" \
            mysql:8.4

          # 초기화용 임시 서버는 TCP를 열지 않으므로, TCP ping이 되면 진짜 서버가 뜬 것 (최대 2분)
          for i in $(seq 1 60); do
            docker exec "$DB_CONTAINER" mysqladmin ping -h127.0.0.1 --silent 2>/dev/null && break
            [ "$i" = 60 ] && { echo "MySQL이 2분 안에 준비되지 않음"; docker logs "$DB_CONTAINER"; exit 1; }
            sleep 2
          done

          docker exec -i "$DB_CONTAINER" \
            mysql -h127.0.0.1 -u"$DB_USERNAME" -p"$DB_PASSWORD" dangcharge < "$DB_SCHEMA"
        '''
      }
    }

    stage('CI') {
      // develop → main PR은 건너뜀. 빌드는 성공으로 끝나므로 GitHub PR 체크는 통과 상태로 남는다
      when {
        beforeAgent true                         // 건너뛸 때는 빌드 컨테이너도 띄우지 않음
        not { changeRequest branch: 'develop', target: 'main' }   // 두 조건 모두 맞을 때만 = develop → main PR
      }
      agent {
        docker {
          image 'eclipse-temurin:17-jdk'   // Jenkins 서버에 JDK를 따로 설치하지 않아도 됨
          reuseNode true                   // 같은 workspace 사용 → 빌드 결과물(JAR)이 다음 단계로 이어짐
          // MTU 1450 네트워크(테스트 DB도 여기 있음) + Gradle 캐시 볼륨
          args "--network ci-net-mtu1450 -v ${env.GRADLE_CACHE}:/gradle-cache"
        }
      }
      environment {
        GRADLE_USER_HOME = '/gradle-cache'
      }
      stages {
        stage('Test') {
          steps {
            sh '''
              export DB_URL="jdbc:mysql://${DB_CONTAINER}:3306/dangcharge?useSSL=false&allowPublicKeyRetrieval=true"
              ./gradlew test --no-daemon
            '''
          }
          post {
            always {
              junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
            }
          }
        }
        stage('Build') {
          steps {
            sh './gradlew bootJar -x test --no-daemon'
          }
        }
      }
    }

    // ---------- main 머지 때만 실행 ----------
    stage('Docker Build & Push') {
      when { branch 'main' }
      steps {
        withCredentials([usernamePassword(credentialsId: env.GITHUB_CRED,
                                          usernameVariable: 'GH_USER',
                                          passwordVariable: 'GH_TOKEN')]) {
          sh '''
            # docker build는 사용자 정의 네트워크를 못 써서 host 네트워크(VM NIC MTU 그대로) 사용
            docker build --network host -t "$IMAGE_REPO:$IMAGE_TAG" .

            # 로그인 정보는 이 빌드 전용 임시 디렉터리에만 저장하고, push 실패로 중간에 끝나도 지운다
            export DOCKER_CONFIG="$(mktemp -d)"
            trap 'rm -rf "$DOCKER_CONFIG"' EXIT
            echo "$GH_TOKEN" | docker login "$REGISTRY" -u "$GH_USER" --password-stdin
            docker push "$IMAGE_REPO:$IMAGE_TAG"
          '''
        }
      }
    }

    stage('Update GitOps Repo') {
      when { branch 'main' }
      steps {
        withCredentials([usernamePassword(credentialsId: env.GITHUB_CRED,
                                          usernameVariable: 'GIT_USER',
                                          passwordVariable: 'GIT_TOKEN')]) {
          sh '''
            rm -rf gitops
            git clone --depth 1 --branch "$GITOPS_BRANCH" "https://${GIT_USER}:${GIT_TOKEN}@${GITOPS_REPO}" gitops
            cd gitops

            # 매니페스트를 아직 안 만들었으면 이미지 push까지만 하고 넘어감
            if [ ! -f "$GITOPS_FILE" ]; then
              echo "WARN: $GITOPS_FILE 없음 → GitOps 업데이트 생략 (이미지는 $IMAGE_REPO:$IMAGE_TAG 로 push됨)"
              exit 0
            fi

            # kustomization.yaml의 images 중 name이 KUSTOMIZE_IMAGE인 항목의 newName·newTag만 교체 (주석은 유지)
            # 태그는 따옴표로 감싼다. 짧은 SHA가 숫자로만 되면 YAML이 숫자로 읽기 때문
            # 항목이나 두 줄 중 하나라도 못 찾으면 조용히 넘어가지 않고 실패
            awk -v name="$KUSTOMIZE_IMAGE" -v repo="$IMAGE_REPO" -v tag="$IMAGE_TAG" '
              /^[^ #]/                              { in_images = /^images:/ }
              in_images && /^ *- name:/             { target = ($0 ~ ("- name: *\\"?" name "\\"? *$")) }
              in_images && target && /^ *newName:/  { sub(/newName:.*/, "newName: " repo); n++ }
              in_images && target && /^ *newTag:/   { sub(/newTag:.*/, "newTag: \\"" tag "\\""); t++ }
              { print }
              END { if (n != 1 || t != 1) exit 1 }
            ' "$GITOPS_FILE" > "$GITOPS_FILE.tmp" \
              || { echo "$GITOPS_FILE 에서 images name '$KUSTOMIZE_IMAGE'의 newName/newTag를 찾지 못함"; exit 1; }
            mv "$GITOPS_FILE.tmp" "$GITOPS_FILE"

            git config user.name  "jenkins-ci"
            git config user.email "jenkins-ci@users.noreply.github.com"
            git add "$GITOPS_FILE"

            # 바뀐 게 없으면 종료 (같은 커밋 재빌드 등)
            git diff --cached --quiet && { echo "No manifest change"; exit 0; }

            git commit -m "chore: update ${APP_NAME} image tag to ${IMAGE_TAG}"
            git pull --rebase origin "$GITOPS_BRANCH"   # 다른 파이프라인과 동시에 push할 때 충돌 방지
            git push origin "$GITOPS_BRANCH"
          '''
        }
      }
    }
  }

  post {
    success {
      script {
        if (env.BRANCH_NAME == 'main') {
          notifySlack('good', "✅ ${env.APP_NAME} main 배포 이미지 push 완료: ${env.IMAGE_REPO}:${env.IMAGE_TAG}\n<${env.BUILD_URL}|빌드 #${env.BUILD_NUMBER}>")
        } else if (env.BRANCH_NAME == 'develop') {   // develop 머지
          notifySlack('good', "✅ ${env.APP_NAME} ${env.BRANCH_NAME} 머지 빌드 성공 (${env.IMAGE_TAG})\n<${env.BUILD_URL}|빌드 #${env.BUILD_NUMBER}>")
        }
      }
    }
    fixed {
      script {
        if (!(env.BRANCH_NAME in ['main', 'develop']) && !isReleasePr()) {   // main·develop은 위 success에서 매번 알림
          notifySlack('good', "✅ ${env.APP_NAME} CI 복구 (${env.BUILD_KIND})\n<${env.BUILD_URL}|빌드 #${env.BUILD_NUMBER}>")
        }
      }
    }
    failure {
      script {
        if (!isReleasePr()) {
          // Prepare 전에 실패하면 BUILD_KIND가 없으므로 브랜치 이름으로 대신 표시
          notifySlack('danger', "❌ ${env.APP_NAME} CI 실패 (${env.BUILD_KIND ?: env.BRANCH_NAME})\n<${env.BUILD_URL}|빌드 #${env.BUILD_NUMBER}>")
        }
      }
    }
    cleanup {
      sh '''
        docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true        # 테스트 DB 정리
        docker image rm "$IMAGE_REPO:$IMAGE_TAG" >/dev/null 2>&1 || true   # Jenkins 서버 디스크 정리
      '''
      cleanWs()
    }
  }
}
