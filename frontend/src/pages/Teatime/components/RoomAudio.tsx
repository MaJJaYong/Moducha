import { LocalAudioTrack, RemoteAudioTrack } from 'livekit-client';
import { useEffect, useRef } from 'react';

interface RoomAudioProps {
  track: LocalAudioTrack | RemoteAudioTrack;
  participantIdentity: string;
}

export default function RoomAudio({ track }: RoomAudioProps) {
  const audioElement = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    if (audioElement.current) {
      track.attach(audioElement.current);
    }

    return () => {
      track.detach();
    };
  }, [track]);

  return (
    <div id='audio-box'>
      <audio ref={audioElement} id={track.sid} />
    </div> 
  );
}