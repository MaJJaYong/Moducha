import { Room } from 'livekit-client';
import RoomVideo from './RoomVideo';
import { GroupedTracks, LocalTrack, SourceKind, TrackInfo } from '../../../types/WebRTCType';
import { useEffect, useState } from 'react';
import RoomSharingButton from './RoomSharingButton';
import { GoUnmute, GoMute } from 'react-icons/go';
import RoomRemoteTrack from './RoomRemoteTrack';
import { IoVideocamOffOutline, IoVideocamOutline } from 'react-icons/io5';

interface RoomVideoAudioProps {
  room: Room;
  localTrack: LocalTrack | undefined;
  participantName: string;
  remoteTracks: GroupedTracks;
  isScreenSharing: boolean;
  leaveRoom: () => void;
  apiData: { roomName: string; boardId: string; boardType: string };
}

interface ScreenShareTrack {
  isScreenShare: boolean;
  screenShareTrack?: {
    [key in SourceKind]?: TrackInfo;
  };
}

const RoomVideoAudioTracks = ({
  room,
  localTrack,
  participantName,
  remoteTracks,
  isScreenSharing,
  apiData,
  leaveRoom,
}: RoomVideoAudioProps) => {
  const [isAudioMuted, setIsAudioMuted] = useState<boolean>(false);
  const [isVideoMuted, setIsVideoMuted] = useState<boolean>(false);
  const [screenShare, setScreenShare] = useState<ScreenShareTrack>({
    isScreenShare: false,
  });

  useEffect(() => {
    const screenShareTrack = Object.values(remoteTracks).find(
      (tracks) => tracks?.screen_share
    );

    setScreenShare(() => ({
      isScreenShare: !!screenShareTrack, 
      screenShareTrack: screenShareTrack 
    }));
  }, [remoteTracks]);

  const handleLocalAudio = () => {
    setIsAudioMuted((prev) => !prev);
    room.localParticipant.setMicrophoneEnabled(isAudioMuted);
  };

  const handleLocalVideo = () => {
    setIsVideoMuted((prev) => !prev);
    room.localParticipant.setCameraEnabled(isVideoMuted);
  };

  return (
    <div
      className="
        screen-container 
        col-span-12
        lg:col-span-9
        grid grid-rows-12
        border-e-2 border-e-gray-300
        h-[calc(100vh-200px)]
      "
    >
      <div className="local-share-bg row-span-10 bg-gray-200 grid grid-row-10 p-5">
        <div className="local-share-container relative row-span-6 rounded-2xl overflow-hidden">
          {/* screen_share video start */}
          {screenShare.isScreenShare && screenShare.screenShareTrack?.screen_share && (
            <div className="screen-share-container w-full h-full">
              <RoomVideo
                key={
                  screenShare.screenShareTrack.screen_share.trackPublication.trackSid
                }
                track={
                  screenShare.screenShareTrack.screen_share.trackPublication.videoTrack!
                }
                participantIdentity={
                  screenShare.screenShareTrack.screen_share.participantIdentity
                }
                participantName={
                  screenShare.screenShareTrack.screen_share.participantName
                }
              />
            </div>
          )}
          {/* screen_share video end */}

          {/* local video start */}
          {localTrack && (
            <div
              className={`
                local-video-container
                ${
                  screenShare.isScreenShare
                    ? 'absolute top-0 right-0 w-2/6'
                    : 'w-full h-full'
                }
              `}
            >
              <RoomVideo
                track={localTrack.localTrack}
                participantIdentity={participantName}
                participantName={localTrack.participantName}
                local={true}
              />
            </div>
          )}
          {/* local video end */}
        </div>

        {/* remote video start */}
        <RoomRemoteTrack remoteTracks={remoteTracks} apiData={apiData} />
        {/* remote video end */}
      </div>

      <div 
        className="
          room-controller 
          row-span-2 flex 
          items-center relative justify-between
          md:justify-center 
        "
      >
        <div
          className="
              local-contoller 
              flex gap-3
              ml-3
            "
        >
          <button
            onClick={handleLocalAudio}
            className={`
                ${
                  !isAudioMuted
                    ? 'bg-blue-500 text-white px-4 py-1 rounded hover:bg-blue-600'
                    : 'bg-red-500 text-white px-4 py-1 rounded hover:bg-red-600'
                }
                px-2 py-3
                text-3xl
              `}
          >
            {!isAudioMuted ? <GoUnmute /> : <GoMute />}
          </button>
          <button
            onClick={handleLocalVideo}
            className={`
                ${
                  !isVideoMuted
                    ? 'bg-blue-500 text-white px-4 py-1 rounded hover:bg-blue-600'
                    : 'bg-red-500 text-white px-4 py-1 rounded hover:bg-red-600'
                }
                px-2 py-3
                text-3xl
              `}
          >
            {!isVideoMuted ? <IoVideocamOutline /> : <IoVideocamOffOutline />}
          </button>

          <RoomSharingButton room={room} isScreenSharing={isScreenSharing} />
        </div>
        <button
          className="
              leave-room-button 
              bg-red-500 hover:bg-red-600 rounded
              text-white text-lg
              md:absolute end-1 lg:me-3 
              px-4 py-3 me-3
            "
          onClick={leaveRoom}
        >
          통화 종료
        </button>
      </div>
    </div>
  );
};

export default RoomVideoAudioTracks;
