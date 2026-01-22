package com.awesomemin.awesomeshares.repository;

import com.awesomemin.awesomeshares.domain.FileStatus;
import com.awesomemin.awesomeshares.domain.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    // shareToken으로 조회
    Optional<SharedFile> findByShareToken(String shareToken);

    // 6자리 숫자 코드 + 'READY' 상태인 것 조회
    Optional<SharedFile> findByShareCodeAndStatus(String shareCode, FileStatus status);

    // 상태가 status면서 만료 시간이 date 이전인 파일 모두 조회
    List<SharedFile> findByStatusAndExpireAtBefore(FileStatus status, LocalDateTime date);

    // 특정 status인 파일 모두 조회
    List<SharedFile> findByStatus(FileStatus status);
}