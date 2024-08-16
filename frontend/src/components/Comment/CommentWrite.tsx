import { Dispatch, SetStateAction, useState } from 'react';
import axiosInstance from '../../api/axiosInstance';
import { BoardType } from '../../types/BoardType';
import { Comment, CommentReplyType } from '../../types/CommentType';
import useAuthStore from '../../stores/authStore';

interface CommentWriteProp {
  commentType: CommentReplyType;
  boardType: BoardType;
  boardId: number;
  commentId?: number;
  setCommentList: Dispatch<SetStateAction<Comment[]>>;
}

const CommentWrite = ({
  commentType,
  boardType,
  boardId,
  commentId,
  setCommentList,
}: CommentWriteProp) => {
  const [content, setContent] = useState('');
  const [isSending, setIsSending] = useState(false);
  const { isLoggedIn, userId, nickname, picture } = useAuthStore((state) => ({
    isLoggedIn: state.isLoggedIn,
    userId: state.currentUserId,
    nickname: state.currentUsername,
    picture: state.currentUserPicture,
  }));
  const replyOrComment = commentType === 'comment' ? '댓글' : '답글';

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSending(() => true);
    const BASE_URL = `${boardType}/${boardId}/comments`;
    const FETCH_URL =
      commentType === 'comment' ? BASE_URL : `${BASE_URL}/${commentId}/replies`;

    axiosInstance
      .post(FETCH_URL, {
        content,
      })
      .then((res) => {
        // creationResponse에 현재 유저 정보 삽입
        setCommentList((prev) => [
          ...prev,
          {
            ...res.data.data,
            picture,
            replyCount: 0,
            userId,
            nickname,
          },
        ]);
        clearForm();
      })
      .catch((err) => {
        alert(`${replyOrComment} 작성 중 에러가 발생했습니다.`);
        return err;
      })
      .finally(() => {
        setIsSending(() => false);
      });
  };
  const clearForm = () => {
    setContent(() => '');
  };

  return (
    <form onSubmit={handleSubmit} className="flex py-4">
      <textarea
        className={`w-full border p-4 resize-none focus:outline-none focus:border-tea focus:ring-tea ${!isLoggedIn && 'bg-gray-100'}`}
        name="content"
        placeholder={
          isLoggedIn
            ? `${replyOrComment}을 작성해 보세요`
            : `${replyOrComment}을 작성하려면 로그인 하세요`
        }
        value={content}
        onChange={(e) => setContent(e.target.value)}
        required
        disabled={!isLoggedIn}
      />
      <div className="w-24 flex flex-col px-2 gap-2">
        <button
          type="submit"
          className="btn rounded bg-teabg text-tea hover:bg-tea hover:text-white"
          disabled={isSending || !isLoggedIn}
        >
          작성
        </button>
        <button
          onClick={clearForm}
          type="button"
          className="btn rounded bg-teabg text-tea hover:bg-tea hover:text-white"
          disabled={isSending || !isLoggedIn}
        >
          리셋
        </button>
      </div>
    </form>
  );
};

export default CommentWrite;
