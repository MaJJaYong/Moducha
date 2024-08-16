import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode';
// 백엔드에서 accesstoken의 key인 username을 nickname으로 변경하여 코드를 수정하였습니다.
// 프론트엔드에서 currentUsername으로 쓰는 변수는 그대로 유지하였습니다. 따라서 authStore를 참조하는 코드들은 변경할 필요가 없습니다.
interface AuthState {
  isLoggedIn: boolean;
  setLoggedIn: (status: boolean) => void;
  // access token의 username을 zustand에 초기화
  currentUsername: string;
  // 닉네임 변경 페이지에서 닉네임 바꾸면 setCurrentUsername을 쓰세요
  setCurrentUsername: (username: string) => void;
  // currentUserId, currentUserRole 설정,
  //setCurrentUserId, setCurrentUserRole은 만들면 안됩니다.
  currentUserId: string;
  currentUserRole: string;
  currentUserPicture: string;
  token: string | null;
  teatimeToken: string;
  setTeatimeToken: (token: string) => void;
}

interface TokenType {
  nickname: string;
  userId: string;
  role: string;
  picture: string;
}

const useAuthStore = create<AuthState>()(
  devtools((set) => {
    const token = localStorage.getItem('authorization');
    const tokenData = token ? jwtDecode<TokenType>(token) : null;
    const teatimeToken = '';

    return {
      token,
      isLoggedIn: !!tokenData,
      setLoggedIn: (status) => set({ isLoggedIn: status }),
      currentUsername: tokenData?.nickname ?? '',
      setCurrentUsername: (username) => set({ currentUsername: username }),
      currentUserId: tokenData?.userId ?? '',
      currentUserPicture: tokenData?.picture ?? '',
      currentUserRole: tokenData?.role ?? '',
      teatimeToken,
      setTeatimeToken: (token) => set({ teatimeToken: token }),
    };
  })
);

export default useAuthStore;
