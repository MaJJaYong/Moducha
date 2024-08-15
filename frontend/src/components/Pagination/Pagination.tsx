import {
  ChevronLeftIcon,
  ChevronRightIcon,
  ChevronDoubleLeftIcon,
  ChevronDoubleRightIcon,
} from '@heroicons/react/16/solid';
import {
  Dispatch,
  ReactNode,
  useEffect,
  useState,
  MouseEventHandler,
  SetStateAction,
  useCallback,
} from 'react';

interface Page {
  page: number;
  totalPage: number;
  setPage: Dispatch<SetStateAction<number>>;
}

const Pagination = ({ page, totalPage, setPage }: Page) => {
  const perSection = 5;
  const startLimit = Math.max(1, totalPage - perSection + 1);

  const [range, setRange] = useState({
    start: 1,
    end: Math.min(perSection, totalPage),
  });
  const { start, end } = range;

  useEffect(() => {
    const buttons = Array.from({ length: end - start + 1 }, (_, i) => (
      <button
        key={`page_${i + start}`}
        className={`join-item btn rounded-2xl hover:bg-[#ccd5ae] border-none font-bold text-white focus:bg-[#ccd5ae] active:bg-[#ccd5ae] disabled:bg-[#ccd5ae] disabled:text-white ${
          i + start === page ? 'bg-[#ccd5ae]' : 'bg-[#e9edc9]'
        }`}
        onClick={() => setPage(i + start)}
        disabled={page === i + start}
      >
        {i + start}
      </button>
    ));
    setButtonArray(buttons);
  }, [page, start, end, setPage]);

  const [buttonArray, setButtonArray] = useState<ReactNode[]>([]);

  const updateRange = useCallback((newStart: number) => {
    setRange({
      start: newStart,
      end: Math.min(totalPage, newStart + perSection - 1),
    });
    setPage(newStart);
  }, [setPage, totalPage]);

  useEffect(() => {
    updateRange(1);
  }, [totalPage, updateRange]);
  
  const handlePrev = () => updateRange(Math.max(1, start - perSection));
  const handleNext = () =>
    updateRange(Math.min(start + perSection, startLimit));
  const handleFirst = () => updateRange(1);
  const handleLast = () => updateRange(startLimit);

  return (
    <div className="join">
      <PageNavigation onClick={handleFirst} disabled={start === 1}>
        <ChevronDoubleLeftIcon className="size-4" />
      </PageNavigation>
      <PageNavigation onClick={handlePrev} disabled={start === 1}>
        <ChevronLeftIcon className="size-4" />
      </PageNavigation>
      {buttonArray}
      <PageNavigation onClick={handleNext} disabled={end === totalPage}>
        <ChevronRightIcon className="size-4" />
      </PageNavigation>
      <PageNavigation onClick={handleLast} disabled={end === totalPage}>
        <ChevronDoubleRightIcon className="size-4" />
      </PageNavigation>
    </div>
  );
};

const PageNavigation = ({
  children,
  disabled,
  onClick,
}: {
  children: ReactNode;
  disabled?: boolean;
  onClick: MouseEventHandler<HTMLButtonElement>;
}) => (
  <button
    className={
      'join-item btn rounded-2xl hover:bg-[#ccd5ae] border-none text-white bg-[#e9edc9] disabled:bg-[#e9edc9] focus:bg-[#ccd5ae] active:bg-[#ccd5ae]  disabled:text-white'
    }
    onClick={onClick}
    disabled={disabled}
  >
    {children}
  </button>
);

export default Pagination;
