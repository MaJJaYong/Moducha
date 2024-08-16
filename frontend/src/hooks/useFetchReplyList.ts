import { MutableRefObject, useEffect, useState } from 'react';
import { fetchReplyList } from '../api/fetchComment';
import { BoardType } from '../types/BoardType';
import { Comment } from '../types/CommentType';
import useIntersectionObserver from './useIntersectionObserver';

const useFetchReplyList = (
  boardType: BoardType,
  boardId: number,
  commentId: number,
  replyCount: number | undefined,
  sentinel: MutableRefObject<HTMLDivElement | null>
) => {
  const [replyList, setReplyList] = useState<Comment[]>([]);
  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(null);
  const [observe, unobserve] = useIntersectionObserver(() =>
    setPage((prev) => prev + 1)
  );

  useEffect(() => {
    if (replyCount) {
      fetchReplyList({
        boardType: boardType,
        boardId,
        commentId,
        page,
        perPage: 10,
      }).then((res) => {
        if (res.status === 200) {
          setReplyList((prev) => [...prev, ...res.data.data]);
          setTotalPage(res.data.pagination.total);
        }
      });
    }
  }, [boardId, boardType, commentId, page, replyCount]);

  useEffect(() => {
    if (replyCount) {
      if (!totalPage || page >= totalPage) {
        unobserve(sentinel.current);
      } else {
        observe(sentinel.current);
      }
    }
  });
  return { replyList, setReplyList };
};

export default useFetchReplyList;
