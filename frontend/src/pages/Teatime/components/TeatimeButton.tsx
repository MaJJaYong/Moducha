import { useParams } from 'react-router-dom';
import useAuthStore from '../../../stores/authStore';
import TeatimeHostButton from './TeatimeHostButton';
import TeatimeParticipantButton from './TeatimeParticipantButton';
import axiosInstance from '../../../api/axiosInstance';
import { useQuery } from '@tanstack/react-query';

interface TeatimeButtonProps {
  title: string;
  boardType: string;
  nickname: string;
}

export default function TeatimeButton({
  title,
  boardType,
  nickname,
}: TeatimeButtonProps) {
  const { userName } = useAuthStore((state) => ({
    userName: state.currentUsername,
  }));
  const { boardId } = useParams<{ boardId: string }>();
  const { data, isSuccess, isLoading } = useQuery({
    queryKey: ['teatimes'],
    queryFn: fetchTeatimeData,
  });

  async function fetchTeatimeData() {
    return axiosInstance
      .get(`/${boardType}/${boardId}`)
      .then((res) => res.data.data);
  }
  if (isLoading) return null;
  return nickname === userName ? (
    <TeatimeHostButton
      boardType={boardType}
      title={title}
      isLoading={isLoading}
      isSuccess={isSuccess}
      data={data}
    />
  ) : (
    <TeatimeParticipantButton
      boardType={boardType}
      title={title}
      endDate={data.endDate}
    />
  );
}
