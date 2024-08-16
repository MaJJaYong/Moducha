import axios, { AxiosError, AxiosResponse } from 'axios';
import { toast } from 'react-toastify'; // Toastify 임포트
import useAuthStore from '../stores/authStore'; // Zustand 상태 관리 훅

/**
 * axiosInstance.ts
 *  axios 인스턴스를 생성하고, 액세스 토큰이 만료된 경우 리프레시 토큰으로 새 액세스 토큰을 발급받는 로직을 처리합니다.
 *  로그인 관련 요청 등에는 이 axios 인스턴스를 사용하지 않아 성능을 향상시킬 수 있습니다.
 */

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 로그아웃 및 알림 처리를 위한 상태 트리거
function logoutLogic() {
  console.log('accessToken reissue에 실패하여 로그아웃합니다');
  localStorage.removeItem('authorization');

  // Toast 알림을 사용하여 사용자에게 정보 제공
  toast.error('로그인이 만료되었습니다. 재로그인이 필요합니다.', {
    onClose: () => {
      // Zustand 상태를 업데이트하여 로그아웃 상태로 설정
      const { setLoggedIn, setCurrentUsername } = useAuthStore.getState();
      setLoggedIn(false);
      setCurrentUsername('');
      // 리디렉션은 이 컴포넌트 외부에서 처리할 수 있도록 설계
    },
  });
}

// request 인터셉터 - 액세스 토큰을 헤더에 추가
axiosInstance.interceptors.request.use(
  function (config) {
    const token = localStorage.getItem('authorization');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  function (error) {
    console.log('error at request interceptor: ' + error);
    return Promise.reject(error);
  }
);

// response 인터셉터 - 401 오류 시 액세스 토큰 재발급 시도
axiosInstance.interceptors.response.use(
  function (response: AxiosResponse) {
    // 200대 status 일때 이 함수를 트리거 - 비워두면 됩니다.
    return response;
  },
  async (error: AxiosError) => {
    if (error.response?.status === 401) {
      try {
        await tokenRefresh(); // 정상 작동 시 localStorage에 갱신된 accessToken이 저장됩니다
        // 갱신된 accessToken으로 request 재요청
        const accessToken = localStorage.getItem('authorization');
        if (accessToken && error.config) {
          console.log(
            'axios interceptor가 새로운 accessToken을 받아옴',
            error.config
          );
          error.config.headers.Authorization = `Bearer ${accessToken}`;
          return axiosInstance(error.config);
        }
      } catch (refreshError) {
        logoutLogic();
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

// 리프레시 토큰으로 액세스 토큰을 갱신하는 함수
const tokenRefresh = async () => {
  try {
    const response = await axios.post(
      `${import.meta.env.VITE_API_URL}/reissue`,
      {},
      {
        headers: { 'Content-Type': 'application/json' },
      }
    );
    const newAccessToken = response.headers.authorization.replace(
      /^Bearer\s+/,
      ''
    );
    if (!newAccessToken) {
      console.log(
        '리프레시 토큰을 통해 액세스 토큰을 재발급하지 못했습니다. axios interceptor의 문제이거나 재로그인이 필요합니다'
      );
      return;
    }
    localStorage.setItem('authorization', newAccessToken);
  } catch (error) {
    console.error('리프레시 토큰 요청 중 오류 발생:', error);
    throw error;
  }
};

export default axiosInstance;
