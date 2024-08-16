import React from 'react';
import TeatimeListToggle from './MyTeatimeListToggle';
import TeatimeListCard from '../../Home/componenets/TeatimeListCard';
import useFetchMyTeatimeList from '../../../hooks/useFetchMyTeatimeList';
import useFetchMyTeatimeParticipateList from '../../../hooks/useFetchMyTeatimeParticipateList';
import { TeatimeListItem } from '../../../types/TeatimeType';
import { Dispatch, SetStateAction } from 'react';

const TeatimeList = (props: React.HTMLProps<HTMLElement>) => {
  interface TeatimeFetch {
    articleList: TeatimeListItem[];
    sort: string;
    setSort: Dispatch<SetStateAction<string>>;
    pageData: {
      page: number;
      totalPage: number;
      setPage: Dispatch<SetStateAction<number>>;
    };
    isLoading: boolean;
  }

  const {
    articleList: myTeatimeList = [], // 기본값을 빈 배열로 설정
    sort: myTeatimeSort,
    setSort: setMyTeatimeSort,
    pageData: myTeatimePageData,
    isLoading: myTeatimeIsLoading,
  }: TeatimeFetch = useFetchMyTeatimeList('teatimes', 12);

  const {
    articleList: participatedTeatimeList = [], // 기본값을 빈 배열로 설정
    sort: participatedTeatimeSort,
    setSort: setParticipatedTeatimeSort,
    pageData: participatedTeatimePageData,
    isLoading: participatedTeatimeIsLoading,
  }: TeatimeFetch = useFetchMyTeatimeParticipateList('ongoing', 12);

  if (myTeatimeIsLoading || participatedTeatimeIsLoading) {
    return <div>로딩 중...</div>; // 로딩 중일 때 표시할 컴포넌트
  }

  return (
    <>
      {/* 내가 작성한 티타임 */}
      <section {...props}>
        <h1 className="font-semibold text-2xl mt-5 mb-5">내가 작성한 티타임</h1>
        <section className="flex items-center justify-between">
          <div className="flex gap-2">
            {/* <TeatimeListToggle
              sort={myTeatimeSort}
              setSort={setMyTeatimeSort}
            /> */}
          </div>
        </section>
        <article className="grid grid-cols-2 grid-rows-auto lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {myTeatimeList.length > 0 ? (
            myTeatimeList.map((el) => (
              <TeatimeListCard key={el.boardId} {...el} />
            ))
          ) : (
            <div>등록된 티타임이 없습니다.</div>
          )}
        </article>
        <footer className="flex justify-center"></footer>
      </section>

      {/* 내가 참여한 티타임 */}
      <section {...props}>
        <h1 className="font-semibold text-2xl mt-5">내가 참여한 티타임</h1>
        <section className="flex items-center justify-between">
          <div className="flex gap-2 mt-5 mb-5">
            <TeatimeListToggle
              sort={participatedTeatimeSort}
              setSort={setParticipatedTeatimeSort}
            />
          </div>
        </section>
        <article className="grid grid-cols-2 grid-rows-auto lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {participatedTeatimeList.length > 0 ? (
            participatedTeatimeList.map((el) => (
              <TeatimeListCard key={el.boardId} {...el} />
            ))
          ) : (
            <div>참여한 티타임이 없습니다.</div>
          )}
        </article>
      </section>
    </>
  );
};

export default TeatimeList;
