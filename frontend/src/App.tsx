import { lazy, Suspense } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Layout from './components/Layout/Layout';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
const Home = lazy(() => import('./pages/Home/Home'));
const Login = lazy(() => import('./pages/Login/LoginPage'));
const MyPage = lazy(() => import('./pages/MyPage/MyPage'));
const Teatime = lazy(() => import('./pages/Teatime/Teatime'));
const ArticleDetail = lazy(() => import('./pages/Article/ArticleDetail'));
const Share = lazy(() => import('./pages/Share/Share'));
const Notifications = lazy(() => import('./pages/Notifications/Notifications'));
const AccessPage = lazy(() => import('./pages/Login/AccessPage'));
const ArticleWrite = lazy(() => import('./pages/Article/ArticleWrite'));
const ArticleUpdate = lazy(() => import('./pages/Article/ArticleUpdate'));
const TeatimeRoom = lazy(() => import('./pages/Teatime/TeatimeRoom'));
const ErrorPage = lazy(() => import('./pages/Error/ErrorPage'));

const queryClient = new QueryClient();

// Router 인스턴스 생성, 자식인 Layout 컴포넌트로 페이지 레이아웃 세팅
// 새로운 컴포넌트를 추가하려면 children에 등록해 주세요
// 추가된 컴포넌트들은 Layout의 Outlet에 렌더링 됩니다
const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'login',
        element: <Login />,
      },
      {
        path: 'teatimes',
        element: <Teatime />,
      },
      {
        path: 'shares',
        element: <Share />,
      },
      {
        path: ':boardType/write',
        element: <ArticleWrite />,
      },
      {
        path: ':boardType/:boardId/update',
        element: <ArticleUpdate />,
      },
      {
        path: ':boardType/:boardId',
        element: <ArticleDetail />,
      },
      {
        path: 'teatimes/room',
        element: <TeatimeRoom />,
      },
      {
        path: 'mypage',
        element: <MyPage />,
      },
      {
        path: 'notifications',
        element: <Notifications />,
      },
    ],
  },
  { path: 'access', element: <AccessPage /> }, // access token 처리용 더미 페이지
]);

// RouterProvider에 라우트 객체들이 렌더링
const App = () => {
  return (
    <>
      <QueryClientProvider client={queryClient}>
        <Suspense>
          <RouterProvider router={router} />
        </Suspense>
        <ToastContainer />
      </QueryClientProvider>
    </>
  );
};

export default App;
