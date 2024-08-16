//API 작성 완료
import { useEffect, useState } from 'react';
import {
  getMypageRecords,
  MypageRecordListResponse,
} from '../api/getMyRecords';

const Records = () => {
  // 상태 정의
  const [records, setRecords] = useState<MypageRecordListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 데이터 가져오기
  useEffect(() => {
    const fetchRecords = async () => {
      try {
        setLoading(true);
        const response = await getMypageRecords(); // 기본 파라미터 사용
        setRecords(response);
      } catch (error) {
        setError('기록을 가져오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchRecords();
  }, []);

  // 로딩 상태 처리
  if (loading) {
    return (
      <>
        <h1 className="font-semibold text-2xl">나의 기록</h1>
        <div>로딩 중...</div>
      </>
    );
  }

  // 오류 상태 처리
  if (error) {
    return (
      <>
        <h1 className="font-semibold text-2xl">나의 기록</h1>
        <div>{error}</div>
      </>
    );
  }

  // 데이터가 없을 경우 처리
  if (!records || records.data.items.length === 0) {
    return (
      <>
        <h1 className="font-semibold text-2xl">나의 기록</h1>
        <div>기록이 없습니다.</div>;
      </>
    );
  }

  // 데이터 표시
  return (
    <div>
      <h1 className="font-semibold text-2xl">나의 기록</h1>
      <ul>
        {records.data.items.map((record) => (
          <li key={record.recordId} className="mb-4 p-4 border rounded">
            <h2 className="font-bold text-xl">{record.title}</h2>
            <p>{record.content}</p>
            <p className="text-gray-500 text-sm">
              작성일: {new Date(record.createdDate).toLocaleDateString()}
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Records;
