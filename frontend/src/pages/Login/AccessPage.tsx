// 1. 이 컴포넌트는 스프링 서버에서 redirect해줌. 로딩 및 리프레시를 가리기 위한 빈 페이지입니다.
// js 로직만 구현하며 html 요소는 없습니다.
import { useEffect } from 'react';
// import { jwtDecode } from 'jwt-decode';
import useAuthStore from '../../stores/authStore';

const Access = () => {
  const setLoggedIn = useAuthStore((state) => state.setLoggedIn);
  useEffect(() => {
    console.log('[Dev: Access.tsx Mounted');
    // 2. 쿼리스트링에서 'authorization' 값을 읽어서 localStorage에 저장
    const query = new URLSearchParams(window.location.search);
    const accessToken = query.get('access');

    if (accessToken) {
      localStorage.setItem('authorization', accessToken);
      // console.log(jwtDecode(accessToken));

      // console.log(
      //   'access token localStorage: ' + localStorage.getItem('authorization')
      // );
    } else {
      console.log('error: access token을 localStorage에 저장하지 못했습니다.');
    }
    // 3. authStore.ts에서 localStorage를 보고 isLoggedIn 초기화(done)
    setLoggedIn(!!accessToken); // Access.tsx에서도 초기화. 크게 의미는 없는데 일단 양쪽에 로직을 구현해두었습니다.
    // 4. index.tsx로 redirect, 페이지 리프레시로 인해 자동 로그인 처리
    window.location.replace('/');
  }, [setLoggedIn]);

  return (
    <>
      {/* <button onClick={() => (window.location.href = '/')}>
        [Dev용: 홈으로 redirect]
      </button> */}
    </>
  );
};

export default Access;
