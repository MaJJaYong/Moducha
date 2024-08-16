import { useEffect, useState } from 'react';
import axiosInstance from '../api/axiosInstance';
import { BoardType } from '../types/BoardType';
import { ApplicantListItemProps } from '../components/Modal/ApplicantModal';

const useFetchApplicantList = (boardType: BoardType, boardId: number) => {
  const [applicantList, setApplicantList] = useState<ApplicantListItemProps[]>(
    []
  );
  useEffect(() => {
    axiosInstance.get(`${boardType}/${boardId}/participants`).then((res) => {
      if (res.status === 200) {
        setApplicantList(res.data.data);
      }
    });
  }, []);
  return { applicantList };
};

export default useFetchApplicantList;
