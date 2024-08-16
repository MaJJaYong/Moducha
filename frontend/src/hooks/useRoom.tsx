import { useState, useCallback } from 'react';
import {
  Room,
  RoomEvent,
  RemoteTrack,
  RemoteTrackPublication,
  RemoteParticipant,
  Track,
  TrackPublication,
  Participant,
} from 'livekit-client';
import { GroupedTracks, LocalTrack, SourceKind } from '../types/WebRTCType';
import { liveKitURL } from '../api/mediaServer';
import { useNavigate } from 'react-router-dom';

interface UseRoomProps {
  roomName: string;
  participantName: string;
  teatimeToken: string;
  boardId: string;
}

interface Message {
  sender: string | undefined;
  content: string;
}

export const useRoom = ({
  roomName,
  participantName,
  teatimeToken,
  boardId,
}: UseRoomProps) => {
  const [room, setRoom] = useState<Room | undefined>(undefined);
  const [remoteTracks, setRemoteTracks] = useState<GroupedTracks>({});
  const [messages, setMessages] = useState<Message[]>([]);
  const [isScreenSharing, setIsScreenSharing] = useState<boolean>(false);
  const [localTrack, setLocalTrack] = useState<LocalTrack | undefined>(
    undefined
  );
  const navigate = useNavigate();

  const leaveRoom = useCallback(async () => {
    await room?.disconnect();
    setRoom(undefined);
    setRemoteTracks({});
    setMessages([]);
    setIsScreenSharing(false);
    setLocalTrack(undefined);
    navigate(`/teatimes/${boardId}`);
  }, [room]);

  const setMuteInfo = useCallback(
    (
      remoteTracks: GroupedTracks,
      participant: Participant,
      publication: TrackPublication
    ): GroupedTracks => {
      const newGroupedTracks = { ...remoteTracks };
      if (newGroupedTracks[participant.identity]) {
        newGroupedTracks[participant.identity][
          publication.source as SourceKind
        ] = {
          ...newGroupedTracks[participant.identity][
            publication.source as SourceKind
          ],
          isMute: publication.isMuted,
        };
      }
      return newGroupedTracks;
    },
    []
  );

  const joinRoom = useCallback(async () => {
    const newRoom = new Room();
    setRoom(newRoom);

    newRoom
      .on(
        RoomEvent.TrackSubscribed,
        (
          _track: RemoteTrack,
          publication: RemoteTrackPublication,
          participant: RemoteParticipant
        ) => {
          setRemoteTracks((prev) => {
            const newGroupedTracks = { ...prev };
            if (!newGroupedTracks[participant.identity])
              newGroupedTracks[participant.identity] = {};

            newGroupedTracks[participant.identity][
              publication.source as SourceKind
            ] = {
              participantIdentity: participant.identity,
              participantName: participant.name,
              trackPublication: publication,
              isMute: publication.isMuted,
            };
            return newGroupedTracks;
          });
        }
      )
      .on(
        RoomEvent.TrackUnsubscribed,
        (
          _track: RemoteTrack,
          publication: RemoteTrackPublication,
          participant: RemoteParticipant
        ) => {
          setRemoteTracks((prev) => {
            const newGroupedTracks = { ...prev };
            delete newGroupedTracks[participant.identity][publication.source];
            return newGroupedTracks;
          });
        }
      )
      .on(RoomEvent.DataReceived, (payload, participant) => {
        const decoder = new TextDecoder();
        const message = decoder.decode(payload);
        setMessages((prevMessages) => [
          ...prevMessages,
          { sender: participant?.name, content: message },
        ]);
      })
      .on(RoomEvent.LocalTrackPublished, (publication) => {
        if (publication.track?.source === Track.Source.ScreenShare) {
          setIsScreenSharing(true);
        }
      })
      .on(RoomEvent.LocalTrackUnpublished, (publication) => {
        if (publication.track?.source === Track.Source.ScreenShare) {
          setIsScreenSharing(false);
        }
      })
      .on(
        RoomEvent.TrackMuted,
        (publication: TrackPublication, participant: Participant) => {
          if (participantName !== participant.identity)
            setRemoteTracks((remoteTracks) =>
              setMuteInfo(remoteTracks, participant, publication)
            );
        }
      )
      .on(
        RoomEvent.TrackUnmuted,
        (publication: TrackPublication, participant: Participant) => {
          if (participantName !== participant.identity)
            setRemoteTracks((remoteTracks) =>
              setMuteInfo(remoteTracks, participant, publication)
            );
        }
      );

    try {
      await newRoom.connect(liveKitURL, teatimeToken);
      await newRoom.localParticipant.enableCameraAndMicrophone();
      setLocalTrack({
        localTrack: newRoom.localParticipant.videoTrackPublications
          .values()
          .next().value.videoTrack,
        participantName: newRoom.localParticipant.name,
      });
      console.log('*******************************************');
      console.log('*******************************************');
      console.log(newRoom.localParticipant);
      console.log('*******************************************');
      console.log('*******************************************');
    } catch (error) {
      console.log(
        'There was an error connecting to the room:',
        (error as Error).message
      );
      leaveRoom();
    }
  }, [leaveRoom, participantName, setMuteInfo, teatimeToken]);

  return {
    room,
    roomName,
    remoteTracks,
    messages,
    isScreenSharing,
    localTrack,
    setMessages,
    joinRoom,
    leaveRoom,
  };
};
