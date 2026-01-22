package com.awesomemin.awesomeshares.repository;

import com.awesomemin.awesomeshares.domain.DownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {
}