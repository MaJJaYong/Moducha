## 목차
- [버전 정보](#버전-정보)
- [환경변수](#환경변수)
  - [frontend .env](#frontend-env)
  - [backend .env](#backend-env)
- [배포 시 주의사항](#배포-시-주의사항)
- [시나리오](#시나리오)

<br>

# 버전 정보

- Intellij 2024.1.4
- Vscode 1.90.2
- Node 20.15.0
- npm 10.8.1
- MySQL 8.4.1
- SpringBoot 3.3.0
- JPA
- Nginx 1.26.1
- OpenVidu v3.0.0-beta2
- Ubuntu 22.04 LTS
- docker 27.1.1


---
# 환경변수

## frontend .env
- 민감한 정보는 알잘딱하게 대충 채워주세요

  ```json
  {/* .env-cmdrc.json */}
  {
    "prod": {
      "VITE_API_URL": "https://moducha.site/api/v1",
      "VITE_TEST_APP_SERVER_URL": "https://moducha.site/api/v1",
      "VITE_APP_SERVER_URL": "",
      "VITE_LIVETKIT_URL": "https://livekit.moducha.site"
    }
  }
  
  {/* .env */ }
  VITE_DOCK_APP_SERVER_URL=https://localhost:443/api/v1
  VITE_DOCK_LIVEKIT_URL=https://livekit.moducha.site:8443
  
  VITE_DP_APP_SERVER_URL=https://moducha.site/api/v1
  VITE_DP_LIVETKIT_URL=https://livekit.moducha.site:8443
  ```

  

## backend .env
- 

---
# 배포 시 주의사항
- certbot을 통해서 HTTPS 인증서를 발급받아야 합니다.
- openvidu 설정 파일을 수정해야 합니다.
  - caddy.yaml에 http, https 포트번호를 설정해야 합니다.
  - livekit.yaml에 webhook 받는 주소를 수정해야 합니다.

---
# 시나리오
1. moducha.site로 접속한다.
2. 메인 페이지에서 로그인한다.

---