import { Editor, FileLoader, UploadAdapter } from 'ckeditor5';
import axiosInstance from '../../api/axiosInstance';
import { MutableRefObject } from 'react';
import { toast } from 'react-toastify';

class ImageAdapter implements UploadAdapter {
  loader: FileLoader;
  images: MutableRefObject<string[]>;
  constructor(loader: FileLoader, images: MutableRefObject<Array<string>>) {
    this.loader = loader;
    this.images = images;
  }

  upload(): Promise<{ default: string }> {
    return this.loader.file.then(
      (file: File | null) =>
        new Promise((resolve, reject) => {
          const image = new FormData();
          if (!file) throw new Error();
          image.append('upload', file);
          axiosInstance
            .post('/s3/upload', image, {
              headers: { 'Content-Type': 'multipart/form-data' },
            })
            .then((res) => {
              const uploadedUrl = res.data.url;
              this.images.current.push(uploadedUrl);
              resolve({ default: uploadedUrl });
            })
            .catch((err) => {
              if (err && err.response.status === 413) {
                toast.error('5MB 이하의 이미지를 업로드 해 주세요.');
              } else {
                toast.error('이미지 업로드 중 오류가 발생했습니다.');
              }
              reject();
            });
        })
    );
  }
  abort(): void {}
}

export default function ImageAdapterPlugin(
  editor: Editor,
  images: MutableRefObject<string[]>
) {
  editor.plugins.get('FileRepository').createUploadAdapter = (
    loader: FileLoader
  ) => {
    return new ImageAdapter(loader, images);
  };
}
