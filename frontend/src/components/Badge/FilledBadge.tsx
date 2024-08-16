export type FilledBadgeColor = 'red' | 'green' | 'dark';

type FilledBadgeProp = {
  color: FilledBadgeColor;
  children: React.ReactNode;
};
const FilledBadge = ({ color, children }: FilledBadgeProp) => {
  const bgColor = {
    red: '#FF5140',
    green: '#26BB69',
    dark: '#787878',
  };

  return (
    <div
      className="flex items-center justify-center px-2 py-1 rounded-lg text-xs text-white shrink-0"
      style={{
        backgroundColor: bgColor[color],
      }}
    >
      {children}
    </div>
  );
};

export default FilledBadge;
