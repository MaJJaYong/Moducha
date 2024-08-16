import { Link } from 'react-router-dom';
import { TeatimeListItem } from '../../../types/TeatimeType';
import parse from 'node-html-parser';
import dayjs from 'dayjs';

interface TeatimeListCardType extends TeatimeListItem {
  index: number;
  participants: number;
  maxParticipants: number;
}

const TeatimeListCard = ({
  boardId,
  title,
  content,
  index,
  viewCount,
  participants,
  maxParticipants,
  createdDate,
  endDate,
}: TeatimeListCardType) => {
  const parsedContent = parse(content);
  const imageUrl = parsedContent.querySelector('img')?.getAttribute('src');
  const defaultImage = new URL(
    `../../../assets/defaultcard/tea${(boardId % 6) + 1}.jpg`,
    import.meta.url
  ).href;
  const isEnd = dayjs() >= dayjs(endDate) || maxParticipants === participants;
  const isNew = dayjs(createdDate).add(5, 'day') >= dayjs();
  return (
    <Link
      to={`/teatimes/${boardId}`}
      className={`flex flex-col overflow-hidden border h-68 rounded-lg transition ease-in-out hover:bg-teabg hover:shadow-lg duration-150 ${index > 5 && 'hidden xl:flex'}`}
    >
      <figure
        className="overflow-hidden h-40 shrink-0 bg-cover bg-center bg-no-repeat"
        style={{ backgroundImage: `url(${imageUrl || defaultImage})` }}
      ></figure>
      <main className="p-4 pt-2 flex flex-col justify-between h-full">
        <article>
          <header className="truncate font-semibold">{title}</header>
          <p className="text-sm line-clamp-2 text-disabled">
            {parsedContent.textContent}
          </p>
        </article>
        <footer className="flex gap-1 text-sm text-tea mt-1 truncate">
          <span>
            {isEnd ? '#마감' : `#${maxParticipants - participants}명 모집 중`}
          </span>
          <span>{viewCount > 29 && '#인기'}</span>
          <span>{isNew && '#신규'}</span>
        </footer>
      </main>
    </Link>
  );
};

export default TeatimeListCard;
