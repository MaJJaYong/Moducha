import { useEffect, useState } from 'react';

const ArticleListLoading = () => {
  const [isVisible, setIsVisible] = useState(false);
  useEffect(() => {
    setTimeout(() => {
      setIsVisible(true);
    }, 100);
  }, []);
  if (!isVisible) return null;
  return (
    <>
      <div className="my-4 grid gap-4 sm:grid-cols-2 2xl:grid-cols-3">
        {Array(12)
          .fill(0)
          .map((v, i) => (
            <ArticleLoadingCard key={`articleloadingcard${v + i}`} />
          ))}
      </div>
    </>
  );
};

const ArticleLoadingCard = () => (
  <div className="flex p-5 border gap-4 bg-base-100 overflow-hidden shadow rounded-lg">
    <header className="flex justify-between">
      <figure className="skeleton rounded shrink-0 size-28"></figure>
      <div className="flex tag-region gap-1 shrink-0"></div>
    </header>
    <article className="flex flex-col justify-end w-full gap-4">
      <div className="skeleton rounded h-4 w-1/4"></div>
      <div className="skeleton rounded h-4 w-full"></div>
    </article>
  </div>
);

export default ArticleListLoading;
