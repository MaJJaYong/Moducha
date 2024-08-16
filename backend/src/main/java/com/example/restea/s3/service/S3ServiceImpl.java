package com.example.restea.s3.service;

import static com.example.restea.s3.enums.S3Message.IMAGE_ARRAY_NULL;
import static com.example.restea.s3.enums.S3Message.IMAGE_UPLOAD_FAILED;
import static com.example.restea.s3.enums.S3Message.INVALID_FILE_FORMAT;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.restea.common.dto.ImageRequest;
import com.example.restea.s3.util.UrlParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final AmazonS3 amazonS3;
    private final UrlParser urlParser;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    /**
     * S3 버킷에 파일을 등록하는 메소드
     *
     * @param multipartFile MultiFile
     * @return 파일 업로드 된 URL
     */
    public String uploadSingleFile(MultipartFile multipartFile) {

        String fileName = createFileName(multipartFile.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, IMAGE_UPLOAD_FAILED.getMessage());
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException se) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_FILE_FORMAT.getMessage(fileName));
        }
    }

    /**
     * 사용되지 않는 이미지를 제거하는 메소드 - 글 생성 시
     *
     * @param request ImageRequest를 상속받는 RequestDTO
     * @param <T>     ImageRequest를 상속받는 제네릭
     */
    public <T extends ImageRequest> void deleteImagesNotUsedInContent(T request) {
        List<String> allImages = request.getImages();
        if (allImages == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, IMAGE_ARRAY_NULL.getMessage());
        }
        if (allImages.isEmpty()) {
            return;
        }

        Set<String> contentImages = urlParser.parseContentToSet(request.getContent());
        List<String> extraImages = getExtraImages(allImages, contentImages);

        deleteImagesFromS3(extraImages);
    }

    /**
     * 사용되지 않는 이미지를 제거하는 메소드 - 글 생성 시
     *
     * @param request       ImageRequest를 상속받는 RequestDTO
     * @param contentBefore 수정 전 content
     * @param contentAfter  수정 후 content
     * @param <T>           ImageRequest를 상속받는 제네릭
     */
    @Override
    public <T extends ImageRequest> void deleteImagesNotUsedInContent(T request, String contentBefore,
                                                                      String contentAfter) {

        List<String> allImages = request.getImages();
        if (allImages == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, IMAGE_ARRAY_NULL.getMessage());
        }

        // 수정 후 content에서 사용되지 않은 이미지 삭제
        Set<String> contentAfterImages = urlParser.parseContentToSet(contentAfter);
        deleteImagesFromS3(getExtraImages(allImages, contentAfterImages));

        // 수정 전 content와 수정 후 content를 비교하여 사용되지 않은 이미지 삭제
        List<String> contentBeforeImages = urlParser.parseContentToList(contentBefore);
        deleteImagesFromS3(getExtraImages(contentBeforeImages, contentAfterImages));
    }

    private @NotNull List<String> getExtraImages(List<String> minuend, Set<String> subtrahend) {
        return minuend.stream()
                .filter(image -> !subtrahend.contains(image))
                .toList();
    }

    private void deleteImagesFromS3(List<String> extraImages) {
        extraImages.stream()
                .map(this::getObjectKey)
                .forEach(imageKey -> amazonS3.deleteObject(bucket, imageKey));
    }

    private @NotNull String getObjectKey(String image) {
        return image.substring(image.lastIndexOf("/") + 1);
    }
}
