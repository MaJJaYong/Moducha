interface Prop {
  children?: React.ReactNode;
  value?: string;
  currentTab?: string;
  className?: string;
  onClick?: () => void;
}

const TabButton = ({
  children,
  value,
  currentTab,
  className,
  onClick,
}: Prop) => {
  return (
    <button
      className={
        `hover:bg-teabg flex rounded-xl border-2 px-4 py-2 items-center font-semibold ${className} ` +
        (value === currentTab
          ? 'bg-teabg text-tea border-teabg'
          : 'text-neutral-500 border-dborder')
      }
      onClick={onClick}
    >
      {children}
    </button>
  );
};

export default TabButton;
