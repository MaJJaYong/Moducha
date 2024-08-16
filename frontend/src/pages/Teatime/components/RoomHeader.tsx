import roomIcon from '/logo/room-icon.svg';
import { IoChatbubbleEllipsesOutline } from 'react-icons/io5';

interface RoomHeaderProps {
  roomName: string;
  onToggleChat: () => void;
  isChatVisible: boolean;
}

const RoomHeader = ({ roomName, onToggleChat, isChatVisible }: RoomHeaderProps) => {
  return (
    <div
      className="
        room-header
        flex justify-between items-center
        border-b-2
        px-5
      "
    >
      <div
        className="
          header-name
          flex justify-center items-center
        "
      >
        <img src={roomIcon} alt="" className="w-16 h-16 pe-6 border-e-2" />
        <h2 id="room-title" className="ps-6 text-3xl font-bold ">
          {roomName}
        </h2>
      </div>
      {/* TODO 채팅창 활성화 시 버튼 tea 색으로 지속, 종료되면 원래 색으로 */}
      <button 
        className={`
          lg:hidden text-4xl hover:text-tea transition-colors
          ${isChatVisible ? 'text-tea' : ''}
        `} 
        onClick={onToggleChat}>
        <IoChatbubbleEllipsesOutline />
      </button>
    </div>
  );
};

export default RoomHeader;
