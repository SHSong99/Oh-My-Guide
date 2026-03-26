package com.e103.ohmyguide.global.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;

@Profile("hdfs-log-consumer")
@Slf4j
@Configuration
public class HdfsConfig {

    @Value("${hdfs.uri}")
    private String hdfsUri;

    @Value("${hdfs.user}")
    private String hdfsUser;

    @Bean
    public FileSystem fileSystem() throws Exception {
        org.apache.hadoop.conf.Configuration configuration = new org.apache.hadoop.conf.Configuration();
        configuration.set("fs.defaultFS", hdfsUri);
        configuration.set("dfs.client.use.datanode.hostname", "true");

        System.setProperty("HADOOP_USER_NAME", hdfsUser);

        try {
            FileSystem fs = FileSystem.get(new URI(hdfsUri), configuration, hdfsUser);
            log.info("HDFS FileSystem connected: {}", hdfsUri);
            return fs;
        } catch (Exception e) {
            log.error("Failed to connect to HDFS: {}. Error: {}", hdfsUri, e.getMessage());
            throw e;
        }
    }
}
