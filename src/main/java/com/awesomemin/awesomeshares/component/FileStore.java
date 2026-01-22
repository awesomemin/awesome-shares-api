package com.awesomemin.awesomeshares.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStore {

    private final Path fileLocation;

    // 생성자: 설정 파일에서 경로 가져와서 폴더 만듦
    public FileStore(@Value("${file.upload-dir}") String uploadDir) {
        this.fileLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileLocation);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장소 폴더를 만들 수 없습니다.", e);
        }
    }

    // 파일이 하나일 때 (유저가 입력한 파일 하나)
    public String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = extractExt(originalFilename);
        String storeFilename = UUID.randomUUID() + ext;

        try {
            Files.copy(file.getInputStream(), this.fileLocation.resolve(storeFilename), StandardCopyOption.REPLACE_EXISTING);
            return storeFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + originalFilename, e);
        }
    }

    // 파일이 여러 개일 때 (유저가 입력한 여러 파일을 하나의 .zip 파일로 저장)
    public String storeFile(File file) {
        String storeFilename = UUID.randomUUID() + ".zip";

        try {
            Path targetLocation = this.fileLocation.resolve(storeFilename);
            Files.move(file.toPath(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storeFilename;
        } catch (IOException e) {
            throw new RuntimeException("압축 파일 저장 실패", e);
        }
    }

    public Resource loadFile(String storeFilename) {
        try {
            Path filePath = this.fileLocation.resolve(storeFilename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) return resource;
            else throw new RuntimeException("파일을 찾을 수 없습니다: " + storeFilename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("경로 오류", e);
        }
    }

    private String extractExt(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
}