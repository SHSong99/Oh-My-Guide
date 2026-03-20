package com.e103.ohmyguide;

import com.e103.ohmyguide.global.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class OhMyGuideApplication {

    public static void main(String[] args) {
        SpringApplication.run(OhMyGuideApplication.class, args);
    }

}
