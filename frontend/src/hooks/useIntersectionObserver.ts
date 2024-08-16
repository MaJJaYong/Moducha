import { useRef } from 'react';

const useIntersectionObserver = (callback: () => void) => {
  const observer = useRef(
    new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            callback();
          }
        });
      },
      { threshold: 1 }
    )
  );

  const observe = (element: HTMLDivElement | null) => {
    if (!element) return;
    observer.current.observe(element);
  };

  const unobserve = (element: HTMLDivElement | null) => {
    if (!element) return;
    observer.current.unobserve(element);
  };

  return [observe, unobserve];
};
export default useIntersectionObserver;
