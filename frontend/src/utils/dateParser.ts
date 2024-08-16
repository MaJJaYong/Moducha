import dayjs from 'dayjs';
const dateParser = (dateString: string | undefined) => {
  const dateParsed = dayjs(dateString).format('YYYY. MM. DD. HH:mm');

  return dateParsed;
};
export default dateParser;
