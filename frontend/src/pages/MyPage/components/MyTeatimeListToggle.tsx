import TabButton from '../../../components/Button/TabButton';

interface TeatimeListToggle {
  sort: string;
  setSort: (data: string) => void;
}

const TeatimeListToggle = ({ sort, setSort }: TeatimeListToggle) => (
  <>
    <TabButton
      className="h-8"
      onClick={() => {
        console.log('Ongoing tab clicked');
        setSort('ongoing');
      }}
      value="ongoing"
      currentTab={sort}
    >
      진행 중
    </TabButton>
    <TabButton
      className="h-8"
      onClick={() => {
        console.log('Before tab clicked');
        setSort('before');
      }}
      value="before"
      currentTab={sort}
    >
      마감
    </TabButton>
  </>
);

export default TeatimeListToggle;
