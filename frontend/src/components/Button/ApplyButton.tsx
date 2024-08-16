import { BoardType } from '../../types/BoardType';
import ApplicantModal from '../Modal/ApplicantModal';
import ApplyModal from '../Modal/ApplyModal';
import ArticleCardButton from './ArticleCardButton';
import useApplyButtonStatus from './useApplyButtonStatus';

const ApplyButton = ({
  isEnded,
  boardId,
  boardType,
  userId,
}: {
  isEnded: boolean;
  boardId: number;
  boardType: BoardType;
  userId: string;
}) => {
  const { buttonValue, buttonHandler, isDisabled, isAuthor } =
    useApplyButtonStatus({
      isEnded,
      boardId,
      boardType,
      userId,
    });
  return (
    <>
      <ArticleCardButton onClick={buttonHandler.current} disabled={isDisabled}>
        {buttonValue}
      </ArticleCardButton>
      {!isAuthor ? (
        <ApplyModal {...{ boardType, boardId }} />
      ) : (
        <ApplicantModal {...{ boardType, boardId }} />
      )}
    </>
  );
};

export default ApplyButton;
