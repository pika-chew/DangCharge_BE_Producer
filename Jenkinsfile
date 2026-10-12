// ============================================================
// DangCharge BE Producer CI Pipeline
//
// 실행 범위
//   - 작업 브랜치 → develop PR : Test → Build
//   - develop 머지             : Test → Build
//   - develop → main PR        : Test → Build
//   - main 머지                : Test → Build → Docker Build/Push → GitOps 업데이트
//
// [중요]
// 어떤 브랜치/PR에서 빌드가 시작될지는 이 파일이 아니라 Jenkins Multibranch Job의
// Branch Sources 설정(브랜치·PR 탐색 범위)이 정한다. 이 파일은 시작된 빌드 안에서
// main일 때만 이미지/GitOps 단계를 돌리도록 when 조건으로 나눈다.
//
// 필요 플러그인: Pipeline, GitHub Branch Source, Docker Pipeline,
//               Credentials Binding, JUnit, Workspace Cleanup
// ============================================================

pipeline {
  agent any

  options {
    disableConcurrentBuilds()                    // 같은 브랜치 빌드가 겹치지 않게 (GitOps push 충돌 방지)
    timeout(time: 30, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  environment {
    APP_NAME       = 'be-producer'      // 레포마다 다르게 (BE Consumer는 'be-consumer')

    // ===== main 단계용 — 아직 미정, 정해지면 수정 =====
    // TODO: GitOps 레포 확정 후 채우기, 레포 루트에 Dockerfile 추가 필요
    REGISTRY       = 'ghcr.io'
    IMAGE_REPO     = "${REGISTRY}/pika-chew/dangcharge-${APP_NAME}"  // GHCR 이미지 이름은 소문자만 가능
    GITOPS_REPO    = 'github.com/pika-chew/your-gitops-repo.git'     // https:// 없이 작성
    GITOPS_BRANCH  = 'main'
    GITOPS_FILE    = "apps/${APP_NAME}/deployment.yaml"               // 이미지 태그를 바꿀 매니페스트
    GITHUB_CRED    = 'github-cred'       // Jenkins Credentials ID (Username + PAT: repo, write:packages)
    // ==================================================
  }

  stages {
    stage('Prepare') {
      steps {
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
        }
        echo "${env.BUILD_KIND} / Image tag: ${env.IMAGE_TAG}"
      }
    }

    // ---------- 모든 이벤트에서 실행 ----------
    stage('CI') {
      agent {
        docker {
          image 'eclipse-temurin:17-jdk'   // Jenkins 서버에 JDK를 따로 설치하지 않아도 됨
          reuseNode true                   // 같은 workspace 사용 → 빌드 결과물(JAR)이 다음 단계로 이어짐
        }
      }
      environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle-home"   // 컨테이너 안 권한 문제 방지
      }
      stages {
        stage('Test') {
          steps {
            sh 'chmod +x gradlew'
            sh './gradlew test --no-daemon'
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
            docker build -t "$IMAGE_REPO:$IMAGE_TAG" .
            echo "$GH_TOKEN" | docker login "$REGISTRY" -u "$GH_USER" --password-stdin
            docker push "$IMAGE_REPO:$IMAGE_TAG"
            docker logout "$REGISTRY"
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
            git clone --branch "$GITOPS_BRANCH" "https://${GIT_USER}:${GIT_TOKEN}@${GITOPS_REPO}" gitops
            cd gitops

            # 매니페스트의 이미지 태그만 교체
            sed -i "s|image: ${IMAGE_REPO}:.*|image: ${IMAGE_REPO}:${IMAGE_TAG}|" "$GITOPS_FILE"

            git config user.name  "jenkins-ci"
            git config user.email "jenkins-ci@users.noreply.github.com"
            git add "$GITOPS_FILE"

            # 바뀐 게 없으면 종료 (같은 커밋 재빌드 등)
            git diff --cached --quiet && { echo "No manifest change"; exit 0; }

            git commit -m "ci(${APP_NAME}): update image tag to ${IMAGE_TAG}"
            git pull --rebase origin "$GITOPS_BRANCH"   # 다른 파이프라인과 동시에 push할 때 충돌 방지
            git push origin "$GITOPS_BRANCH"
          '''
        }
      }
    }
  }

  post {
    success {
      echo "✅ ${env.APP_NAME} CI 성공 (${env.BUILD_KIND}, ${env.IMAGE_TAG})"
    }
    failure {
      echo "❌ ${env.APP_NAME} CI 실패 (${env.BUILD_KIND})"
      // TODO: Slack/Discord 알림 추가
    }
    always {
      sh 'docker image rm "$IMAGE_REPO:$IMAGE_TAG" 2>/dev/null || true'   // Jenkins 서버 디스크 정리
      cleanWs()
    }
  }
}
