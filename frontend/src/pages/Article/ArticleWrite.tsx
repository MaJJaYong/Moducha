import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import TextEditor from '../../utils/TextEditor/TextEditor';
import axiosInstance from '../../api/axiosInstance';
import { BoardType } from '../../types/BoardType';
import useAuthStore from '../../stores/authStore';
import InputTitle from '../../components/Form/InputTitle';
import InputDate from '../../components/Form/InputDate';
import dayJsNow from '../../utils/dayJsNow';
import InputBroadcastDate from '../../components/Form/InputBroadcastDate';
import InputParticipants from '../../components/Form/InputParticipants';
import dayjs from 'dayjs';
import TitleCard from '../../components/Title/TitleCard';
import { toast } from 'react-toastify';

const ArticleWrite = () => {
  const { boardType } = useParams() as { boardType: BoardType };
  const [pickedDate, setPickedDate] = useState<string>(
    dayJsNow(dayjs().add(1, 'minutes').toString())
  );
  const [broadcastDate, setBroadcastDate] = useState<string>(
    dayJsNow(dayjs().add(2, 'minutes').toString())
  );
  const [content, setContent] = useState<string>('');
  const isLoggedIn = useAuthStore((state) => state.isLoggedIn);
  const images = useRef<string[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    // 비로그인 상태인 경우 로그인 페이지로 리디렉션
    if (!isLoggedIn) {
      navigate('/login');
    }
  });
  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (content === '') {
      toast.error('본문을 입력해 주세요.');
      return;
    }
    const formData = new FormData(event.currentTarget);
    const inputData = {
      title: formData.get('title'),
      endDate: formData.get('endDate') + 'Z',
      maxParticipants: formData.get('maxParticipants'),
      ...(boardType === 'teatimes' && {
        broadcastDate: formData.get('broadcastDate') + 'Z',
      }),
      content,
      images: images.current,
    };

    try {
      const response = await axiosInstance.post(`/${boardType}`, inputData);
      const boardId = response.data.data.boardId;
      navigate(`/${boardType}/${boardId}`);
    } catch (error) {
      toast.error('글 작성에 실패하였습니다. 잠시 후 다시 시도해 주세요.');
    }
  };
  // 비로그인 상태인 경우 useEffect 훅 이전에 표시되지 않도록 null 리턴
  if (!isLoggedIn) return null;
  if (!(boardType == 'shares' || boardType == 'teatimes')) {
    throw new Error();
  }
  return (
    <div className="grid grid-cols-12">
      <aside className="hidden lg:flex col-span-3"></aside>
      <main className="col-span-12 lg:col-span-6 flex flex-col h-screen m-5 gap-4">
        <TitleCard>
          <span className="text-disabled">
            {boardType === 'teatimes' ? '티타임' : '나눔'} 글쓰기
          </span>
        </TitleCard>
        <hr />
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <InputTitle />
          <InputDate
            pickedDate={pickedDate}
            setPickedDate={setPickedDate}
            setBroadcastDate={setBroadcastDate}
          />
          {boardType === 'teatimes' && (
            <InputBroadcastDate
              pickedDate={pickedDate}
              broadcastDate={broadcastDate}
              setBroadcastDate={setBroadcastDate}
            />
          )}
          <InputParticipants boardType={boardType} />
          <TextEditor images={images} setInput={setContent} />
        </form>
      </main>
      <aside className="hidden lg:flex col-span-3"></aside>
    </div>
  );
};

export default ArticleWrite;
