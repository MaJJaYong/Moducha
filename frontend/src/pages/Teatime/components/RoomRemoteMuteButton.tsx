import axios from "axios";
import useConfigureUrls from "../../../hooks/useConfigureUrls";

interface RoomRemoteMuteButtonProps {
  
}

export default function RoomRemoteMuteButton() {
  const {APPLICATION_SERVER_URL} = useConfigureUrls(); 

  const handleMuteRemoteUserVideo = () => {
    axios({
      method: 'post',
      url: `${APPLICATION_SERVER_URL}/teatimes/7/lives/mute/1`,
      headers: {
        'Content-Type': 'application/json',
      },
      data: {
        userId: `${participantIdentity}`,
        trackSid: `${trackPublication.videoTrack!.trackSid}`
      }
    })
    .then((res) => {
      console.log(res);
      
    })
    .catch((err) => {
      console.log(err);
      
    })
  }

  return (
    <button
      className="bg-yellow-500 text-white px-4 py-1 rounded ml-2 hover:bg-yellow-600"
      onClick={handleMuteRemoteUserVideo}
    >
      REMOTE USER MUTE VIDEO
    </button>
  );
}