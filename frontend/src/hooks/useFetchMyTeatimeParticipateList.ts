import { useState, useEffect } from 'react';
import axiosInstance from '../api/axiosInstance';
import { TeatimeListItem } from '../types/TeatimeType';
import useAuthStore from '../stores/authStore';

interface UseFetchMyTeatimeParticipateList {
  articleList: TeatimeListItem[];
  sort: string;
  setSort: React.Dispatch<React.SetStateAction<string>>;
  pageData: {
    page: number;
    totalPage: number;
    setPage: React.Dispatch<React.SetStateAction<number>>;
  };
  isLoading: boolean;
}

const useFetchMyTeatimeParticipateList = (
  sort: string,
  perPage: number
): UseFetchMyTeatimeParticipateList => {
  const { currentUserId } = useAuthStore((state) => ({
    currentUserId: state.currentUserId,
  }));
  const [articleList, setArticleList] = useState<TeatimeListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [sortState, setSort] = useState(sort);
  const [scrollPosition, setScrollPosition] = useState(0);

  useEffect(() => {
    const fetchTeatimes = async () => {
      setIsLoading(true);

      try {
        const response = await axiosInstance.get(
          `/users/${currentUserId}/mypage/participated-teatimes`,
          {
            params: {
              sort: sortState,
              page,
              perPage,
            },
          }
        );

        if (response.status === 204) {
          setArticleList([]);
          setTotalPage(0);
        } else {
          const { data, pagination } = response.data;
          setArticleList(data || []);
          setTotalPage(pagination ? Math.ceil(pagination.total / perPage) : 0);
        }
      } catch (error) {
        console.error('Failed to fetch participated teatimes:', error);
        setArticleList([]);
        setTotalPage(0);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTeatimes();
  }, [sortState, page, perPage, currentUserId]);

  useEffect(() => {
    // 스크롤 위치 복원
    window.scrollTo(0, scrollPosition);
  }, [sortState]);

  useEffect(() => {
    // 스크롤 위치 저장
    const handleScroll = () => {
      setScrollPosition(window.scrollY);
    };

    window.addEventListener('scroll', handleScroll);

    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  return {
    articleList,
    sort: sortState,
    setSort,
    pageData: {
      page,
      totalPage,
      setPage,
    },
    isLoading,
  };
};

export default useFetchMyTeatimeParticipateList;
