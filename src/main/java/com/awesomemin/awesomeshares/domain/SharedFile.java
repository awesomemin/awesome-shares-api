package com.awesomemin.awesomeshares.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "shared_files", indexes = {
        @Index(name = "idx_share_code", columnList = "shareCode")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SharedFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String contentType;

    private long fileSize;
    private int fileCount;

    @Column(length = 6)
    private String shareCode;

    @Column(unique = true)
    private String shareToken;

    private int downloadCount = 0;
    private int maxDownloadCount = 10;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    private String uploaderIp;

    @Column(updatable = false)
    private LocalDateTime uploadedAt;
    private LocalDateTime expireAt;

    public SharedFile(String filename, String path, String contentType, long fileSize, int fileCount, String shareCode, String shareLink, String uploaderIp) {
        this.filename = filename;
        this.path = path;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileCount = fileCount;
        this.shareCode = shareCode;
        this.shareToken = shareLink;
        this.uploaderIp = uploaderIp;

        this.status = FileStatus.PENDING;
        this.uploadedAt = LocalDateTime.now();
        this.expireAt = this.uploadedAt.plusHours(3);
    }

    public void changeStatus(FileStatus status) {
        this.status = status;
    }

    public void increaseDownloadCount() {
        this.downloadCount++;
    }

}