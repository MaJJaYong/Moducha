import { useState } from 'react';
import useAuthStore from '../../../stores/authStore';
import axiosInstance from '../../../api/axiosInstance';
import { handleLogout } from '../../../api/logout';
import updateNickname from '../api/updateNickname';
import { toast } from 'react-toastify'; // Toastify 임포트

const AccountDeactivation = () => {
  const { setLoggedIn, setCurrentUsername, currentUsername } =
    useAuthStore.getState();

  const [isEditing, setIsEditing] = useState(false);
  const [nickname, setNickname] = useState(currentUsername);
  const [errorMessage, setErrorMessage] = useState('');

  // 회원탈퇴 API
  const handleDeactivate = async () => {
    const isConfirmed = window.confirm(
      '정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.'
    );
    if (!isConfirmed) {
      return;
    }

    try {
      const response = await axiosInstance.patch('/users/withdraw');

      if (response.status === 204) {
        localStorage.removeItem('authorization');
        toast.success('모두차를 이용해 주셔서 감사합니다');
        handleLogout(setLoggedIn, setCurrentUsername);
      } else {
        console.error('Unexpected response status: ', response.status);
        toast.error('알 수 없는 오류가 발생했습니다.');
      }
    } catch (error) {
      console.error('Network error: ', error.message);
      toast.error('네트워크 오류가 발생했습니다.');
    }
  };

  const handleEditClick = () => {
    setIsEditing(true);
    setErrorMessage(''); // Clear error message when editing
  };

  const handleSaveClick = async () => {
    try {
      const response = await updateNickname({ nickname });
      if (response.error) {
        const { status, message } = response.error;

        if (status >= 400 && message) {
          toast.error(message);
        }
        return;
      }

      const {
        data: { nickname: updatedNickname },
        token,
      } = response;

      if (token) {
        localStorage.setItem('authorization', token);
      }

      setCurrentUsername(updatedNickname);
      toast.success('닉네임이 성공적으로 수정되었습니다.');
      setIsEditing(false);

      // 2초 후 페이지 새로 고침
      setTimeout(() => {
        window.location.reload();
      }, 2000);
    } catch (error) {
      console.error('Error updating nickname: ', error.message);
      toast.error('닉네임 변경 규칙을 읽으세요!!!');
    }
  };

  return (
    <>
      <div className="mt-4">
        <label htmlFor="nickname" className="block text-lg font-semibold">
          닉네임
        </label>
        {isEditing ? (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              handleSaveClick();
            }}
          >
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className="border border-gray-300 p-2 rounded"
              placeholder="닉네임을 입력하세요"
            />
            <button
              type="submit"
              className="ml-2 font-semibold bg-blue-500 text-white p-2 rounded hover:bg-blue-600"
            >
              수정완료
            </button>
          </form>
        ) : (
          <div>
            <span>{nickname}</span>
            <button
              onClick={handleEditClick}
              className="ml-2 font-semibold text-blue-500 hover:underline"
            >
              닉네임 수정하기
            </button>
          </div>
        )}
        {isEditing && (
          <div className="mt-4 text-sm text-gray-600">
            <p>닉네임은 2~12자의 별명만 가능해요.</p>
            <p>
              닉네임은 특수문자를 포함하거나 숫자 단독으로 사용할 수 없어요.
            </p>
            <p>닉네임은 연속으로 공백을 넣을 수 없어요.</p>
            <p>닉네임은 같은 이름으로 수정할 수 없어요.</p>
          </div>
        )}
      </div>
      <div className="mt-4">
        <h1 className="font-semibold text-2xl">회원 탈퇴</h1>
        <p className="text-red-500">
          작성한 글, 댓글, 대댓글은 삭제하지 않으면 탈퇴 이후에도 남겨집니다.
        </p>
        <button
          onClick={handleDeactivate}
          className="font-semibold bg-red-500 text-white p-2 rounded hover:bg-red-600"
        >
          회원 탈퇴
        </button>
        <br />
      </div>
    </>
  );
};

export default AccountDeactivation;
