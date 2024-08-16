import { useEffect, useState } from 'react';
import { BoardType } from '../types/BoardType';
import useAuthStore from '../stores/authStore';
import { fetchMyList } from '../api/fetchArticle';

const useFetchMyList = (boardType: BoardType) => {
  const [myArticleList, setMyArticleList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const { userId } = useAuthStore((state) => ({ userId: state.currentUserId }));
  useEffect(() => {
    fetchMyList({ userId, boardType })
      .then((res) => {
        if (res.status === 200) {
          setMyArticleList(res.data.data);
        }
      })
      .catch((err) => console.log(err))
      .finally(() => setIsLoading(false));
  }, [userId]);
  return { myArticleList, isLoading };
};

export default useFetchMyList;
