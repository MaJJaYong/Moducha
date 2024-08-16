import React from 'react';

interface ArticleCardButtonProps extends React.ComponentProps<'button'> {
  className?: string;
  children?: React.ReactNode;
  onClick?: (event: React.MouseEvent<HTMLElement>) => void;
}

const ArticleCardButton = ({
  className,
  children,
  ...props
}: ArticleCardButtonProps) => {
  return (
    <button
      className={`btn rounded font-bold bg-tea hover:bg-green-700 text-white ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default ArticleCardButton;
