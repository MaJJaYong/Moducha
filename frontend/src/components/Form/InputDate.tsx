import dayjs from 'dayjs';
import dayJsNow from '../../utils/dayJsNow';

type DateInputProps = {
  pickedDate: string;
  setPickedDate: (date: string) => void;
  setBroadcastDate: (date: string) => void;
};

const InputDate = ({
  pickedDate,
  setPickedDate,
  setBroadcastDate,
}: DateInputProps) => {
  const handleChange = (e: React.FormEvent<HTMLInputElement>) => {
    const value = (e.target as HTMLInputElement).value;
    setPickedDate(value);
    setBroadcastDate(dayJsNow(dayjs(value).add(1, 'minutes').toString()));
  };
  return (
    <label className="input input-bordered w-full md:w-1/2 flex items-center gap-2">
      <span className="pr-2 border-r-2">마감</span>
      <input
        className="grow w-4"
        type="datetime-local"
        name="endDate"
        value={pickedDate}
        onChange={handleChange}
        min={dayJsNow(dayjs().add(1, 'minutes').toString())}
        step={1}
        required
      />
    </label>
  );
};
export default InputDate;
