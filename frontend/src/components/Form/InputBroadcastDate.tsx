import dayjs from 'dayjs';
import dayJsNow from '../../utils/dayJsNow';

type BroadcastProps = {
  pickedDate: string;
  broadcastDate: string;
  setBroadcastDate: (date: string) => void;
};

const InputBroadcastDate = ({
  pickedDate,
  broadcastDate,
  setBroadcastDate,
}: BroadcastProps) => (
  <label className="input input-bordered w-full md:w-1/2 flex items-center gap-2">
    <span className="pr-2 border-r-2">방송</span>
    <input
      className="grow w-4"
      type="datetime-local"
      name="broadcastDate"
      value={broadcastDate}
      onChange={(e) => setBroadcastDate(e.target.value)}
      min={dayJsNow(dayjs(pickedDate).add(1, 'minutes').toString())}
      step={1}
      required
    />
  </label>
);

export default InputBroadcastDate;
