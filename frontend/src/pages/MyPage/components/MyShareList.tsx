import React from 'react';
import ShareListCard from '../../Home/componenets/ShareListCard';
import { ShareListItem } from '../../../types/ShareType';

interface MyShareListProps {
  list: ShareListItem[];
  error: string | null;
  total: number;
  page: number;
  setPage: (page: number) => void;
  perPage: number;
}

const MyShareList: React.FC<MyShareListProps> = ({
  list,
  error,
  total,
  page,
  setPage,
  perPage,
}) => {
  if (error) {
    return <div className="text-red-500">{error}</div>;
  }

  if (list.length === 0) {
    return <div className="text-gray-500">데이터가 없습니다.</div>;
  }

  const totalPages = Math.ceil(total / perPage);

  return (
    <div>
      <article className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {list.map((item) => (
          <ShareListCard key={item.boardId} {...item} />
        ))}
      </article>
    </div>
  );
};

export default MyShareList;
