#!/bin/bash

#################
#
# init.sh
# 프로젝트를 실행하기 위한 초기화 스크립트
# 2024.07.31 created by uigeun-kim
#
#################

set -e  # 실패하면 바로 멈추도록 설정

read -p "Enter profile(dev or prod): " PROFILE

# 이전에 사용한 frontend, backend image를 삭제하는 스크립트
docker-compose --profile $PROFILE down || { echo "fail to docker compose down"; exit 1; } # docker-compose down
frontend_images=$(docker images -q moducha_frontend)  # frontend 이미지 id
if  [ -n "$frontend_images" ]; then
  docker rmi -f $frontend_images || { echo "fail to remove moducha_frontend image"; exit 1; } # frontend 이미지 제거
fi
backend_images=$(docker images -q moducha_backend)  # backend 이미지 id
if  [ -n "$backend_images" ]; then
  docker rmi -f $backend_images  || { echo "fail to remove moducha_backend image"; exit 1; } # backend 이미지 제거
fi

# frontend build
cd ./frontend
npm install || { echo "npm install failed"; exit 1; }
npm run build:$PROFILE || { echo "npm run build failed"; exit 1; }
cd ..

# backend build
cd ./backend
./gradlew clean build -x test || { echo "./gradlew clean build -x test failed"; exit 1; }
cd ..

# PROFILE이 dev인 경우, localhost.crt와 localhost.key가 존재하지 않으면 openssl로 인증서를 생성
if [ "$PROFILE" = "dev" ]; then
  if [ ! -f "./frontend/nginx/localhostCerts/localhost.crt" ] || [ ! -f "./frontend/nginx/localhostCerts/localhost.key" ]; then
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./frontend/nginx/localhostCerts/localhost.key -out ./frontend/nginx/localhostCerts/localhost.crt -subj "/C=KR/ST=Gyeongsangbuk-do/L=Gumi/O=SSAFY/OU=SSAFY/CN=localhost" || { echo "openssl 실패"; exit 1; }
  fi
fi

# docker compose up
# 프로젝트 최상단 디렉터리에 .env 파일을 설정하세요.
export PROFILE
docker-compose --profile $PROFILE up -d || { echo "docker-compose up failed"; exit 1; }

echo "success"  # 모든 단계가 성공적으로 완료되면 출력
# chrome 실행
cd /mnt/c/Program\ Files/Google/Chrome/Application
if [ $PROFILE = dev ]
then
  ./chrome.exe http://localhost
fi
