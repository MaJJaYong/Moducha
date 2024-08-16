import { useEffect, useState } from 'react';
import getMyTeatimeWriteList from '../pages/MyPage/api/getMyTeatimeWriteList';
import { TeatimeListItem } from '../types/TeatimeType';

const useFetchMyTeatimeList = (boardType: 'teatimes', perPage = 12) => {
  const [articleList, setArticleList] = useState<TeatimeListItem[]>([]);
  const [sort, setSort] = useState('latest');
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await getMyTeatimeWriteList({
          page,
          perPage,
          sort, // sort 값을 추가
        });
        setArticleList(response.data);
        setTotalPage(Math.ceil(response.pagination.total / perPage));
      } catch (error) {
        console.error('티타임 목록을 가져오는 데 실패했습니다:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [sort, page]);

  return {
    articleList,
    setArticleList,
    sort,
    setSort,
    pageData: { page, totalPage, setPage },
    isLoading,
  };
};

export default useFetchMyTeatimeList;
