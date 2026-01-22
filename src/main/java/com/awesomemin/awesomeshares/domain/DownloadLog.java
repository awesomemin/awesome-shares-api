package com.awesomemin.awesomeshares.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "download_logs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DownloadLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private SharedFile sharedFile;

    @Column(nullable = false)
    private String downloaderIp;

    private String userAgent;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime downloadedAt;

    public DownloadLog(SharedFile sharedFile, String downloaderIp, String userAgent) {
        this.sharedFile = sharedFile;
        this.downloaderIp = downloaderIp;
        this.userAgent = userAgent;
    }

}