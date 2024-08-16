import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

// API 요청을 위한 인스턴스 생성
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 닉네임 수정
export const updateNickname = async (accessToken: string, nickname: string) => {
  const response = await api.patch(
    '/users/nickname',
    { nickname },
    {
      headers: { Authorization: `Bearer ${accessToken}` },
    }
  );
  return response.data;
};

// 회원 탈퇴
export const deactivateAccount = async (
  accessToken: string,
  refreshToken: string
) => {
  const response = await api.patch(
    '/users/withdraw',
    {},
    {
      headers: { Authorization: `Bearer ${accessToken}` },
      withCredentials: true, // 쿠키를 포함한 요청
      credentials: 'include',
    }
  );
  return response.data;
};
