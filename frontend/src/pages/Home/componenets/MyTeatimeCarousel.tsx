import { TeatimeListItem } from '../../../types/TeatimeType';
import { useState, useRef } from 'react';
import MyTeatimeCard from './MyTeatimeCard';
import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/solid';

type MyTeatimeCarouselProps = {
  myTeatimeList: TeatimeListItem[];
};

const MyTeatimeCarousel = ({ myTeatimeList }: MyTeatimeCarouselProps) => {
  const [currentSlide, setCurrentSlide] = useState<number>(0);
  const carouselRef = useRef<HTMLDivElement>(null);

  const slideTo = (targetImageNumber: number) => {
    const carouselWidth = carouselRef.current?.clientWidth || 0;
    const targetXPixel = carouselWidth * targetImageNumber + 1;
    carouselRef.current?.scrollTo(targetXPixel, 0);
  };

  const chunkArray = (array: TeatimeListItem[], size: number) => {
    const result = [];
    for (let i = 0; i < array.length; i += size) {
      result.push(array.slice(i, i + size));
    }
    return result;
  };

  const chunks = chunkArray(myTeatimeList, 4);

  const handleNextSlide = () => {
    const nextIndex = (currentSlide + 1) % chunks.length;
    setCurrentSlide(nextIndex);
    slideTo(nextIndex);
  };

  const handlePreviousSlide = () => {
    const prevIndex = (currentSlide - 1 + chunks.length) % chunks.length;
    setCurrentSlide(prevIndex);
    slideTo(prevIndex);
  };
  return (
    <div className="relative">
      <div className="w-[65vw] h-12 top-1/2 transform -translate-y-1/2 left-1/2 transform -translate-x-1/2 hidden absolute lg:flex lg:justify-between">
        <button
          onClick={handlePreviousSlide}
          className="transition ease-in-out hover:scale-125"
        >
          <ChevronLeftIcon className="rounded-full bg-gray-200 size-12 p-3 text-gray-400" />
        </button>
        <button
          onClick={handleNextSlide}
          className="transition ease-in-out hover:scale-125"
        >
          <ChevronRightIcon className="rounded-full bg-gray-200 size-12 p-3 text-gray-400" />
        </button>
      </div>
      <div className="carousel w-full transition gap-4" ref={carouselRef}>
        {chunks.map((chunk, chunkIndex) => (
          <div
            key={chunkIndex}
            id={`slide${chunkIndex}`}
            className="carousel-item relative w-full transition ease-in-out duration-500 gap-4 grid grid-cols-2 lg:grid-cols-4 gap-4"
          >
            {chunk.map((el) => (
              <MyTeatimeCard key={el.boardId} {...el} />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
};

export default MyTeatimeCarousel;
