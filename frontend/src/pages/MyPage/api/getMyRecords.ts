// 마이페이지 기록 조회
import axiosInstance from '../../../api/axiosInstance';
import useAuthStore from '../../../stores/authStore';
const { currentUserId } = useAuthStore.getState();

// 요청 파라미터 타입
export interface MypageRecordListRequestParams {
  page?: number; // 페이지 번호 (기본 값: 1)
  limit?: number; // 페이지 당 항목 수 (기본 값: 12)
}

// 응답 데이터 타입
export interface MypageRecord {
  recordId: number;
  title: string;
  content: string;
  createdDate: string;
}

export interface PaginationInfo {
  total: number;
  page: number;
  perPage: number;
}

export interface MypageRecordListResponse {
  data: {
    items: MypageRecord[];
    pagination: PaginationInfo;
  };
}

// 기록 조회 함수
export const getMypageRecords = async (
  params: MypageRecordListRequestParams = {}
): Promise<MypageRecordListResponse> => {
  try {
    const response = await axiosInstance.get<MypageRecordListResponse>(
      `/users/${currentUserId}/mypage/records`,
      { params }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching mypage records:', error);
    throw error;
  }
};
