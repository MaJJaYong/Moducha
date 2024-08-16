import { EVDown, EVDownItem, EVDownMenu } from '../Dropdown/EllipsisDropdown';
import { PencilIcon, TrashIcon } from '@heroicons/react/24/outline';

interface Dropdown {
  handleDelete: () => void;
  handleUpdate: () => void;
}

const DropdownMenu = ({ handleDelete, handleUpdate }: Dropdown) => (
  <EVDown>
    <EVDownMenu className="w-24">
      <EVDownItem onClick={handleUpdate} className="p-2">
        <PencilIcon className="size-5" />
        <span>수정</span>
      </EVDownItem>
      <EVDownItem onClick={handleDelete} className="p-2">
        <TrashIcon className="size-5" />
        삭제
      </EVDownItem>
    </EVDownMenu>
  </EVDown>
);

export default DropdownMenu;
