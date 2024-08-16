import { CameraIcon } from '@heroicons/react/24/outline';
import { ImageList } from '../../types/ArticleType';
const ArticleImageUpload = ({ imageList }: { imageList: ImageList }) => {
  return (
    <div className="flex gap-2">
      <button className="btn bg-cornsilk hover:bg-wood hover:text-white size-28 overflow-clip p-2 flex flex-col">
        <CameraIcon className="size-8" />
        <span className="text-xs">0/10</span>
      </button>
      {/* 업로드 이미지 순회하며 버튼 생성 */}
      <div className="flex w-full gap-2 overflow-x-scroll">
        {imageList.map((el) => (
          <button
            key={el.id}
            className="btn bg-cornsilk hover:bg-wood size-28 overflow-clip p-0 flex flex-col"
          >
            <img src={el.url} alt="" />
          </button>
        ))}
      </div>
    </div>
  );
};

export default ArticleImageUpload;
