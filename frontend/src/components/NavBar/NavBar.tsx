import { Link, useNavigate } from 'react-router-dom';
import useAuthStore from '../../stores/authStore';
import { handleLogout } from '../../api/logout'; // handleLogout 함수 import
import navModucha from '/logo/nav-moducha.svg'; // navModucha 이미지 경로
import navModuchaMini from '/logo/nav-moducha-mini.svg';
const NavBar = () => {
  const navigate = useNavigate();
  const { isLoggedIn, setLoggedIn, setCurrentUsername } = useAuthStore(
    (state) => ({
      isLoggedIn: state.isLoggedIn,
      setLoggedIn: state.setLoggedIn,
      setCurrentUsername: state.setCurrentUsername,
    })
  );

  const handleLoginClick = () => {
    navigate('/login');
  };

  const handleLogoutClick = () => {
    handleLogout(setLoggedIn, setCurrentUsername);
  };

  return (
    <div className="flex justify-center border-b-2 border-[#eee]">
      <nav className="flex justify-between w-full lg:w-3/5 h-16 items-center">
        <Link
          to="/"
          className="btn btn-ghost hover:bg-none rounded-xl hover:bg-inherit hidden sm:flex"
        >
          <img src={navModucha} alt="nav-logo" />
        </Link>
        <Link
          to="/"
          className="btn btn-ghost hover:bg-none rounded-xl hover:bg-inherit sm:hidden"
        >
          <img src={navModuchaMini} alt="nav-logo_mini" />
        </Link>
        <ul className="flex shrink-0 justify-between items-center gap-4 mr-5">
          {!isLoggedIn && (
            <>
              <li>
                <Link
                  to="/teatimes"
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  티타임
                </Link>
              </li>
              <li>
                <Link
                  to="/shares"
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  나눔
                </Link>
              </li>
              <li>
                <button
                  onClick={handleLoginClick}
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  로그인
                </button>
              </li>
            </>
          )}
          {isLoggedIn && (
            <>
              <li>
                <Link
                  to="/teatimes"
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  티타임
                </Link>
              </li>
              <li>
                <Link
                  to="/shares"
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  나눔
                </Link>
              </li>
              <li>
                <Link
                  to="/mypage"
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  마이페이지
                </Link>
              </li>
              <li>
                <button
                  onClick={handleLogoutClick}
                  className="text-center text-[#6d6d6d] text-lg font-medium hover:text-black"
                >
                  로그아웃
                </button>
              </li>
            </>
          )}
        </ul>
      </nav>
    </div>
  );
};

export default NavBar;
