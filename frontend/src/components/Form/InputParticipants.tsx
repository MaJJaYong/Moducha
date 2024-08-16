import { BoardType } from '../../types/BoardType';

type ParticipantsInputProps = {
  boardType: BoardType;
  maxParticipants?: number;
  setMaxParticipants?: (max: number) => void;
};
const InputParticipants = ({
  boardType,
  maxParticipants,
  setMaxParticipants,
}: ParticipantsInputProps) => (
  <label className="input input-bordered w-full md:w-1/2 flex items-center gap-2">
    <span className="pr-2 border-r-2">인원</span>
    <input
      className="grow w-4"
      type="number"
      name="maxParticipants"
      min={1}
      max={boardType === 'teatimes' ? 6 : 100}
      value={maxParticipants}
      onChange={
        setMaxParticipants &&
        ((e) => setMaxParticipants(Number(e.target.value)))
      }
      required
    />
  </label>
);

export default InputParticipants;
