package com.awesomemin.awesomeshares.repository;

import com.awesomemin.awesomeshares.domain.FileStatus;
import com.awesomemin.awesomeshares.domain.SharedFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // 1. JPA 관련 설정만 로드해서 가볍게 테스트 (H2 DB 자동 사용)
class SharedFileRepositoryTest {

    @Autowired
    private SharedFileRepository sharedFileRepository;

    @Test
    @DisplayName("UUID 토큰으로 파일을 조회할 수 있다")
    void findByShareToken() {
        // given (준비)
        SharedFile file = new SharedFile(
                "test.jpg", "path/test.jpg", "image/jpeg",
                100L, 1, "123456", "uuid-token-123", "127.0.0.1"
        );
        sharedFileRepository.save(file);

        // when (실행)
        Optional<SharedFile> result = sharedFileRepository.findByShareToken("uuid-token-123");

        // then (검증)
        assertThat(result).isPresent();
        assertThat(result.get().getFilename()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("공유 코드와 상태(READY)가 일치하면 파일이 조회된다")
    void findByShareCodeAndStatus_Success() {
        // given
        SharedFile file = new SharedFile(
                "doc.pdf", "path/doc.pdf", "application/pdf",
                500L, 1, "999999", "uuid-token-999", "127.0.0.1"
        );
        // 테스트를 위해 강제로 상태를 READY로 변경 (원래 생성 시엔 PENDING)
        file.changeStatus(FileStatus.READY);
        sharedFileRepository.save(file);

        // when
        // 코드 "999999"이고 상태가 "READY"인 것을 찾아라
        Optional<SharedFile> result =
                sharedFileRepository.findByShareCodeAndStatus("999999", FileStatus.READY);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getShareCode()).isEqualTo("999999");
    }

    @Test
    @DisplayName("코드는 맞지만 상태가 다르면(EXPIRED 등) 조회되지 않아야 한다")
    void findByShareCodeAndStatus_Fail_StatusMismatch() {
        // given
        SharedFile file = new SharedFile(
                "secret.txt", "path/secret.txt", "text/plain",
                10L, 1, "111111", "uuid-token-111", "127.0.0.1"
        );

        // 상황: 파일이 만료됨 (EXPIRED)
        file.changeStatus(FileStatus.EXPIRED);
        sharedFileRepository.save(file);

        // when
        // 나는 "READY(다운로드 가능)" 상태인 것만 찾고 싶어!
        Optional<SharedFile> result =
                sharedFileRepository.findByShareCodeAndStatus("111111", FileStatus.READY);

        // then
        assertThat(result).isEmpty(); // 없어야 함! (이게 핵심)
    }
}