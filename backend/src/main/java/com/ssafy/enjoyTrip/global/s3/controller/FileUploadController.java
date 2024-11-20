package com.ssafy.enjoyTrip.global.s3.controller;

import com.ssafy.enjoyTrip.global.s3.dto.UploadResponse;
import com.ssafy.enjoyTrip.global.s3.exception.InvalidFileException;
import com.ssafy.enjoyTrip.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/domain/upload")
@RequiredArgsConstructor
public class FileUploadController {
    private final S3Service s3Service;

    @PostMapping
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("파일이 비어있습니다.");
        }

        validateFileSize(file);
        validateFileType(file);

        String imageUrl = s3Service.uploadFile(file);
        return ResponseEntity.ok(new UploadResponse(imageUrl));
    }

    private void validateFileSize(MultipartFile file) {
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileException("이미지 파일만 업로드 가능합니다.");
        }
    }
}
