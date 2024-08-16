type OutlineBadgeProp = {
  color: 'red' | 'yellow' | 'green' | 'blue';
  children: React.ReactNode;
};
const OutlineBadge = ({ color, children }: OutlineBadgeProp) => {
  const bgColor = {
    red: '#FFEDED',
    yellow: '#FFF3E7',
    green: '#e6f8f6',
    blue: '#e7ebfe',
  };
  const textColor = {
    red: '#F87171',
    yellow: '#EC9748',
    green: '#17BCA6',
    blue: '#6477e6',
  };
  return (
    <div
      className={`flex items-center justify-center w-9 h-5 rounded-lg text-xs border border-2`}
      style={{
        backgroundColor: bgColor[color],
        color: textColor[color],
        borderColor: textColor[color],
      }}
    >
      {children}
    </div>
  );
};

export default OutlineBadge;
