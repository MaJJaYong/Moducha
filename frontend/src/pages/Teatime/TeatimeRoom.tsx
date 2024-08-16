import { useLocation } from 'react-router-dom';
import { useRoom } from '../../hooks/useRoom';
import useAuthStore from '../../stores/authStore';
import { useEffect, useState } from 'react';
import RoomHeader from './components/RoomHeader';
import RoomVideoAudioTracks from './components/RoomVideoAudioTracks';
import RoomChatting from './components/RoomChatting';

export default function TeatimeRoom() {
  const { state: {
    roomName,
    boardId,
    boardType
  }} = useLocation();
  const { userName, teatimeToken, token } = useAuthStore((state) => ({
    token: state.token,
    userName: state.currentUsername,
    teatimeToken: state.teatimeToken,
  }));
  const {
    joinRoom,
    room,
    isScreenSharing,
    localTrack,
    remoteTracks,
    messages,
    setMessages,
    leaveRoom,
  } = useRoom({
    roomName,
    participantName: userName,
    teatimeToken,
    boardId
  });
  
  const [isChatVisible, setIsChatVisible] = useState<boolean>(false);
  const toggleChatVisibility = () => {
    setIsChatVisible((prev) => !prev)
  }

  useEffect(() => {
    joinRoom();
  }, [token]);

  useEffect(() => {
    return () => {
      const cleanUp = async () =>{
        await room?.disconnect();
      }
      cleanUp();
    }
  }, [room, token])

  const apiData = {
    roomName,
    boardId,
    boardType
  }

  return (
    <div className="room-wrapper grid grid-cols-12">
      {room && (
        <div 
          id="room" 
          className="
            col-span-12
            lg:col-start-2 lg:col-span-10 
            md:col-span-12
          "
        >
          <RoomHeader roomName={roomName} onToggleChat={toggleChatVisibility} isChatVisible={isChatVisible} />
          <div
            className="
              room-container 
              lg:grid lg:grid-cols-12
              flex flex-col
              items-center
            "
          >
            <RoomVideoAudioTracks
              room={room}
              isScreenSharing={isScreenSharing}
              localTrack={localTrack}
              remoteTracks={remoteTracks}
              participantName={userName}
              leaveRoom={leaveRoom}
              apiData={apiData}
            />
            <RoomChatting
              room={room}
              userName={userName}
              messages={messages}
              isChatVisible={isChatVisible}
              setMessages={setMessages}
            />
          </div>
        </div>
      )}
    </div>
  );
}
