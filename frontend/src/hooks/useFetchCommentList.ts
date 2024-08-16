import { MutableRefObject, useEffect, useState } from 'react';
import { fetchCommentList } from '../api/fetchComment';
import { BoardType } from '../types/BoardType';
import { Comment } from '../types/CommentType';
import useIntersectionObserver from './useIntersectionObserver';

const useFetchCommentList = (
  boardType: BoardType,
  boardId: number,
  sentinel: MutableRefObject<HTMLDivElement | null>
) => {
  const [commentList, setCommentList] = useState<Comment[]>([]);
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(null);
  const handleObserver = () => {
    setPage((prev) => prev + 1);
  };
  const [observe, unobserve] = useIntersectionObserver(handleObserver);

  useEffect(() => {
    fetchCommentList({
      boardType,
      boardId,
      page,
      perPage: 10,
    }).then((res) => {
      if (res.status === 200) {
        setCommentList((prev) => [...prev, ...res.data.data]);
        setTotalPage(res.data.pagination.total);
      }
    });
  }, [boardId, boardType, page]);

  useEffect(() => {
    if (!totalPage || page >= totalPage) {
      unobserve(sentinel.current);
    } else {
      observe(sentinel.current);
    }
  });
  return { commentList, setCommentList };
};

export default useFetchCommentList;
