package com.e103.ohmyguide.domain.userlog.service;

import com.e103.ohmyguide.domain.userlog.dto.UserLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HdfsLogWriter {

    private final FileSystem fileSystem;

    public void writeLogs(List<UserLogRequest> logs) {
        if (logs.isEmpty()) {
            return;
        }

        String date = LocalDate.now().toString();
        String fileName = "log_" + System.currentTimeMillis() + ".csv";
        Path path = new Path("/user-logs/" + date + "/" + fileName);

        try (FSDataOutputStream outputStream = fileSystem.create(path, true)) {
            for (UserLogRequest logEntry : logs) {
                String csvLine = String.join(",",
                        logEntry.getUserId().toString(),
                        logEntry.getNationality(),
                        String.valueOf(logEntry.getAge()),
                        logEntry.getGender(),
                        logEntry.getTravelPurpose(),
                        logEntry.getLifestyle(),
                        logEntry.getAction(),
                        logEntry.getPlaceId().toString(),
                        logEntry.getTimestamp()
                );
                outputStream.writeBytes(csvLine + "\n");
            }
            log.info("Wrote {} logs to HDFS: {}", logs.size(), path);
        } catch (Exception e) {
            log.error("Failed to write logs to HDFS: {}", path, e);
            throw new RuntimeException("Failed to write logs to HDFS", e);
        }
    }
}