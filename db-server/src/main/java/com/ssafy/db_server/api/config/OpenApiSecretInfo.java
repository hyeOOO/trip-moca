package com.ssafy.db_server.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "open-api")
public class OpenApiSecretInfo {
    //누리집 api 요청 키
    private String serviceKey;
}
