import React, { useEffect, useRef, useState } from 'react';
import { Room } from 'livekit-client';
import { Message } from '../../../types/WebRTCType';
import { RiMailSendLine } from 'react-icons/ri';
import { FaLeaf } from 'react-icons/fa';
import { LuLeaf } from 'react-icons/lu';
import { BiSolidLeaf } from 'react-icons/bi';

interface RoomChattingProps {
  room: Room;
  messages: Message[];
  userName: string;
  isChatVisible: boolean;
  setMessages: React.Dispatch<React.SetStateAction<Message[]>>;
}

const RoomChatting = ({ room, messages, userName, isChatVisible, setMessages }: RoomChattingProps) => {
  const messageEndRef = useRef<HTMLDivElement | null>(null);
  const [inputMessage, setInputMessage] = useState<string>('');
  useEffect(() => {
    messageEndRef.current?.scrollIntoView();
  }, [messages]);

  const sendMessage = () => {
    if (inputMessage.trim() && room) {
      const encoder = new TextEncoder();
      const data = encoder.encode(inputMessage);
      room.localParticipant.publishData(data, { reliable: true });
      setMessages((prevMessages) => {
        let newMessages = [];
        if (prevMessages) newMessages = [...prevMessages];
        else newMessages.push({ sender: 'Me', content: inputMessage });

        // return newMessage;
        return [
           ...prevMessages, {sender: 'Me', content: inputMessage }
        ]
      });
      setInputMessage('');
    }
  };

  return (
    // Height 문제 정말 모르겠다.
    <div
      id='chat-container'
      className={`
        chat-container
        grid-rows-12 col-span-12
        absolute flex flex-col w-11/12 z-50 mt-10 mx-auto
        transition-all duration-300 transform
        ${isChatVisible ? 
          'translate-y-0 opacity-1'
          : 'translate-y-full opacity-0 h-auto'
        }
        h-[calc(100vh-200px)] lg:h-[calc(100vh-200px)]
        lg:grid lg:col-span-3 lg:translate-y-full lg:transform-none
        lg:flex-none lg:w-auto lg:mt-0 lg:mx-0 lg:static lg:transition-none
        lg:opacity-1 lg:opacity-100
      `}
    >
      <div
        className={`
          chat-header 
          flex justify-center items-center
          row-span-1
          text-3xl text-tea
          flex-wrap
          gap-6
          ${isChatVisible ? 'h-20 bg-white' : ''}
        `}
      >
        {/* <div 
          className='
            w-full h-3/5 
            border-b-3 border-solid
            flex items-center justify-center
            text-center text-2xl
            border-b-2
          '
        >
        </div> */}
        <FaLeaf /> <LuLeaf /> <BiSolidLeaf /> 
      </div>
      <div
        id="chat-message-box"
        className={`
          overflow-y-auto
          row-span-9
          bg-gray-200
          ${isChatVisible ? 'h-full' : ''}
        `}
      >
        <>
          {messages.length === 0 && (
            <div className='flex justify-center items-center flex-wrap'> 
              <span className='max-w-60 text-center mt-10'>{userName}님, 반갑습니다! <br/><br/> 따뜻한 시간 되세요~!!!</span>  
            </div>
          )}
          {messages.length !== 0 && messages.map((msg, index) => (
            <div
              key={index}
              className={`
                px-1 w-full my-2
              `}
            >
              {msg.sender !== 'Me' && (
                <div className="">
                  <strong className="pe-1">{msg.sender}:</strong>
                </div>
              )}{' '}
              <div
                className={`w-full flex ${msg.sender === 'Me' ? 'justify-end pe-1' : 'justify-start ps-3'}`}
              >
                <div className="bg-white m-1 p-2 rounded-xl max-w-52 text-sm break-words">
                  <p className='w-full text-wrap'>
                    {msg.content}
                  </p>
                </div>
              </div>
            </div>
          ))}
          <div ref={messageEndRef}></div>
        </>
      </div>
      <div
        id="chat-footer"
        className={`
          relative
          row-span-2
          flex justify-center items-center 
          w-full 
          pt-3 pb-3
          ${isChatVisible ? 'bg-white h-40' : ''}
        `}
      > 
        <textarea
          className="
            border border-gray-300 rounded-3xl
            h-3/4 w-full 
            mx-2 ps-3 py-5 pe-14 align-middle
            outline-none
            overflow-hidden
          "
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
          wrap='hard'
        />
        <button
          className="
            absolute right-2
            bg-blue-500 hover:bg-blue-600 transition-colors
            text-3xl text-white 
            px-3 py-2 me-2
            rounded-full 
          "
          onClick={sendMessage}
        >
          <RiMailSendLine />
        </button>
      </div>
    </div>
  );
};

export default RoomChatting;
