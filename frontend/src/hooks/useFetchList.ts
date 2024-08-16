import { useEffect, useState } from 'react';
import { fetchArticleList } from '../api/fetchArticle';
import { BoardType } from '../types/BoardType';

const useFetchList = (boardType: BoardType, perPage = 12) => {
  const [articleList, setArticleList] = useState([]);
  const [sort, setSort] = useState('latest');
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [searchBy, setSearchBy] = useState('title');
  const [keyword, setKeyword] = useState('');

  useEffect(() => {
    fetchArticleList({ boardType, sort, page, perPage, searchBy, keyword })
      .then((res) => {
        if (res.status === 200) {
          setArticleList(res.data.data);
          setTotalPage(res.data.pagination.total);
        }
      })
      .catch((err) => console.log(err))
      .finally(() => setIsLoading(false));
  }, [sort, page, boardType, perPage, searchBy, keyword]);

  return {
    articleList,
    setArticleList,
    sort,
    setSort,
    pageData: { page, totalPage, setPage, setSearchBy, setKeyword },
    isLoading,
  };
};

export default useFetchList;
