package com.e103.ohmyguide.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Profile("hdfs-log-consumer")
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
