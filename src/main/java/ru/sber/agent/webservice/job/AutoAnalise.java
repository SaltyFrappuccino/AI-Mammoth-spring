package ru.sber.agent.webservice.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.agent.webservice.dto.AnaliseResults;
import ru.sber.agent.webservice.dto.ArtifactLink;
import ru.sber.agent.webservice.dto.IssueVerification;
import ru.sber.agent.webservice.integration.IntegrationException;
import ru.sber.agent.webservice.service.AnaliseService;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty(name = "auto-analyze", havingValue = "true")
@Component
@Slf4j
public class AutoAnalise {

    @Autowired
    private AnaliseService analisService;

    @Scheduled(fixedDelayString = "100000")
    @ConditionalOnProperty(name = "auto-analyze", havingValue = "true")
    public void analiseDb() {
        log.info("Запуск задачи авто анализа");
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = AutoAnalise.class.getResourceAsStream("/data_for_analise.json");

        try {
            List<ArtifactLink> links = objectMapper.readValue(inputStream, new TypeReference<List<ArtifactLink>>() {});
            log.info("База задач для анализа: {}", links.size());
            for (ArtifactLink link : links) {
                TimeUnit.SECONDS.sleep(2);
                try {
                    log.info("Анализ задачи {}", link.getJiraLink());
                    ResponseEntity<AnaliseResults> analise = analisService.analise(link);
                    if (analise.getBody().getErrors() != null) {
                        log.info("Анализ НЕ проведен для задачи {}", link.getJiraLink());
                    } else if (analise.getBody().getAnaliseResult() != null && analise.getBody().getAnaliseResultPercent() != null) {
                        log.info("Анализ проведен для задачи {}", link.getJiraLink());
                    } else {
                        log.error("Нету результатов анализа для задачи {}", link.getJiraLink());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Ошибка анализа задачи {}", link.getJiraLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
