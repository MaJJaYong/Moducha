import { Dispatch, SetStateAction } from 'react';
import { BoardType } from '../../types/BoardType';
import CommentWrite from './CommentWrite';
import { Comment } from '../../types/CommentType';

interface CommentReplyWriteType {
  boardType: BoardType;
  boardId: number;
  commentId: number;
  setReplyList: Dispatch<SetStateAction<Comment[]>>;
}

const CommentReplyWrite = ({
  boardId,
  boardType,
  commentId,
  setReplyList,
}: CommentReplyWriteType) => {
  return (
    <li key={`reply-write_${boardId}`} className="grid grid-cols-12">
      <div className="col-span-1 relative">
        <div className="after:div after:border-l-2 after:border-b-2  after:rounded-sm after:border-slate-300 after:w-1/3 after:h-1/4 after:absolute after:right-2 after:top-2"></div>
      </div>
      <div className="col-span-11">
        <hr />
        <CommentWrite
          commentType="reply"
          boardType={boardType}
          boardId={boardId}
          commentId={commentId}
          setCommentList={setReplyList}
        />
      </div>
    </li>
  );
};

export default CommentReplyWrite;
