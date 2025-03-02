package ru.sber.agent.webservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "verification")
public class VerificationMatrixProperties {
    private Map<Integer, Range> matrix;

    @Data
    public static class Range {
        private Integer min;
        private Integer max;

        public boolean isInRange(Integer value) {
            if (min != null && max != null) {
                return value >= min && value <= max;
            }
            if (min != null) {
                return value >= min;
            }
            if (max != null) {
                return value <= max;
            }
            throw new RuntimeException("Range is not defined");
        }
    }
}


