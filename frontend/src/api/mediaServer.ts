import axios from 'axios';

/**
 * TeaTime App server & LiveKit media server
 * 싸피 내 테스트 환경(도커 환경)과 배포 환경 구분
 */

export const appServerAxiosInstance = axios.create({
  baseURL: window.location.hostname === 'localhost' ? 
    import.meta.env.VITE_DOCK_APP_SERVER_URL 
    : import.meta.env.VITE_DP_APP_SERVER_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const liveKitURL = window.location.hostname === 'localhost' ?
  import.meta.env.VITE_DOCK_LIVEKIT_URL
  : import.meta.env.VITE_DP_LIVETKIT_URL