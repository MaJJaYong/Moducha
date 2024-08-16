import { useEffect, useState } from 'react';
import SideLayout from '../Layout/SideLayout';
const ArticleLoading = () => {
  const [isVisible, setIsVisible] = useState(false);
  useEffect(() => {
    setTimeout(() => {
      setIsVisible(true);
    }, 200);
  }, []);
  if (!isVisible) return null;
  return (
    <div className="grid grid-cols-10">
      <SideLayout></SideLayout>
      <main className="col-span-10 lg:col-span-6 md:grid md:grid-cols-12">
        <section className="md:col-span-4 p-2">
          <div className="md:sticky md:top-2 flex flex-col overflow-clip p-4 border shadow gap-4">
            {/* card skeleton */}
            <div className="flex w-full flex-col gap-4">
              <div className="skeleton rounded h-40 w-full"></div>
              <div className="flex flex-col w-full gap-3">
                <div className="skeleton rounded h-4 w-1/4"></div>
                <div className="skeleton rounded h-4 w-3/4"></div>
                <div className="skeleton rounded h-4 w-3/4"></div>
                <div className="skeleton rounded h-4 w-3/4"></div>
                <div className="skeleton rounded h-4 w-3/4"></div>
              </div>
              <div className="skeleton rounded h-10 w-full"></div>
            </div>
          </div>
        </section>
        <article className="md:col-span-8 p-2">
          <div className="p-4 shadow border flex flex-col gap-4">
            {/* content skeleton */}
            <header className="flex  justify-between items-center border rounded p-4 shadow-md">
              <div className="skeleton rounded h-12 w-full"></div>
            </header>
            <div className="flex w-full flex-col gap-4">
              <div className="skeleton rounded h-60 w-full"></div>
              <div className="skeleton rounded h-4 w-1/12"></div>
              <div className="skeleton rounded h-4 w-1/6"></div>
              <div className="skeleton rounded h-4 w-1/6"></div>
              <div className="skeleton rounded h-4 w-4/6"></div>
              <div className="skeleton rounded h-4 w-4/6"></div>
              <div className="skeleton rounded h-4 w-full"></div>
              <div className="skeleton rounded h-4 w-full"></div>
              <div className="skeleton rounded h-4 w-full"></div>
              <div className="skeleton rounded h-4 w-full"></div>
            </div>
          </div>
        </article>
      </main>
      <SideLayout></SideLayout>
    </div>
  );
};

export default ArticleLoading;
