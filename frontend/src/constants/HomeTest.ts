export const myTeatimeListData = () => {
  const result = [];
  for (let i = 0; i < 6; i++) {
    const item = {
      boardId: i + 1,
      title: '제목',
      content:
        '여러 가지 홍차에 대해 탐구하고 나누는 시간입니다. 부담없이 함께해요!',
      createdDate: '2023-07-15T14:30:00Z',
      lastUpdated: '2023-07-15T14:30:00Z',
      endDate: '2023-08-01T10:00:00Z',
      maxParticipants: 10,
      participants: 5,
      viewCount: 100,
    };
    result.push(item);
  }
  return result
};
