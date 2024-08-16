import { useRef } from 'react';
import CommentListItem from './CommentListItem';
import { BoardType } from '../../types/BoardType';
import { CommentReplyType } from '../../types/CommentType';
import CommentReplyWrite from './CommentReplyWrite';
import useFetchReplyList from '../../hooks/useFetchReplyList';

interface ReplyType {
  isReplyWrite: boolean;
  commentType: CommentReplyType;
  boardType: BoardType;
  boardId: number;
  commentId: number;
  replyCount?: number;
  currentUserId: string;
}

const CommentReply = ({
  isReplyWrite,
  boardType,
  boardId,
  replyCount,
  commentId,
  currentUserId,
}: ReplyType) => {
  const sentinel = useRef(null);
  const { replyList, setReplyList } = useFetchReplyList(
    boardType,
    boardId,
    commentId,
    replyCount,
    sentinel
  );
  return (
    <ul id="reply-list">
      {isReplyWrite && (
        <CommentReplyWrite
          {...{ boardId, boardType, commentId, setReplyList }}
        />
      )}
      {replyList.map((el) => (
        <li key={el.replyId} className="grid grid-cols-12">
          <div className="col-span-1 relative">
            <div className="after:div after:border-l-2 after:border-b-2 after:rounded-sm after:border-slate-300 after:w-1/3 after:h-1/4 after:absolute after:right-2 after:top-2"></div>
          </div>
          <div className="col-span-11">
            <hr />
            <CommentListItem
              setCommentList={setReplyList}
              {...{
                ...el,
                commentType: 'reply',
                boardType,
                boardId,
                currentUserId,
              }}
            />
          </div>
        </li>
      ))}
      <div className="h-0.5" ref={sentinel} />
    </ul>
  );
};

export default CommentReply;
