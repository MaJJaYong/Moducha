import TabButton from '../../../components/Button/TabButton';

interface TeatimeListToggle {
  sort: string;
  setSort: (data: string) => void;
}

const TeatimeListToggle = ({ sort, setSort }: TeatimeListToggle) => (
  <>
    <TabButton
      className="h-8"
      onClick={() => setSort('latest')}
      value="latest"
      currentTab={sort}
    >
      최신 순
    </TabButton>
    <TabButton
      className="h-8"
      onClick={() => setSort('urgent')}
      value="urgent"
      currentTab={sort}
    >
      마감 순
    </TabButton>
  </>
);

export default TeatimeListToggle;
