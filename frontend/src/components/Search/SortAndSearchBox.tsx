import { useRef } from 'react';
import { CiSearch } from 'react-icons/ci';
import { PageDataType } from './SortAndSearch';

const SearchBox = ({
  pageData,
  className,
}: {
  pageData: PageDataType;
  className: string;
}) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const selectRef = useRef<HTMLSelectElement>(null);
  const { setSearchBy, setKeyword, setPage } = pageData;
  const handleSearch = () => {
    if (
      !inputRef.current ||
      !inputRef.current.value.trim() ||
      !selectRef.current
    ) {
      return;
    }
    setSearchBy(selectRef.current.value);
    setKeyword(inputRef.current.value);
    setPage(1);
  };
  return (
    <div className={className}>
      <input
        type="text"
        maxLength={20}
        className="input input-bordered w-full join-item"
        placeholder="검색"
        required
        ref={inputRef}
      />
      <select
        className="select select-bordered join-item"
        defaultValue={'title'}
        ref={selectRef}
      >
        <option value="title">제목</option>
        <option value="content">내용</option>
        <option value="writer">작성자</option>
      </select>
      <div className="indicator">
        <button
          onClick={handleSearch}
          className="btn join-item bg-tea text-white hover:bg-emerald-600 rounded-xl"
        >
          <CiSearch size={36} />
        </button>
      </div>
    </div>
  );
};

export default SearchBox;
