import { useTeatime } from '../../../hooks/useTeatime';
import dayjs from 'dayjs';

interface TeatimeHostButtonProps {
  boardType: string;
  title: string;
  isLoading: boolean;
  isSuccess: boolean;
  data: any;
}

export default function TeatimeHostButton({
  boardType,
  title,
  isLoading,
  isSuccess,
  data,
}: TeatimeHostButtonProps) {
  const { startTeatime } = useTeatime(boardType, title);
  

  const checkButtonDisable = () => {
    const { broadcastDate } = isSuccess && data;
    const isValid =
      dayjs() >= dayjs(`${broadcastDate}`).subtract(30, 'm') &&
      dayjs() <= dayjs(`${broadcastDate}`).add(30, 'm');

    if (isValid) return false;
    return true;
  };

  return (
    !isLoading &&
    <button
      className="btn rounded bg-tea hover:bg-green-700 text-white"
      onClick={startTeatime}
      disabled={checkButtonDisable()}
    >
      티타임 시작하기
    </button>
  );
}
