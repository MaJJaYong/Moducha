package com.example.restea.s3.service;

import com.example.restea.common.dto.ImageRequest;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadSingleFile(MultipartFile multipartFile) throws IOException;

    <T extends ImageRequest> void deleteImagesNotUsedInContent(T request);

    <T extends ImageRequest> void deleteImagesNotUsedInContent(T request, String contentBefore, String contentAfter);
}