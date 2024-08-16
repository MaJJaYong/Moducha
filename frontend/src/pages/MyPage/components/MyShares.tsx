import { useState, useEffect } from 'react';
import getMyShareWriteList from '../api/getMyShareWriteList';
import getMyShareParticipateList from '../api/getMyShareParticipateList';
import MyShareList from './MyShareList';
import MyShareListToggle from './MyShareListToggle';

const MyShares = () => {
  const [writeList, setWriteList] = useState<any[]>([]);
  const [participatedList, setParticipatedList] = useState<any[]>([]);
  const [writePage, setWritePage] = useState<number>(1);
  const [participatedPage, setParticipatedPage] = useState<number>(1);
  const [writePerPage] = useState<number>(12);
  const [participatedPerPage] = useState<number>(12);
  const [writeTotal, setWriteTotal] = useState<number>(0);
  const [participatedTotal, setParticipatedTotal] = useState<number>(0);
  const [writeSort, setWriteSort] = useState<'before' | 'ongoing'>('ongoing');
  const [participatedSort, setParticipatedSort] = useState<
    'before' | 'ongoing'
  >('ongoing');
  const [writeError, setWriteError] = useState<string | null>(null);
  const [participatedError, setParticipatedError] = useState<string | null>(
    null
  );

  useEffect(() => {
    fetchMyShareWriteList();
    fetchMySharesParticipateList();
  }, [writePage, participatedPage, writeSort, participatedSort]);

  const fetchMyShareWriteList = async () => {
    try {
      const response = await getMyShareWriteList({
        page: writePage,
        perPage: writePerPage,
      });

      const { data, pagination } = response;

      if (!data) {
        setWriteError('작성한 나눔이 없습니다.');
      } else {
        setWriteList(data);
        setWriteTotal(pagination.total);
        setWriteError(null);
      }
    } catch (error) {
      setWriteError('나의 나눔 작성 목록을 가져오는 데 실패했습니다.');
    }
  };

  const fetchMySharesParticipateList = async () => {
    try {
      const response = await getMyShareParticipateList({
        sort: participatedSort,
        page: participatedPage,
        perPage: participatedPerPage,
      });

      const { data, pagination } = response;

      if (!data || data.length === 0) {
        setParticipatedError('참여한 나눔이 없습니다.');
      } else {
        setParticipatedList(data);
        setParticipatedTotal(pagination.total);
        setParticipatedError(null);
      }
    } catch (error) {
      setParticipatedError(
        '내가 참여 신청한 나눔 목록을 가져오는 데 실패했습니다.'
      );
    }
  };

  return (
    <div>
      <h1 className="font-semibold text-2xl mt-5 mb-5">
        내가 작성한 나눔 목록
      </h1>
      {/* <section className="flex items-center justify-between">
        <div className="flex gap-2 mb-5">
          <MyShareListToggle sort={writeSort} setSort={setWriteSort} />
        </div>
      </section> */}

      <MyShareList
        list={writeList}
        error={writeError}
        total={writeTotal}
        page={writePage}
        setPage={setWritePage}
        perPage={writePerPage}
      />

      <div>
        <h1 className="font-semibold text-2xl mt-5 mb-5">
          내가 참여 신청한 나눔 목록
        </h1>
        <section className="flex items-center justify-between">
          <div className="flex gap-2 mb-5">
            <MyShareListToggle
              sort={participatedSort}
              setSort={setParticipatedSort}
            />
          </div>
        </section>

        <MyShareList
          list={participatedList}
          error={participatedError}
          total={participatedTotal}
          page={participatedPage}
          setPage={setParticipatedPage}
          perPage={participatedPerPage}
        />
      </div>
    </div>
  );
};

export default MyShares;
