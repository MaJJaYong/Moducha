import axiosInstance from '../../../api/axiosInstance';
import useAuthStore from '../../../stores/authStore';
import { AxiosResponse } from 'axios';
const { currentUserId } = useAuthStore.getState();

interface MypageTeatimeParticipateListRequestParams {
  sort?: 'before' | 'ongoing'; // 필터링 상태
  page?: number; // 페이지 번호 (기본값: 1)
  perPage?: number; // 페이지 당 항목 수 (기본값: 12)
}

interface MypageTeatimeParticipateListResponseItem {
  teatimeBoardId: number;
  title: string;
  content: string;
  createdDate: string;
  lastUpdated: string;
  nickname: string;
  maxParticipants: number;
  participants: number;
  viewCount: number;
}

interface MypageTeatimeParticipateListResponse {
  data: {
    items: MypageTeatimeParticipateListResponseItem[];
    pagination: {
      total: number;
      page: number;
      perPage: number;
    };
  };
}

async function getMyTeatimeParticipateList({
  sort,
  page = 1,
  perPage = 12,
}: MypageTeatimeParticipateListRequestParams): Promise<MypageTeatimeParticipateListResponse> {
  try {
    const response: AxiosResponse<MypageTeatimeParticipateListResponse> =
      await axiosInstance.get(
        `/users/${currentUserId}/mypage/participated-teatimes`,
        {
          params: {
            sort,
            page,
            perPage,
          },
        }
      );
    console.log(
      'getMyTeatimeParticipateList response.data is: ' + response.data
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching my teatime participate list:', error);
    throw error; // 오류 발생 시 호출한 쪽에서 처리하도록 예외를 던집니다.
  }
}

export default getMyTeatimeParticipateList;
