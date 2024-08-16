package com.example.restea.s3.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3Message {
    IMAGE_UPLOAD_FAILED("이미지 업로드에 실패했습니다."),
    INVALID_FILE_FORMAT("잘못된 형식의 파일(\"%s\") 입니다."),
    IMAGE_ARRAY_NULL("이미지 배열이 null 입니다.");

    private final String message;

    public String getMessage(String fileName) {
        return String.format(message, fileName);
    }
}