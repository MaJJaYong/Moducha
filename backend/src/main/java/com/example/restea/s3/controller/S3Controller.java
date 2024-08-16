package com.example.restea.s3.controller;

import com.example.restea.s3.dto.ImageUploadResponse;
import com.example.restea.s3.service.S3Service;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;


    @PostMapping("/api/v1/s3/upload")
    public ImageUploadResponse createImage(
            @RequestPart(name = "upload", required = false) MultipartFile file) throws IOException {

        String url = s3Service.uploadSingleFile(file);

        return ImageUploadResponse.from(url);
    }
}
