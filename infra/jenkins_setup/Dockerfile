FROM jenkins/jenkins:lts

USER root

ARG DOCKER_GID=988

# Docker CLI 및 Docker Compose 플러그인 설치
RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
    tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli docker-compose-plugin rsync && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# JDK 17 설치 (Gradle 테스트 실행용)
RUN apt-get update && \
    apt-get install -y wget apt-transport-https gnupg && \
    wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo "$VERSION_CODENAME") main" | \
    tee /etc/apt/sources.list.d/adoptium.list > /dev/null && \
    apt-get update && \
    apt-get install -y temurin-17-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 1단계: docker 그룹 생성 시도 (이미 존재하면 무시)
RUN groupadd -g ${DOCKER_GID} docker 2>/dev/null || true

# 2단계: 생성된 docker 그룹의 GID가 호스트와 일치하는지 검증
RUN getent group docker | grep -q ":${DOCKER_GID}:" || \
    (echo "Error: Group 'docker' exists with a different GID, or GID ${DOCKER_GID} is taken." && exit 1)

# 3단계: jenkins 유저를 docker 그룹에 추가
RUN usermod -aG docker jenkins

USER jenkins
