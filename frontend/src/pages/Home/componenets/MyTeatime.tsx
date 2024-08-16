import { Link } from 'react-router-dom';
import MyTeatimeCarousel from './MyTeatimeCarousel';
import NotFound from './NotFound';
import useFetchMyList from '../../../hooks/useFetchMyList';

const MyTeatime = ({ ...props }) => {
  const { myArticleList: myTeatimeList, isLoading } =
    useFetchMyList('teatimes');
  return (
    <section {...props}>
      <header className="flex justify-between items-center">
        <h1 className="font-semibold text-2xl">나의 티타임</h1>
        <Link to={'/mypage'} className="text-disabled">
          모두보기
        </Link>
      </header>
      <main className="relative">
        {!isLoading && !myTeatimeList.length && <NotFound />}
        {!isLoading && !!myTeatimeList.length && (
          <MyTeatimeCarousel myTeatimeList={myTeatimeList} />
        )}
      </main>
    </section>
  );
};

export default MyTeatime;
