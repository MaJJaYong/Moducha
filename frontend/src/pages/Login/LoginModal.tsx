import signInWithGoogle from '../../assets/google/web_light_rd_SI.svg';

const LoginModal = () => {
  const handleLogin = () => {
    window.location.href =
      import.meta.env.VITE_API_URL + '/oauth2/authorization/google';
  };

  return (
    <button className="button" onClick={handleLogin}>
      <img src={signInWithGoogle} alt="signInWithGoogle" />
    </button>
  );
};

export default LoginModal;
