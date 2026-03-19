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

# 호스트 docker 그룹 GID와 동일한 그룹 생성 후 jenkins 유저 추가
# GID 불일치 시 빌드 실패로 명시적 오류 처리
RUN groupadd -g ${DOCKER_GID} docker 2>/dev/null || \
    (getent group docker | grep -q ":${DOCKER_GID}:" || \
    (echo "Error: Group 'docker' exists with a different GID, or GID ${DOCKER_GID} is taken." && exit 1)) && \
    usermod -aG docker jenkins

USER jenkins
