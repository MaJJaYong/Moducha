import { useParams } from 'react-router-dom';
import ArticleCard from '../../components/Article/ArticleCard';
import ArticleContent from '../../components/Article/ArticleContent';
import CommentList from '../../components/Comment/CommentList';
import SideLayout from '../../components/Layout/SideLayout';
import ArticleLoading from '../../components/Loading/ArticleLoading';
import useFetchDetail from '../../hooks/useFetchDetail';
import { ArticleDetailProp } from '../../types/ArticleType';
import { BoardType } from '../../types/BoardType';

const ArticleDetail = () => {
  const { boardType, boardId } = useParams() as {
    boardType: BoardType;
    boardId: string;
  };
  const { articleDetail, isLoading } = useFetchDetail<ArticleDetailProp>(
    boardType,
    boardId
  );
  if (!boardId) return null;
  if (isLoading) return <ArticleLoading />;
  if (!articleDetail) return null;
  return (
    <div className="grid grid-cols-10">
      <SideLayout></SideLayout>
      <main className="col-span-10 lg:col-span-6 md:grid md:grid-cols-12">
        <section className="md:col-span-4 p-2">
          <ArticleCard {...{ ...articleDetail }} />
        </section>
        <article className="md:col-span-8 p-2">
          <ArticleContent {...{ ...articleDetail }}>
            <CommentList boardType={boardType} boardId={parseInt(boardId)} />
          </ArticleContent>
        </article>
      </main>
      <SideLayout></SideLayout>
    </div>
  );
};

export default ArticleDetail;
