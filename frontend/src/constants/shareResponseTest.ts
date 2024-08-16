// 아이템 12개의 테스트 나눔리스트 생성
export const genShareListItems = (n: number) => {
  const shareItems = [];
  for (let i = 1; i < n + 1; i++) {
    const shareItem = {
      boardId: i,
      title: '나눔 ' + i,
      createdDate: '2023-07-01T12:00:00Z',
      lastUpdated: '2023-07-10T15:00:00Z',
      content: '보이차 나눔해요 선착순 세분',
      endDate: '2023-07-10T15:00:00Z',
      maxParticipants: 2 + i,
      participants: 1 + i,
      nickname: '엄준식',
      viewCount: i + 10,
    };
    shareItems.push(shareItem);
  }
  return shareItems;
};

export const shareResponse = {
  message: '200 OK',
  pagination: {
    total: 25,
    page: 1,
    perPage: 10,
  },
  data: genShareListItems(12),
};

export const ShareDetailResponse = {
  data: {
    boardId: 1,
    title: '보성녹차 나눔합니다~~',
    content:
      '되벤어사 구븜소잣눌은 라리가하 러락호의 셔구로 마암지 디다거믄가솹으묘, 옥쇤요사는 앗뮬너어 저말티. 여몽옴아 리서를, 챌배를 가낭쇱으면서, 챡녀던 뫼놈소온 사얍걸딘줜신에 돌옹아울은. 언셔스에에 빨지구아게 으강갠어 으히아이 아혀힣잉과, 마오구라. 사지조뚱어서 나홀안번으며 깄세넘의, 힉소한려적을 커를 믄흐 섹리데다. 제른이어 버알으똘라 므바호며 오멪보벼가 번삼딴 재헤는 그므. 시호징갠을 여멈 비추상기 토멘시다 졍셔히, 젬잠묑부바 갤는사는 거댐이 단놜당창야.',
    createdDate: '2023-07-15T10:00:00Z',
    lastUpdated: '2023-07-16T15:30:00Z',
    maxParticipants: 50,
    endDate: '2023-07-19T23:59:59Z',
    viewCount: 100,
    participants: 30, // 현재 참가자
    nickname: '홍길동',
    userId: '10',
  },
};
