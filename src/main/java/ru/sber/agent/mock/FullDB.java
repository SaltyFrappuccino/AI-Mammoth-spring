package ru.sber.agent.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class FullDB {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Для тестирования механизма свкерки
    @Scheduled(fixedDelay = 10)
    public void executeTask() {
        Random random = new Random();
        String issueKey = "EKOB2BCP-" + (random.nextInt(6001) + 1000);
        String deleteSql = "DELETE FROM hac.verification WHERE issue_id = ?";
        jdbcTemplate.update(deleteSql, issueKey);
        String sql = "INSERT INTO hac.verification (issue_id, num_of_linked_bugs, ai_convergance) VALUES (?, ?, ?)";
        int numOfBugs = randomExp();
        jdbcTemplate.update(sql, issueKey, 0, numOfBugs);
        log.info("Создал таску {}, ожидаемое количество багов - {}", issueKey, numOfBugs);
    }

    public static Integer randomExp() {
        Random random = new Random();
        int previousNumber = -1;
        int currentNumber;
        int count = 0;

        for (int i = 0; i < 10; i++) {
            currentNumber = random.nextInt(4) + 1;
            if (currentNumber == previousNumber) {
                count++;
            } else {
                count = 0;
            }
            previousNumber = currentNumber;
        }
        log.info("Количество подряд выкинутых дублей за на десятую попытку: {}", count);
        return count;
}
}