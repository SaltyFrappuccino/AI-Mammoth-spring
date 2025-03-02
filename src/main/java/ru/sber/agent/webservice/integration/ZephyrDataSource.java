package ru.sber.agent.webservice.integration;

import com.pt.taco.jira.zephyr.ZephyrRestClient;
import com.pt.taco.jira.zephyr.client.model.Step;
import com.pt.taco.jira.zephyr.client.model.TestCase;
import com.pt.taco.jira.zephyr.client.model.TestScript;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ZephyrDataSource {

    public static final String SYSTEM = "Zephyr";

    String LINK_REGEX = "^(https?|ftp):\\/\\/[^\s/$.?#].[^\s]*$";



    private ZephyrRestClient zephyrRestClient;
    public ZephyrDataSource(
            @Value("${auth.token.jira}")
            String token,
            @Value("${auth.login}")
            String login,
            @Value("${auth.pass}")
            String pass,
            @Value("${connection.jira}")
            String url
    ) {
        System.setProperty("JIRA_API_TOKEN", pass);
        System.setProperty("JIRA_USERNAME", login);
        zephyrRestClient = new ZephyrRestClient(url);
    }

    public String getData(List<String> testCases) throws IntegrationException, ExecutionException, InterruptedException {

        StringBuilder builder = new StringBuilder();

        if(testCases != null && testCases.size() > 0) {
            List<String> testCasesIds = getTestCasesIds(testCases);

            builder.append("Тест кейсы: ").append("\n");
            builder.append("\n");
            testCasesIds.forEach((testCase) -> {
                try {
                    TestCase test = zephyrRestClient.getTestCase(testCase);
                    builder.append("Тест кейс: ").append(test.getName()).append("\n");
                    TestScript testScript = test.getTestScript();
                    List<Step> steps = testScript.getSteps();
                    Collections.sort(steps, (step1, step2) -> Integer.compare(step1.getIndex(), step2.getIndex()));

                    testScript.getSteps().forEach((script) -> {
                        builder.append("Step ").append(script.getIndex()).append("\n");
                        builder.append(script.getDescription()).append("\n");
                        builder.append("Expected Result: ").append("\n");
                        builder.append(script.getExpectedResult()).append("\n");
                        script.getTestCase();
                    });

                } catch (Exception e) {
                    log.error("Ошибка получения тест-кейса {}",testCase);
                }
            });
            return builder.toString();
        } else {
            log.warn("Не найдены тест-кейсы для анализа");

        }
        return null;
    }


    public List<String> getTestCasesIds(List<String> links) {
        return links.stream().map(link -> {
            if (checkLink(link)) {
                Pattern pattern = Pattern.compile("/testCase/([A-Z0-9-]+)");
                Matcher matcher = pattern.matcher(link);
                if (matcher.find()) {
                    String pageId = matcher.group(1);
                    log.info("ID тест-кейса Jira: {}", pageId);
                    return pageId;
                }
                log.warn("Не найден тест-кейс для анализа в {}", link);
                return null;
            }
            return link;
        }).collect(Collectors.toList());
    }

    private Boolean checkLink(String link) {
        Pattern pattern = Pattern.compile(LINK_REGEX);
        Matcher matcher = pattern.matcher(link);
        return matcher.matches();
    }
}
