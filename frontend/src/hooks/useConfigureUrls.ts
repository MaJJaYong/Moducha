// For local development, leave these variables empty
// For production, configure them with correct URLs depending on your deployment

type configureUrls = {
  APPLICATION_SERVER_URL: string;
  LIVEKIT_URL: string;
}

export default function useConfigureUrls(): configureUrls {
  // SSAFY TEST URL
  let APPLICATION_SERVER_URL = import.meta.env.VITE_TEST_APP_SERVER_URL;
  let LIVEKIT_URL = import.meta.env.VITE_TEST_LIVEKIT_URL;

  // TUTORIAL TEST URL
  // let APPLICATION_SERVER_URL = import.meta.env.VITE_TEST_APP_SERVER_URL;
  // let LIVEKIT_URL = import.meta.env.VITE_LIVETKIT_URL;

  // LOCAL TEST URL
  // let APPLICATION_SERVER_URL = '';
  // let LIVEKIT_URL = ''; 

  // If APPLICATION_SERVER_URL is not configured, use default value from local development
  if (!APPLICATION_SERVER_URL) {
    if (window.location.hostname === 'localhost') {
      APPLICATION_SERVER_URL = 'http://localhost:6080/';
    } else {
      APPLICATION_SERVER_URL = 'https://' + window.location.hostname + ':6443/';
    }
  }

  // If LIVEKIT_URL is not configured, use default value from local development
  if (!LIVEKIT_URL) {
    if (window.location.hostname === 'localhost') {
      LIVEKIT_URL = 'ws://localhost:7880/';
    } else {
      LIVEKIT_URL = 'wss://' + window.location.hostname + ':7443/';
    }
  }
  return { APPLICATION_SERVER_URL, LIVEKIT_URL };
}