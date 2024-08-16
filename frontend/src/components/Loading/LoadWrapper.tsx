import NoArticleList from '../Article/NoArticleList';
import ArticleListLoading from './ArticleListLoading';

type LoadWrapperProps = {
  isLoading: boolean;
  listLength: number;
  children: React.ReactNode;
};

const LoadWrapper = ({ isLoading, listLength, children }: LoadWrapperProps) => {
  if (isLoading) return <ArticleListLoading />;
  if (!listLength) return <NoArticleList />;
  return <>{children}</>;
};

export default LoadWrapper;
