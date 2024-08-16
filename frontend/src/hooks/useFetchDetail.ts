import { useEffect, useState } from 'react';
import { BoardType } from '../types/BoardType';
import { fetchArticleDetail } from '../api/fetchArticle';
import { useNavigate } from 'react-router-dom';

const useFetchDetail = <T>(boardType: BoardType, boardId: string) => {
  const [articleDetail, setArticleDetail] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();
  useEffect(() => {
    fetchArticleDetail({ boardType, boardId })
      .then((res) => {
        if (res.status === 200) {
          setArticleDetail(() => ({ ...res.data.data, boardType }));
        }
      })
      .catch((err) => {
        console.log(err);
        navigate('/error');
      })
      .finally(() => setIsLoading(false));
  }, []);
  return { articleDetail, isLoading, setIsLoading };
};
export default useFetchDetail;
