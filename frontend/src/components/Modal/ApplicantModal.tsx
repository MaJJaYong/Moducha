import useFetchApplicantList from '../../hooks/useFetchApplicantList';
import { BoardType } from '../../types/BoardType';

interface ApplicantModalProps {
  boardType: BoardType;
  boardId: number;
}

const ApplicantModal = ({ boardType, boardId }: ApplicantModalProps) => {
  const { applicantList } = useFetchApplicantList(boardType, boardId);
  return (
    <dialog id="applicant_modal" className="modal">
      <div className="modal-box max-w-lg">
        <header className="mb-4">
          <h1 className="text-xl font-bold text-blue-500">신청자 목록</h1>
        </header>
        {applicantList.length ? (
          <main>
            <div className="w-full overflow-x-auto">
              <table className="min-w-full border-collapse">
                <thead>
                  <tr className="bg-gray-100">
                    <th className="py-2 px-4 text-left border-b border-gray-300">
                      이름
                    </th>
                    <th className="py-2 px-4 text-left border-b border-gray-300">
                      전화번호
                    </th>
                    <th className="py-2 px-4 text-left border-b border-gray-300">
                      주소
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {applicantList.map((el) => (
                    <ApplicantListItem
                      key={`applicant_${el.participantId}`}
                      {...el}
                    />
                  ))}
                </tbody>
              </table>
            </div>
          </main>
        ) : (
          <div className="text-lg text-disabled">아직 참여자가 없어요</div>
        )}
      </div>
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
};

export interface ApplicantListItemProps {
  participantId: number;
  createdDate: string;
  name: string;
  phone: string;
  address: string;
  userId: number;
  boardId: number;
  nickname: string;
}

const ApplicantListItem = ({
  name,
  phone,
  address,
}: ApplicantListItemProps) => {
  return (
    <tr className="hover:bg-gray-50">
      <td className="py-2 px-4 border-b border-gray-200 font-bold">{name}</td>
      <td className="py-2 px-4 border-b border-gray-200">{phone}</td>
      <td className="py-2 px-4 border-b border-gray-200">{address}</td>
    </tr>
  );
};

export default ApplicantModal;
