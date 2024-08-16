import { ShareListItem } from '../../../types/ShareType';
import { Link } from 'react-router-dom';
import { PiEyesBold } from 'react-icons/pi';
import { parse } from 'node-html-parser';

const ShareListCard = ({
  boardId,
  title,
  content,
  viewCount,
}: ShareListItem) => {
  const parsedContent = parse(content);
  const imageUrl = parsedContent.querySelector('img')?.getAttribute('src');
  const defaultImage = new URL(
    `../../../assets/defaultcard/share${(boardId % 4) + 1}.jpg`,
    import.meta.url
  ).href;
  // 절대 경로 사용 - 재사용 목적
  const linkTo = `/shares/${boardId}`;

  return (
    <Link
      to={linkTo}
      className="flex flex-col gap-3 rounded group/sharecard duration-300 delay-50 transition ease-in-out hover:text-tea"
    >
      <figure
        className="overflow-hidden rounded border h-32 bg-cover bg-no-repeat bg-center duration-300 delay-50 transition ease-in-out group-hover/sharecard:scale-105"
        style={{
          backgroundImage: `url(${imageUrl || defaultImage})`,
        }}
      ></figure>
      <main className="flex flex-col gap-1">
        <header className="flex items-center gap-1">
          <h1 className="truncate font-semibold">{title}</h1>
        </header>
        <article>
          <p className="line-clamp-2 text-sm text-disabled">
            {parsedContent.textContent}
          </p>
        </article>
        <footer className="flex items-center text-sm gap-0.5 text-gray-400">
          <PiEyesBold className="size-4 text-neutral-600" />
          <span>{viewCount}</span>
        </footer>
      </main>
    </Link>
  );
};

export default ShareListCard;
