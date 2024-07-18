const TitleCard = ({ title }: { title: string }) => {
  return (
    <div className="p-5">
      <div className="card bg-base-200 min-w-72 p-4 shadow-xl">
        <h1 className="text-2xl font-semibold">{title}</h1>
      </div>
    </div>
  );
};

export default TitleCard;
