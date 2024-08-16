import { ShareListItem } from '../../../types/ShareType';
import { HiEye, HiUsers } from 'react-icons/hi';
import { Link } from 'react-router-dom';
import { parse } from 'node-html-parser';
import defaultShare from '../../../assets/defaultcard/defaultShare.webp';

const ShareCard = ({
  boardId,
  title,
  content,
  maxParticipants,
  participants,
  viewCount,
}: ShareListItem) => {
  const parsedContent = parse(content);
  const imageUrl = parsedContent.querySelector('img')?.getAttribute('src');

  return (
    <Link
      to={`${boardId}`}
      className="flex bg-base-100 overflow-hidden shadow rounded-lg transition ease-in-out hover:bg-teabg hover:shadow-lg duration-150"
    >
      <figure
        className="size-32 shrink-0 bg-cover bg-no-repeat bg-center"
        style={{
          backgroundImage: `url(${imageUrl ? imageUrl : defaultShare})`,
        }}
      />
      <main className="flex flex-col w-full p-2 h-32 overflow-hidden">
        <h2 className="font-bold truncate">{title}</h2>
        <section className="flex items-center gap-2">
          <div>
            <HiUsers className="inline size-4 text-green-500" />
            <span className="text-sm ms-1">
              {participants} / {maxParticipants}
            </span>
          </div>
          <div>
            <HiEye className="inline size-3 text-orange-400" />
            <span className="text-sm ms-1">{viewCount}</span>
          </div>
        </section>
        <article className="overflow-hidden mt-1">
          <p className="line-clamp-2 text-disabled">
            {parsedContent.textContent}
          </p>
        </article>
      </main>
    </Link>
  );
};

export default ShareCard;
