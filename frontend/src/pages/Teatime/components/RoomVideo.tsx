import { LocalVideoTrack, RemoteVideoTrack } from 'livekit-client';
import { useEffect, useRef } from 'react';

interface VideoComponentProps {
  track: LocalVideoTrack | RemoteVideoTrack;
  participantIdentity: string;
  participantName?: string;
  local?: boolean;
}

export default function RoomVideo({
  track,
  participantIdentity,
  participantName,
  local = false,
}: VideoComponentProps) {
  const videoElement = useRef<HTMLVideoElement | null>(null);

  useEffect(() => {
    if (videoElement.current) {
      // attach 관련 설명. 현재 트랙을 html 요소와 연결하고자 할 때 사용.
      // https://docs.livekit.io/realtime/client/receive/
      track.attach(videoElement.current);
    }
    return () => {
      track.detach();
    };
  }, [track]);

  return (
    <div 
      id={'camera-' + participantIdentity} 
      className={`
        relative
        h-full w-full
        overflow-hidden
      `}
    >
      <div 
        className="
          participant-data
          flex flex-col absolute justify-center bottom-1
          rounded-full bg-gray-600/50 min-w-20 m-1 p-2
          text-white text-center text-sm z-10
        ">
        <p>{participantName + (local ? ' (You)' : '')}</p>
      </div>
      <video 
        ref={videoElement} 
        id={track.sid}
        className={`w-full h-full object-cover ${track.source === 'screen_share' ? '' : '-scale-x-100'}`}
      />
    </div>
  );
}
