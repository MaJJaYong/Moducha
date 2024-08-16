import { BoardType } from '../types/BoardType';
import axiosInstance from './axiosInstance';

interface ArticleList {
  boardType: BoardType;
  sort?: string;
  page?: number;
  perPage?: number;
  searchBy?: string;
  keyword?: string;
}

export const fetchArticleList = ({
  boardType,
  sort,
  page,
  perPage,
  searchBy,
  keyword,
}: ArticleList) => {
  return axiosInstance.get(`/${boardType}`, {
    params: { sort, page, perPage, searchBy, keyword },
  });
};

export const fetchArticleDetail = ({
  boardType,
  boardId,
}: {
  boardType: BoardType;
  boardId: string;
}) => {
  return axiosInstance.get(`/${boardType}/${boardId}`);
};

interface MyTeatimeList extends ArticleList {
  userId: string;
}
export const fetchMyWriteList = ({
  userId,
  boardType,
  sort = 'latest',
  page = 1,
  perPage = 12,
}: MyTeatimeList) => {
  return axiosInstance.get(`/users/${userId}/mypage/${boardType}`, {
    params: { sort, page, perPage },
  });
};

export const fetchMyList = ({ userId, boardType }: MyTeatimeList) => {
  return axiosInstance.get(`/users/${userId}/my-${boardType}`);
};
