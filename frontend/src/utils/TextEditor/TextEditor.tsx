import { CKEditor } from '@ckeditor/ckeditor5-react';
import { ClassicEditor } from 'ckeditor5';
import editorConfig from './editorConfig';
import 'ckeditor5/ckeditor5.css';
import './TextEditor.css';
import { Dispatch, MutableRefObject, SetStateAction } from 'react';
import ImageAdapterPlugin from './ImageAdapter';

interface EditorType {
  content?: string;
  images: MutableRefObject<string[]>;
  setInput: Dispatch<SetStateAction<string>>;
}

export default function TextEditor({ content, images, setInput }: EditorType) {
  return (
    <CKEditor
      editor={ClassicEditor}
      config={editorConfig}
      onReady={(editor) => {
        ImageAdapterPlugin(editor, images);
      }}
      data={content}
      onBlur={(_event, editor) => setInput(editor.getData())}
    />
  );
}
