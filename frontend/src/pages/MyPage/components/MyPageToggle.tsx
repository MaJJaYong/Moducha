import TabButton from '../../../components/Button/TabButton';

interface MyPageToggle {
  currentTab: string;
  setCurrentTab: (data: string) => void;
}

const MyPageToggle = ({ currentTab, setCurrentTab }: MyPageToggle) => (
  <>
    <TabButton
      className="h-8"
      onClick={() => setCurrentTab('myTeatimes')}
      value="myTeatimes"
      currentTab={currentTab}
    >
      나의 티타임
    </TabButton>
    <TabButton
      className="h-8"
      onClick={() => setCurrentTab('myShares')}
      value="myShares"
      currentTab={currentTab}
    >
      나의 나눔
    </TabButton>
    {/* <TabButton
      className="h-8"
      onClick={() => setCurrentTab('myRecords')}
      value="myRecords"
      currentTab={currentTab}
    >
      나의 기록
    </TabButton> */}
    {/* 나의 기록 기능이 구현되지 않음 */}
    <TabButton
      className="h-8"
      onClick={() => setCurrentTab('privacySetting')}
      value="privacySetting"
      currentTab={currentTab}
    >
      개인정보 수정
    </TabButton>
  </>
);

export default MyPageToggle;
