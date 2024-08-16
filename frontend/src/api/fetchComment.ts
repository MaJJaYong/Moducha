import { BoardType } from '../types/BoardType';
import axiosInstance from './axiosInstance';

interface CommentRequestOption {
  boardType: BoardType;
  boardId?: number;
  page: number;
  perPage: number;
}

interface ReplyRequestOption extends CommentRequestOption {
  commentId: number;
}

export const fetchCommentList = ({
  boardType,
  boardId,
  page,
  perPage,
}: CommentRequestOption) => {
  return axiosInstance.get(`/${boardType}/${boardId}/comments`, {
    params: { page, perPage },
  });
};

export const fetchReplyList = ({
  boardType,
  boardId,
  commentId,
  page,
  perPage,
}: ReplyRequestOption) => {
  return axiosInstance.get(
    `/${boardType}/${boardId}/comments/${commentId}/replies`,
    {
      params: { page, perPage },
    }
  );
};
