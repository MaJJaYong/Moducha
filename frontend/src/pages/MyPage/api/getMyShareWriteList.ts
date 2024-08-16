//getMyShareWriteList.ts
import axiosInstance from '../../../api/axiosInstance';
import useAuthStore from '../../../stores/authStore';
import { AxiosResponse } from 'axios';
const { currentUserId } = useAuthStore.getState();

interface MypageShareWriteListRequestParams {
  page?: number; // 페이지 번호 (기본값: 1)
  perPage?: number; // 페이지 당 항목 수 (기본값: 12)
}

interface MypageShareWriteListResponseItem {
  shareBoardId: number;
  title: string;
  content: string;
  createdDate: string;
  lastUpdated: string;
  nickname: string;
  endDate: string;
  maxParticipants: number;
  participants: number;
  viewCount: number;
}

interface MypageShareWriteListResponse {
  data: {
    items: MypageShareWriteListResponseItem[];
    pagination: {
      total: number;
      page: number;
      perPage: number;
    };
  };
}

async function getMyShareWriteList({
  page = 1,
  perPage = 12,
}: MypageShareWriteListRequestParams): Promise<MypageShareWriteListResponse> {
  try {
    const response: AxiosResponse<MypageShareWriteListResponse> =
      await axiosInstance.get(`/users/${currentUserId}/mypage/shares`, {
        params: {
          page,
          perPage,
        },
      });
    console.log('getMyShareWriteList response.data is: ', response.data);
    return response.data;
  } catch (error) {
    console.error('Error fetching my share write list:', error);
    throw error; // 오류가 발생하면 호출한 쪽에서 처리하도록 예외를 던집니다.
  }
}

export default getMyShareWriteList;
