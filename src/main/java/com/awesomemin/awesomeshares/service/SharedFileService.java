package com.awesomemin.awesomeshares.service;

import com.awesomemin.awesomeshares.component.FileStore;
import com.awesomemin.awesomeshares.domain.FileStatus;
import com.awesomemin.awesomeshares.domain.SharedFile;
import com.awesomemin.awesomeshares.dto.FileDownloadResponse;
import com.awesomemin.awesomeshares.dto.SharedFileResponse;
import com.awesomemin.awesomeshares.repository.SharedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedFileService {

    private final SharedFileRepository sharedFileRepository;
    private final FileStore fileStore;

    @Value("${app.domain:http://localhost:8080}")
    private String domain;

    /**
     * 파일 업로드
     */
    @Transactional
    public SharedFileResponse upload(List<MultipartFile> files, String uploaderIp) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String storeFilename;
        String originalFilename;
        String contentType;
        long fileSize;

        // 파일 1개 vs 여러개 분기
        if (files.size() == 1) {
            MultipartFile file = files.getFirst();
            storeFilename = fileStore.storeFile(file); // 디스크에 저장
            originalFilename = file.getOriginalFilename();
            contentType = file.getContentType();
            fileSize = file.getSize();
        } else {
            File zipFile = createZipFile(files);
            storeFilename = fileStore.storeFile(zipFile); // 디스크에 저장
            originalFilename = files.getFirst().getOriginalFilename() + "외 " + (files.size() - 1) + "건.zip";
            contentType = "application/zip";
            fileSize = zipFile.length();
        }

        String shareCode = generateUniqueShareCode();
        String shareToken = UUID.randomUUID().toString();

        SharedFile sharedFile = new SharedFile(
                originalFilename,
                storeFilename,
                contentType,
                fileSize,
                files.size(),
                shareCode,
                shareToken,
                uploaderIp
        );
        sharedFile.changeStatus(FileStatus.READY);

        // DB에 저장
        sharedFileRepository.save(sharedFile);

        return new SharedFileResponse(sharedFile, domain);
    }

    /**
     * 파일 다운로드
     */
    @Transactional
    public FileDownloadResponse download(String shareToken) {
        SharedFile file = sharedFileRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        // 유효성 검사(만료, 삭제 여부, 횟수 초과 등)
        validateDownloadable(file);

        file.increaseDownloadCount();

        if (file.getDownloadCount() >= file.getMaxDownloadCount()) {
            file.changeStatus(FileStatus.EXPIRED);
        }

        Resource resource = fileStore.loadFile(file.getPath());

        return new FileDownloadResponse(resource, file.getFilename(), file.getContentType());
    }

    private File createZipFile(List<MultipartFile> files) {
        try {
            File zipFile = File.createTempFile("share_", ".zip");

            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                for (MultipartFile file: files) {
                    ZipEntry zipEntry = new ZipEntry(file.getOriginalFilename());
                    zos.putNextEntry(zipEntry);
                    file.getInputStream().transferTo(zos);
                    zos.closeEntry();
                }
            }
            return zipFile;
        } catch (IOException e) {
            throw new RuntimeException("압축 파일 생성 중 오류 발생", e);
        }
    }

    private String generateUniqueShareCode() {
        String code;
        int maxRetry = 10;

        while (maxRetry-- > 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(100000, 1000000);
            code = String.valueOf(randomNum);

            boolean exists = sharedFileRepository
                    .findByShareCodeAndStatus(code, FileStatus.READY)
                    .isPresent();

            if (!exists) {
                return code;
            }
        }
        throw new RuntimeException("공유 코드 생성 실패 (재시도 횟수 초과)");
    }

    private void validateDownloadable(SharedFile file) {
        if(file.getStatus() != FileStatus.READY) {
            throw new IllegalStateException("다운로드 할 수 없는 파일입니다. (만료 또는 삭제됨)");
        }

        if(LocalDateTime.now().isAfter(file.getExpireAt())) {
            file.changeStatus(FileStatus.EXPIRED);
            throw new IllegalStateException("유효 기간이 만료된 파일입니다.");
        }

        if(file.getDownloadCount() >= file.getMaxDownloadCount()) {
            file.changeStatus(FileStatus.EXPIRED);
            throw new IllegalStateException("다운로드 횟수가 초과된 파일입니다.");
        }
    }
}