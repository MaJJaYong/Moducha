import { EllipsisVerticalIcon } from '@heroicons/react/24/outline';

type ItemType = {
  className?: string;
  onClick?: () => void;
  children?: React.ReactNode;
};

export const EVDown = ({ className, children }: ItemType) => {
  return (
    <div className="dropdown dropdown-end">
      <button
        tabIndex={0}
        role="button"
        className={`flex items-center size-8 text-gray-400 rounded-xl hover:bg-gray-100 focus:bg-gray-100 ${className}`}
      >
        <EllipsisVerticalIcon />
      </button>
      {children}
    </div>
  );
};

export const EVDownItem = ({ className, onClick, children }: ItemType) => {
  return (
    <li>
      <a className={className} onClick={onClick}>
        {children}
      </a>
    </li>
  );
};

export const EVDownMenu = ({ className, children }: ItemType) => (
  <ul
    tabIndex={0}
    className={`dropdown-content menu bg-base-100 rounded-box z-[1] drop-shadow ${className}`}
  >
    {children}
  </ul>
);
