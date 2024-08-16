import { useParams, useNavigate } from 'react-router-dom';
import useAuthStore from '../stores/authStore';
import axiosInstance from '../api/axiosInstance';

export const useTeatime = (
  boardType: string,
  title: string,
) => {
  const { boardId } = useParams<{ boardId: string }>();
  const navigate = useNavigate();
  const { userName, accessToken, setTeatimeToken, currentUserId } = useAuthStore((state) => ({
    userName: state.currentUsername,
    accessToken: state.token,
    teatimeToken: state.teatimeToken,
    currentUserId: state.currentUserId,
    setTeatimeToken: state.setTeatimeToken,
  }));

  const teatimeIsOpen = async (): Promise<boolean> => {
    let isOpen = false
    try {
      const response = await axiosInstance.get(`${boardType}/${boardId}/lives`);
      isOpen = response.data.data.open;
    } catch (err) {
      console.error('Error checking if teatime is open:', err);
    }
    return isOpen;
  };

  const teatimeIsApplied = async ():Promise<boolean> => {
    let isApplied = false;
    try {
      const response = await axiosInstance.get(`${boardType}/${boardId}/participants/${currentUserId}`)
      isApplied = response.data.data.participated
      console.log(response.data.data.participated);
    } catch (err) {
      console.log('Error with applied check', err);
    }
    return isApplied;
  }

  const joinTeatime = async () => {
    navigate('/teatimes/room', { state: { roomName: `${title}`, boardId, boardType } });
  };

  const createRoomToken = async () => {
    await axiosInstance
      .post(`${boardType}/${boardId}/lives`)
      .then((res) => {
        setTeatimeToken(res.data.data.token);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const getRoomToken = async () => {
    await axiosInstance
      .post(`${boardType}/${boardId}/lives/token`, {
        headers: { Authorization: `Bearer ${accessToken}` },
      })
      .then((res) => {
        setTeatimeToken(res.data.data.token);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const startTeatime = async () => {
    const isOpen = await teatimeIsOpen();
    if (isOpen) {
      await getRoomToken();
    } else {
      await createRoomToken();
    }
    joinTeatime();
  };

  return { startTeatime, joinTeatime, teatimeIsOpen, teatimeIsApplied };
};
