import { LocalVideoTrack, RemoteTrackPublication } from 'livekit-client';

export type SourceKind = 'camera' | 'microphone' | 'screen_share';

export interface TrackInfo {
  participantIdentity: string;
  participantName?: string;
  trackPublication: RemoteTrackPublication;
  isMute: boolean;
};

export type Message = {
  sender: string | undefined;
  content: string;
};

export interface GroupedTracks {
  [participantIdentity: string]: {
    [key in SourceKind]?: TrackInfo;
  };
};


/**
 * livekit-client에도 동일한 이름을 사용하기 떄문에 차후 수정 필요
 */
export interface LocalTrack {
  localTrack: LocalVideoTrack;
  participantName?: string;
}