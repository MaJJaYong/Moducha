const MainLayout: React.FC<React.HTMLAttributes<HTMLElement>> = ({
  children,
  className,
  ...props
}: {
  children?: React.ReactNode;
  className?: string;
}) => {
  return (
    <main
      className={`col-span-10 m-5 lg:col-span-6 flex flex-col ${className}`}
      {...props}
    >
      {children}
    </main>
  );
};

export default MainLayout;
