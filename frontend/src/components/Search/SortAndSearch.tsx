import {
  Disclosure,
  DisclosureButton,
  DisclosurePanel,
} from '@headlessui/react';
import { XMarkIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { Dispatch, SetStateAction } from 'react';
import SearchBox from './SortAndSearchBox';
import TabButton from '../Button/TabButton';

interface Prop {
  sort: string;
  setSort: Dispatch<SetStateAction<string>>;
}
export interface PageDataType {
  page: number;
  totalPage: number;
  setPage: Dispatch<SetStateAction<number>>;
  setSearchBy: Dispatch<SetStateAction<string>>;
  setKeyword: Dispatch<SetStateAction<string>>;
}

interface SearchProps extends Prop {
  pageData: PageDataType;
}
const SortAndSearch = ({ sort, setSort, pageData }: SearchProps) => {
  return (
    <Disclosure as="div" className="justify-between w-full">
      <div className="flex justify-between">
        <CollapseButton />
        {/* md 이상인 경우 검색창 노출 */}
        <SearchBox pageData={pageData} className="hidden md:flex join" />
        <ToggleSort {...{ sort, setSort }} />
      </div>

      {/* md 이하인 경우 검색창 collapse 패널 노출 */}
      <DisclosurePanel
        transition
        className="md:hidden transition duration-200 ease-out data-[closed]:-translate-y-6 data-[closed]:opacity-0"
      >
        <SearchBox pageData={pageData} className="join w-full mt-3" />
      </DisclosurePanel>
    </Disclosure>
  );
};

const CollapseButton = () => (
  <DisclosureButton className="group relative inline-flex items-center justify-center btn md:hidden bg-tea text-white hover:bg-tea rounded-xl">
    <MagnifyingGlassIcon
      aria-hidden="true"
      className="h-6 w-6 group-data-[open]:hidden"
    />
    <XMarkIcon
      aria-hidden="true"
      className="hidden h-6 w-6 group-data-[open]:block"
    />
  </DisclosureButton>
);

const ToggleSort = ({ sort, setSort }: Prop) => (
  <div className="flex gap-2 items-center">
    <TabButton
      currentTab={sort}
      value="latest"
      onClick={() => setSort('latest')}
    >
      최신 순
    </TabButton>
    <TabButton
      currentTab={sort}
      value="urgent"
      onClick={() => setSort('urgent')}
    >
      마감일 순
    </TabButton>
  </div>
);

export default SortAndSearch;
