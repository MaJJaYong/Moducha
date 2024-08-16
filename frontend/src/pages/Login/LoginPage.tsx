import './LoginPage.css';
import LoginModal from './LoginModal';
import loginImage from '../../assets/login/login-splash.webp';
import useAuthStore from '../../stores/authStore';
import { Navigate } from 'react-router-dom';
const LoginPage = () => {
  const { isLoggedIn } = useAuthStore();
  if (isLoggedIn) {
    return <Navigate to="/mypage" />;
  }

  return (
    <>
      <div className="login-container">
        <div className="image-container">
          <img src={loginImage} alt="Login Background" />
        </div>
        <div className="button-container">
          <div className="login-title">Login</div>
          <div className="google-button">
            {isLoggedIn ? (
              <div>
                <p>[Dev: 로그인 상태입니다]</p>
              </div>
            ) : (
              <div>
                <LoginModal />
                <div className="test-login-button"></div>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default LoginPage;
