import { useEffect, useRef, useState } from 'react';
import useAuthStore from '../../stores/authStore';
import { BoardType } from '../../types/BoardType';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../api/axiosInstance';

interface Props {
  isEnded: boolean;
  boardId: number;
  boardType: BoardType;
  userId: string;
}
const useApplyButtonStatus = ({
  isEnded,
  boardId,
  boardType,
  userId,
}: Props) => {
  const [buttonValue, setButtonValue] = useState('');
  const buttonHandler = useRef(() => {});
  const [isDisabled, setIsDisabled] = useState(false);
  const [isParticipated, setIsParticipated] = useState(false);
  const navigate = useNavigate();
  const { isLoggedIn, currentUserId } = useAuthStore((state) => ({
    isLoggedIn: state.isLoggedIn,
    currentUserId: state.currentUserId,
  }));
  const isAuthor = userId === currentUserId;
  useEffect(() => {
    if (isLoggedIn) {
      axiosInstance
        .get(`/${boardType}/${boardId}/participants/${currentUserId}`)
        .then((res) => {
          if (res.status === 200) {
            setIsParticipated(res.data.data.participated);
          }
        });
    }
  }, []);

  useEffect(() => {
    if (!isLoggedIn) {
      if (!isEnded) {
        setButtonValue('참여하려면 로그인 하세요');
        buttonHandler.current = () => {
          navigate('/login');
        };
      } else {
        setButtonValue('마감되었습니다');
        setIsDisabled(true);
      }
    } else {
      if (isAuthor) {
        setButtonValue('참여자 보기');
        buttonHandler.current = () => {
          (
            document.getElementById('applicant_modal') as HTMLDialogElement
          )?.showModal();
        };
      } else {
        if (isEnded) {
          setButtonValue('마감되었습니다');
          setIsDisabled(true);
        } else {
          if (isParticipated) {
            setButtonValue('취소하기');
            buttonHandler.current = () => {
              console.log(currentUserId);
              axiosInstance
                .delete(
                  `/${boardType}/${boardId}/participants/${currentUserId}`
                )
                .then((res) => {
                  if (res.status === 200) {
                    setButtonValue('참여하기');
                    buttonHandler.current = () => {
                      (
                        document.getElementById(
                          'apply_modal'
                        ) as HTMLDialogElement
                      )?.showModal();
                    };
                    window.location.reload();
                  }
                });
            };
          } else {
            setButtonValue('참여하기');
            buttonHandler.current = () => {
              (
                document.getElementById('apply_modal') as HTMLDialogElement
              )?.showModal();
            };
          }
        }
      }
    }
  }, [isEnded, isLoggedIn, isAuthor, isParticipated, navigate]);

  return { buttonValue, buttonHandler, isDisabled, isAuthor };
};

export default useApplyButtonStatus;
