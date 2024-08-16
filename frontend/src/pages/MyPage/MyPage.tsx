// import Records from './components/MyRecords';
import MyShares from './components/MyShares';
import Teatimes from './components/MyTeatimes';
import PrivacySetting from './components/PrivacySetting';
import MainLayout from '../../components/Layout/MainLayout';
import SideLayout from '../../components/Layout/SideLayout';
import TitleCard from '../../components/Title/TitleCard';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../stores/authStore';
import { useEffect, useState } from 'react';
import MyPageToggle from './components/MyPageToggle';

const MyPage = () => {
  const [currentTab, setCurrentTab] = useState('myTeatimes'); // 초기 기본 탭
  const navigate = useNavigate();
  const { isLoggedIn, currentUsername, currentUserPicture } = useAuthStore();
  useEffect(() => {
    // 비로그인 상태인 경우 메인 페이지로 리디렉션
    if (!isLoggedIn) {
      navigate('/');
    }
  }, [isLoggedIn, navigate]);

  // 클릭 시 탭을 privacySetting으로 변경하는 핸들러
  const handleUsernameClick = () => {
    setCurrentTab('privacySetting');
  };

  return (
    <div className="grid grid-cols-10">
      {/* 좌측 사이드바 영역 */}
      <SideLayout></SideLayout>
      <MainLayout className="gap-4">
        <header>
          <TitleCard>
            <div className="flex justify-between items-center">
              <span className="text-disabled">마이페이지</span>
              {isLoggedIn && (
                <div className="flex items-center gap-2">
                  <img
                    src={currentUserPicture}
                    alt="User Avatar"
                    className="w-10 h-10 rounded-full border border-gray-300"
                  />
                  <div
                    className="btn btn-sm text-wood bg-papaya cursor-pointer"
                    onClick={handleUsernameClick} // 클릭 핸들러 추가
                  >
                    {currentUsername}
                  </div>
                </div>
              )}
            </div>
          </TitleCard>
        </header>
        <div className="flex gap-2">
          <MyPageToggle currentTab={currentTab} setCurrentTab={setCurrentTab} />
        </div>

        <div className="content">
          {currentTab === 'myTeatimes' && <Teatimes />}
          {currentTab === 'myShares' && <MyShares />}
          {/* {currentTab === 'myRecords' && <Records />} */}
          {/* 나의 기록 기능이 구현되지 않음 */}
          {currentTab === 'privacySetting' && <PrivacySetting />}
        </div>
      </MainLayout>
      {/* 우측 사이드바 영역 */}
      <SideLayout></SideLayout>
    </div>
  );
};

export default MyPage;
