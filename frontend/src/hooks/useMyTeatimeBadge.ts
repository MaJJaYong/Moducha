import dayjs from 'dayjs';
import { useEffect, useState } from 'react';
import { FilledBadgeColor } from '../components/Badge/FilledBadge';

const useMyTeatimeBadge = (broadcastDate: string) => {
  const [badgeColor, setBadgeColor] = useState<FilledBadgeColor>('dark');
  const [badgeValue, setBadgeValue] = useState('');
  useEffect(() => {
    if (dayjs() > dayjs(broadcastDate).add(30, 'minutes')) {
      setBadgeColor('dark');
      setBadgeValue('종료');
    } else if (dayjs() < dayjs(broadcastDate).subtract(30, 'minutes')) {
      setBadgeColor('green');
      setBadgeValue('예정');
    } else {
      setBadgeColor('red');
      setBadgeValue('진행 중');
    }
  });
  return [badgeColor, badgeValue];
};
export default useMyTeatimeBadge;
