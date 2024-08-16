import { BoardType } from './BoardType';

export type ImageList = {
  id: number;
  url: string;
}[];

export interface ArticleDetailProp {
  boardType: BoardType;
  boardId: number;
  title: string;
  content: string;
  createdDate: string;
  lastUpdated: string;
  broadcastDate?: string;
  endDate: string;
  maxParticipants: number;
  participants: number;
  nickname: string;
  viewCount: number;
  picture: string;
  userId: string;
}
