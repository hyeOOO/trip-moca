package com.ssafy.enjoyTrip.global.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ssafy.enjoyTrip.global.s3.exception.FileDeleteException;
import com.ssafy.enjoyTrip.global.s3.exception.FileUploadException;
import com.ssafy.enjoyTrip.global.s3.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    public String uploadFile(MultipartFile file) {
        try {
            String fileName = createFileName(file.getOriginalFilename());

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(
                    new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
            );

            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    private String createFileName(String originalFileName) {
        return UUID.randomUUID().toString() + getFileExtension(originalFileName);
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new InvalidFileException("잘못된 형식의 파일입니다.");
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3.deleteObject(bucket, fileName);
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            throw new FileDeleteException("파일 삭제 중 오류가 발생했습니다.");
        }
    }

    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}
