// 아이템 12개의 테스트 나눔리스트 생성
export const genMockList = (n: number) => {
  const mockItems = [];
  for (let i = 1; i < n + 1; i++) {
    const mockItem = {
      userId: `${i}`,
      boardId: i,
      title: `티타임 ${i}`,
      content:
        '여러 가지 홍차에 대해 탐구하고 나누는 시간입니다. 부담없이 함께해요~',
      createdDate: '2023-07-15T14:30:00Z',
      lastUpdated: '2023-07-15T14:30:00Z',
    };
    mockItems.push(mockItem);
  }
  return mockItems;
};

export const mockList = {
  message: '200 OK',
  pagination: {
    total: 25,
    page: 1,
    perPage: 10,
  },
  data: genMockList(12),
};

export const mockDetail = {
  data: {
    userId: "1",
    boardId: 1,
    title: 'Tea Time Event',
    content: 'Join us for a relaxing tea time event.',
    createdDate: '2023-07-15T10:00:00Z',
    lastUpdated: '2023-07-16T15:30:00Z',
    broadcastDate: '2023-07-20T14:00:00Z',
    maxParticipants: 50,
    endDate: '2023-07-19T23:59:59Z',
    viewCount: 100,
    participants: 30,
    nickname: '홍길동',
  },
};
