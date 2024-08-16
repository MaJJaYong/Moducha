import { PaginationDTO } from './PaginationType';

export interface ShareListItem {
  boardId: number;
  title: string;
  createdDate: string;
  lastUpdated: string;
  content: string;
  endDate: string;
  maxParticipants: number;
  participants: number;
  nickname: string;
  viewCount: number;
}

export interface ShareDTO {
  message: string;
  pagination: PaginationDTO;
  data: ShareListItem[];
}

// 상속
export interface ShareDetailItem extends ShareListItem {
  content: string;
  userId: string;
  picture: string;
}

export interface ShareDetail {
  message: string;
  data: ShareDetailItem;
}
