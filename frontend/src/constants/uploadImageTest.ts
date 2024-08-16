const genTestImageList = () => {
  const testImageList = [];
  for (let i = 0; i < 10; i++) {
    testImageList.push({
      id: i,
      url: `https://picsum.photos/id/${10 + i}/1600/900`,
    });
  }
  return testImageList;
};
export default genTestImageList;
