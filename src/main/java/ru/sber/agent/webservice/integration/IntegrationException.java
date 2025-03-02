package ru.sber.agent.webservice.integration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class IntegrationException extends Exception {


    public IntegrationException(String system) {
        super();
        logError(system);
    }

    public IntegrationException(String system, String message) {
        super(message);
        logError(system, message);
    }

    public IntegrationException(String system, String message, Throwable cause) {
        super(message, cause);
        logError(system, message, cause);
    }

    private void logError(String system) {
        log.error("Ошибка интеграции с системой " + system);
    }
    private void logError(String system, String message) {
        log.error("Ошибка интеграции с системой {}: {}\n", system, message );
    }
    private void logError(String system, String message, Throwable cause) {
        log.error("Ошибка интеграции с системой {}: {}\n{}", system, message, cause );
    }

}
