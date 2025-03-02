package ru.sber.agent.webservice.job;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.agent.webservice.configuration.VerificationMatrixProperties;
import ru.sber.agent.webservice.dto.IssueVerification;
import ru.sber.agent.webservice.integration.IntegrationException;
import ru.sber.agent.webservice.integration.JiraDataSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class VerificationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JiraDataSource jiraDataSource;

    @Autowired
    private VerificationMatrixProperties vmp;
    @Value("${verification-month-duration}")
    private Integer vrificationMounthDuration;

    @Scheduled(fixedDelayString = "${verification-delay}")
    public void verify() throws IntegrationException {
        log.info("Запуск задачи синхронизации с Jira и пересчета сверки");

        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(vrificationMounthDuration);
        Timestamp monthAgoTimestamp = Timestamp.valueOf(monthAgo);

        String sql = "SELECT issue_id FROM hac.verification WHERE created_at >= ?";
        List<String> issues = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("issue_id");
        }, monthAgoTimestamp);
        for (String issue : issues) {
            log.info("Обновление количество привязанных багов для задачи {}", issue);
            Integer amountOfBugs = jiraDataSource.getAmountOfBugs(issue);
            if(Objects.isNull(amountOfBugs)) {
                log.warn("Не найдено количество багов по задаче {}. Удаляем из сверки", issue);
                String drop = "DELETE FROM hac.verification WHERE issue_id = ?";
                jdbcTemplate.update(drop, issue);

            } else {
                log.info("Количество багов по задаче {} равно {}", issue, amountOfBugs);
                String up = "UPDATE hac.verification SET num_of_linked_bugs = ? WHERE issue_id = ?";
                jdbcTemplate.update(up, amountOfBugs, issue);
            }

        }

        String verfSql = "SELECT * FROM hac.verification WHERE created_at >= ?";
        List<IssueVerification> issueVerification = jdbcTemplate.query(verfSql, (rs, rowNum) -> {
            return new IssueVerification(
                    rs.getString("issue_id"),
                    rs.getInt("num_of_linked_bugs"),
                    rs.getInt("ai_convergance"),
                    rs.getTimestamp("created_at")
            );
        }, monthAgoTimestamp);
        Integer countVerify = countVerify(issueVerification);

        String saveCount = "UPDATE hac.verification_count SET verification_percent = ? WHERE id = 1";
        jdbcTemplate.update(saveCount, countVerify);
    }

    public Integer countVerify(List<IssueVerification> issueVerification) {
        AtomicInteger avg = new AtomicInteger();
        double sum = 0;
        for (IssueVerification iv : issueVerification) {
            var range = vmp.getMatrix().get(iv.getAiConvergance());
            if(range == null) {
                log.warn("{} Встречено неожидаемое значение количества багов: {}, пропускаем оценку", iv.getIssueId(), iv.getAiConvergance());
            } else if (range.isInRange(iv.getNumOfLinkedBugs())) {
                avg.incrementAndGet();
                log.info("{} Gigachat предсказал фактическое количество багов({}) по задаче c указанной точностью {}-{}. ожидалось {}",iv.getIssueId(), iv.getNumOfLinkedBugs(),range.getMin(),range.getMax(), iv.getAiConvergance());
            } else {
                log.info("{} Gigachat НЕ предсказал фактическое количество багов({}) по задаче c указанной точностью {}-{}. ожидалось {}",iv.getIssueId(), iv.getNumOfLinkedBugs(),range.getMin(),range.getMax(), iv.getAiConvergance());
            }
            sum++;
        }
        return (int) Math.round((avg.get() *100)/sum);

    }

}
