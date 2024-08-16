const SideLayout: React.FC<React.HTMLAttributes<HTMLElement>> = ({
  children,
  className,
  ...props
}: {
  className?: string;
  children?: React.ReactNode;
}) => {
  return (
    <aside className={`hidden lg:flex col-span-2 ${className}`} {...props}>
      {children}
    </aside>
  );
};

export default SideLayout;
