import dayjs from 'dayjs';
const dayJsNow = (time?: string) => dayjs(time).format('YYYY-MM-DDTHH:mm');
export default dayJsNow;
