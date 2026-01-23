package com.awesomemin.awesomeshares.dto;

import com.awesomemin.awesomeshares.domain.SharedFile;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SharedFileResponse {
    private String downloadUrl;   // 다운로드 링크 (전체 주소)
    private String shareCode;     // 6자리 숫자 코드
    private LocalDateTime expireAt; // 만료 시간

    public SharedFileResponse(SharedFile file, String domain) {
        // 예: http://localhost:8080 + /download/ + uuid-token
        this.downloadUrl = domain + "/download/" + file.getShareToken();
        this.shareCode = file.getShareCode();
        this.expireAt = file.getExpireAt();
    }
}