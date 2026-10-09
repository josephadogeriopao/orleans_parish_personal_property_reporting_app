package com.opao.pp_api.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.verification")
@Getter
@Setter
public class VerificationProperties {
    private long codeDurationMs = 3600000; // Safe default fallback
    private String codeDurationText = "1 hour";
}
