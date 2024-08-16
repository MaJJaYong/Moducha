import { useRef } from 'react';
import { Comment } from '../../types/CommentType';
import CommentListItem from './CommentListItem';
import { BoardType } from '../../types/BoardType';
import CommentWrite from './CommentWrite';
import useAuthStore from '../../stores/authStore';
import useFetchCommentList from '../../hooks/useFetchCommentList';

interface Board {
  boardType: BoardType;
  boardId: number;
}

const CommentList = ({ boardType, boardId }: Board) => {
  const sentinel = useRef<HTMLDivElement | null>(null);
  const { currentUserId, isLoggedIn } = useAuthStore((state) => ({
    currentUserId: state.currentUserId,
    isLoggedIn: state.isLoggedIn,
  }));
  const { commentList, setCommentList } = useFetchCommentList(
    boardType,
    boardId,
    sentinel
  );
  return (
    <div>
      <header>
        <h1 className="m-2 text-xl font-bold">댓글</h1>
        <hr className="border border-gray-300" />
      </header>
      <CommentWrite
        {...{ commentType: 'comment', boardType, boardId, setCommentList }}
      />
      <hr />
      <main>
        {!!commentList.length && (
          <ul>
            {commentList.map((el: Comment) => (
              <li key={`commentkey${el.commentId}`}>
                <CommentListItem
                  {...{
                    ...el,
                    commentType: 'comment',
                    boardType,
                    isLoggedIn,
                    currentUserId,
                    setCommentList,
                  }}
                />
              </li>
            ))}
          </ul>
        )}
      </main>
      <div className="h-0.5" ref={sentinel} />
    </div>
  );
};

export default CommentList;
