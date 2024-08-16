import { Comment, CommentReplyType } from '../../types/CommentType';
import { BoardType } from '../../types/BoardType';
import dateParser from '../../utils/dateParser';
import CommentReply from './CommentReply';
import axiosInstance from '../../api/axiosInstance';
import { Dispatch, SetStateAction, useState } from 'react';

interface CommentListItemProps extends Comment {
  commentType: CommentReplyType;
  boardType: BoardType;
  isLoggedIn?: boolean;
  currentUserId: string;
  setCommentList: Dispatch<SetStateAction<Comment[]>>;
}

const CommentListItem = ({
  commentType,
  boardType,
  nickname,
  content,
  createdDate,
  boardId,
  commentId,
  replyId,
  replyCount,
  userId,
  isLoggedIn = false,
  currentUserId,
  picture,
  setCommentList,
}: CommentListItemProps) => {
  const [isReplyWrite, setIsReplyWrite] = useState(false);

  const handleDelete = async () => {
    const baseUrl = `${boardType}/${boardId}`;
    const deleteUrl =
      commentType === 'comment'
        ? `${baseUrl}/deactivated-comments/${commentId}`
        : `${baseUrl}/comments/${commentId}/deactivated-replies/${replyId}`;

    try {
      await axiosInstance.patch(deleteUrl);

      setCommentList((prev) =>
        prev.map((el) => {
          if (
            (commentType === 'comment' && el.commentId === commentId) ||
            (commentType === 'reply' && el.replyId === replyId)
          ) {
            return {
              ...el,
              nickname: '',
              content: `삭제된 ${commentType === 'comment' ? '댓글' : '답글'}입니다.`,
            };
          }
          return el;
        })
      );

      if (commentType === 'comment') {
        setIsReplyWrite(false);
      }
    } catch (error) {
      console.error('Failed to delete the comment:', error);
    }
  };

  const renderReplyButton = () =>
    !!nickname &&
    commentType === 'comment' &&
    isLoggedIn && (
      <button
        onClick={() => setIsReplyWrite((prev) => !prev)}
        className="font-medium text-gray-600"
      >
        답글
      </button>
    );

  const renderDeleteButton = () =>
    userId === currentUserId &&
    nickname && (
      <>
        ·
        <button onClick={handleDelete} className="font-medium text-gray-600">
          삭제
        </button>
      </>
    );

  return (
    <>
      <div className="flex py-4">
        <figure className="w-1/12 px-1 overflow-hidden">
          <img
            className="rounded-full w-full"
            src={picture}
            alt={`${nickname}'s avatar`}
          />
        </figure>
        <main className="w-11/12 px-2 flex flex-col justify-between">
          <header className="flex justify-between">
            <span className="font-bold">{nickname}</span>
          </header>
          <article>
            <p className="pe-4">{content}</p>
          </article>
          <footer className="mt-2 text-sm text-gray-500 font-light flex gap-2">
            <span>{dateParser(createdDate)}</span>
            <div className="flex gap-1">
              {renderReplyButton()}
              {renderDeleteButton()}
            </div>
          </footer>
        </main>
      </div>
      {commentType === 'comment' && (
        <>
          <CommentReply
            isReplyWrite={isReplyWrite}
            commentType={commentType}
            boardType={boardType}
            boardId={boardId}
            commentId={commentId}
            replyCount={replyCount}
            currentUserId={currentUserId}
          />
          <hr />
        </>
      )}
    </>
  );
};

export default CommentListItem;
