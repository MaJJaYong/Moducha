const TitleCard = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className="card min-w-72 p-4 shadow">
      <h1 className="text-2xl font-semibold">{children}</h1>
    </div>
  );
};

export default TitleCard;
