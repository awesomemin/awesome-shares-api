package com.awesomemin.awesomeshares.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class FileDownloadResponse {
    private Resource resource;
    private String filename;
    private String contentType;
}