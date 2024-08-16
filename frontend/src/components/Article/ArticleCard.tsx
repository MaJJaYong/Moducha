import { ArticleDetailProp } from '../../types/ArticleType';
import dateParser from '../../utils/dateParser';
import TeatimeButton from '../../pages/Teatime/components/TeatimeButton';
import ApplyButton from '../Button/ApplyButton';
import dayjs from 'dayjs';
import { PencilSquareIcon } from '@heroicons/react/24/solid';
const ArticleCard = ({
  title,
  boardType,
  boardId,
  nickname,
  createdDate,
  endDate,
  broadcastDate,
  participants,
  maxParticipants,
  picture,
  userId,
}: ArticleDetailProp) => {
  const isEnded = dayjs() >= dayjs(endDate) || participants === maxParticipants;
  return (
    <div className="md:sticky md:top-2 flex flex-col overflow-clip p-4 border shadow gap-4">
      <figure>
        <img
          className="rounded size-24"
          src={picture}
          alt={`profile_${nickname}`}
        />
      </figure>
      <div id="card-body" className="text-disabled">
        <header>
          <div className="flex gap-1 items-center">
            <PencilSquareIcon className='size-5 mb-1 text-slate-400'/>
            <span>{nickname}</span>
          </div>
        </header>
        <div className="pl-0 flex flex-col">
          <span>작성 : {dateParser(createdDate)}</span>
          <span>마감 : {dateParser(endDate)}</span>
          <span>참여 : {participants + ' / ' + maxParticipants}</span>
          {boardType === 'teatimes' && (
            <span>예정 : {dateParser(broadcastDate)}</span>
          )}
        </div>
      </div>
      {boardType === 'teatimes' ? (
        <TeatimeButton
          title={title}
          boardType={boardType}
          nickname={nickname}
        />
      ) : (
        <ApplyButton {...{ boardId, boardType, isEnded, userId }} />
      )}
    </div>
  );
};

export default ArticleCard;
