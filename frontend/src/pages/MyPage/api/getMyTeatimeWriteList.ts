import axiosInstance from '../../../api/axiosInstance';
import useAuthStore from '../../../stores/authStore';
import { AxiosResponse } from 'axios';

const { currentUserId } = useAuthStore.getState();

// 기본적으로 최신순으로 정렬해서 백엔드에서 넘어옵니다.
interface MypageTeatimeWriteListRequestParams {
  page?: number; // 페이지 번호 (기본값: 1)
  perPage?: number; // 페이지 당 항목 수 (기본값: 12)
  sort?: string; // 정렬 기준 (기본값: 'latest')
}

interface MypageTeatimeWriteListResponseItem {
  boardId: number;
  title: string;
  content: string;
  createdDate: string;
  lastUpdated: string;
  broadcastDate: string;
  endDate: string;
  maxParticipants: number;
  participants: number;
  viewCount: number;
  nickname: string;
}

interface MypageTeatimeWriteListResponse {
  data: MypageTeatimeWriteListResponseItem[];
  pagination: {
    total: number;
    page: number;
    perPage: number;
  };
}

async function getMyTeatimeWriteList({
  page = 1, // 기본값
  perPage = 12,
  sort = 'latest', // 기본값
}: MypageTeatimeWriteListRequestParams): Promise<MypageTeatimeWriteListResponse> {
  try {
    const response: AxiosResponse<MypageTeatimeWriteListResponse> =
      await axiosInstance.get(`/users/${currentUserId}/mypage/teatimes`, {
        params: {
          page,
          perPage,
          sort, // `sort` 추가
        },
      });

    // console.log(
    //   'getMyTeatimeWriteList response.data is: ' +
    //     JSON.stringify(response.data, null, 2)
    // );

    return response.data;
  } catch (error) {
    console.error('Error fetching my teatime write list:', error);
    throw error; // 오류 발생 시 호출한 쪽에서 처리하도록 예외를 던집니다.
  }
}

export default getMyTeatimeWriteList;
