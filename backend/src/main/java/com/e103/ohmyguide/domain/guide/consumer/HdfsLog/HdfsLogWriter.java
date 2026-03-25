package com.e103.ohmyguide.domain.guide.consumer.HdfsLog;

import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Profile("hdfs-log-consumer")
@Slf4j
@Component
@RequiredArgsConstructor
public class HdfsLogWriter {

    private final FileSystem fileSystem;

    public void writeLogs(List<UserGoLogMessage> logs) {
        if (logs.isEmpty()) {
            return;
        }

        String date = LocalDate.now().toString();
        String fileName = "log_" + System.currentTimeMillis() + ".csv";
        Path path = new Path("/user-logs/" + date + "/" + fileName);

        try (FSDataOutputStream outputStream = fileSystem.create(path, true)) {
            for (UserGoLogMessage logEntry : logs) {
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
