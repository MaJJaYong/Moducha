import useAuthStore from '../../stores/authStore';
import axiosInstance from '../../api/axiosInstance';
import { useNavigate } from 'react-router-dom';
import DropdownMenu from './ArticleContentDropdown';
import { ArticleDetailProp } from '../../types/ArticleType';
interface ArticleContentProp extends ArticleDetailProp {
  children: React.ReactNode
}
const ArticleContent = ({
  boardType,
  title,
  boardId,
  content,
  children,
  userId,
}: ArticleContentProp) => {
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const navigate = useNavigate();
  const handleDelete = () => {
    axiosInstance
      .patch(`/${boardType}/deactivated-${boardType}/${boardId}`)
      .then(() => {
        navigate(`/${boardType}`);
      });
  };
  const handleUpdate = () => {
    navigate('update');
  };

  return (
    <div className="p-4 shadow border flex flex-col gap-4">
      <header className="flex  justify-between items-center border rounded p-4 pr-1 shadow-md">
        <h1 className="text-2xl font-semibold text-wood truncate">{title}</h1>
        {currentUserId === userId && (
          <DropdownMenu
            handleUpdate={handleUpdate}
            handleDelete={handleDelete}
          />
        )}
      </header>
      <article>
        {/* content -> HTML 태그로 렌더링 */}
        <p
          dangerouslySetInnerHTML={{ __html: content }}
          className="my-4 overflow-hidden text-wrap break-words whitespace-normal"
        ></p>
        <hr />
      </article>
      <section>{children}</section>
    </div>
  );
};

export default ArticleContent;
