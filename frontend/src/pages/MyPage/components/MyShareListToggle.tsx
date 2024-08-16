import TabButton from '../../../components/Button/TabButton';

interface MyShareListToggleProps {
  sort: 'before' | 'ongoing';
  setSort: (sort: 'before' | 'ongoing') => void;
}
const MyShareListToggle = ({ sort, setSort }: MyShareListToggleProps) => (
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

export default MyShareListToggle;
