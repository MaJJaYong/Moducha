import { Link } from 'react-router-dom';
import { TeatimeListItem } from '../../../types/TeatimeType';
import { parse } from 'node-html-parser';
import FilledBadge, { FilledBadgeColor } from '../../../components/Badge/FilledBadge';
import useMyTeatimeBadge from '../../../hooks/useMyTeatimeBadge';

const MyTeatimeCard = ({
  boardId,
  title,
  content,
  broadcastDate,
}: TeatimeListItem) => {
  const parsedContent = parse(content);
  const imageUrl = parsedContent.querySelector('img')?.getAttribute('src');
  const defaultImage = new URL(
    `../../../assets/defaultcard/my${(boardId % 4) + 1}.jpg`,
    import.meta.url
  ).href;
  const [badgeColor, badgeValue] = useMyTeatimeBadge(broadcastDate);

  return (
    <Link
      to={`teatimes/${boardId}`}
      className="flex flex-col gap-3 rounded group/myteatime duration-300 delay-50 transition ease-in-out hover:text-tea"
    >
      <figure
        className="overflow-hidden rounded border h-32 bg-cover bg-no-repeat bg-center duration-300 delay-50 transition ease-in-out group-hover/myteatime:scale-105"
        style={{
          backgroundImage: `url(${imageUrl || defaultImage})`,
        }}
      ></figure>
      <main className="flex flex-col gap-1">
        <header className="flex items-center gap-2">
          <FilledBadge color={badgeColor as FilledBadgeColor}>{badgeValue}</FilledBadge>
          <h1 className="truncate font-semibold">{title}</h1>
        </header>
        <article>
          <p className="line-clamp-2 text-sm text-disabled">
            {parsedContent.textContent}
          </p>
        </article>
      </main>
    </Link>
  );
};

export default MyTeatimeCard;
