import { PaginationDTO } from './PaginationType';

export interface Comment {
  commentId: number;
  replyId?: number;
  boardId: number;
  content: string;
  createdDate: string;
  nickname?: string;
  replyCount?: number;
  userId: string;
  picture: string;
}

export interface CommentDTO {
  data: Comment[];
  pagination: PaginationDTO;
}

export interface ReplyDTO {
  commentId: number;
  replyId: number;
  content: string;
  createdDate: string;
  nickname: string;
}

export type CommentReplyType = 'comment' | 'reply';
