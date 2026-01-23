package com.awesomemin.awesomeshares.controller;

import com.awesomemin.awesomeshares.dto.FileDownloadResponse;
import com.awesomemin.awesomeshares.dto.SharedFileResponse;
import com.awesomemin.awesomeshares.service.SharedFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileController {

    private final SharedFileService sharedFileService;

    @PostMapping("/upload")
    public ResponseEntity<SharedFileResponse> upload(
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest request
    ) {
        String ip = getClientIp(request);
        log.info("Upload request from IP: {}", ip);

        SharedFileResponse response = sharedFileService.upload(files, ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{shareToken}")
    public ResponseEntity<Resource> download(@PathVariable String shareToken) {

        FileDownloadResponse downloadDto = sharedFileService.download(shareToken);
        String encodedFilename = UriUtils.encode(downloadDto.getFilename(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadDto.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition) // ★ 핵심
                .body(downloadDto.getResource());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}