const InputTitle = ({
  title,
  setTitle,
}: {
  title?: string;
  setTitle?: (title: string) => void;
}) => (
  <header className="flex items-center justify-between gap-2">
    <label className="input input-bordered w-full md:w-1/2 items-center flex gap-2">
      <span className="pr-2 border-r-2">제목</span>
      <input
        className="grow w-4"
        type="text"
        placeholder="제목을 입력하세요"
        name="title"
        maxLength={50}
        value={title}
        onChange={setTitle && ((e) => setTitle(e.target.value))}
        required
      />
    </label>
    <input
      className="btn text-wood bg-papaya hover:bg-wood hover:text-white"
      type="submit"
      value="글쓰기"
    />
  </header>
);
export default InputTitle;
