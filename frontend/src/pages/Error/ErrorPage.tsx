import { TbError404 } from 'react-icons/tb';
import { BiError } from 'react-icons/bi';
import { useNavigate } from 'react-router-dom';

const ErrorPage = () => {
  const navigate = useNavigate();
  return (
    <div className="flex flex-col items-center mt-20 gap-10">
      <TbError404 className="size-96 text-tea rounded-full p-8 border shadow-lg" />
      <div className="flex gap-1 items-center text-warning">
        <BiError className="size-8" />
        <h1 className="text-2xl mt-1 font-bold ">
          요청하신 페이지를 찾지 못했습니다
        </h1>
      </div>
      <button
        onClick={() => navigate('/')}
        className="rounded-lg px-4 py-3 text-xl font-bold text-white shadow-md bg-green-300 transition duration-100 active:bg-green-700 active:scale-90 focus:outline-none hover:bg-green-700 transition"
      >
        메인으로
      </button>
    </div>
  );
};

export default ErrorPage;
