package ru.sber.agent.webservice.integration;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class JiraDataSource implements SberWorksDataSource {

    public static final String SYSTEM = "Jira";


    private final JiraRestClient jiraRestClient;

    public JiraDataSource(
            @Value("${auth.token.jira}")
            String token,
            @Value("${auth.login}")
            String login,
            @Value("${auth.pass}")
            String pass,
            @Value("${connection.jira}")
            String url
    ) {
        this.jiraRestClient = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(URI.create(url), login, pass);
    }

    @Override
    public String getData(String link) throws IntegrationException {
        String issue;
        if (checkLink(link)) {
            issue = getIssueId(link);
        } else {
            issue = link;
        }

        Issue issueDto;
        try {
            issueDto = jiraRestClient.getIssueClient().getIssue(issue).get();
        } catch (Exception e) {
            throw new IntegrationException(SYSTEM, e.getMessage());
        }

        StringBuilder builder = new StringBuilder();
        builder.append(issueDto.getSummary());
        builder.append("\n");

        builder.append(issueDto.getDescription());
        builder.append("\n");
        IssueField acceptanceCriteria = issueDto.getField("customfield_25800");

        try {
            if (acceptanceCriteria != null) {
                builder.append("Критерии приемки: ");
                if (acceptanceCriteria.getValue() instanceof JSONArray arr) {
                    for (int i = 0; i < arr.length(); i++) {
                        if (arr.get(i) instanceof JSONObject criteria) {
                            builder.append(criteria.getString("name"));
                            builder.append("\n");

                        }

                    }

                }
            }

        } catch (Exception e) {
            log.error("Ошибка обработки критериев приемки: {}", e.getMessage());
        }
        return builder.toString();
    }

    public Integer getAmountOfBugs(String issueKey) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Issue issueDto;
        try {
            issueDto = jiraRestClient.getIssueClient().getIssue(issueKey).get();
        } catch (Exception e) {
            log.error("Ошибка получения данных задачи {}: {}", issueKey, e.getMessage());
            return null;
        }
        Iterable<IssueLink> issueLinks = issueDto.getIssueLinks();
        if (issueLinks == null) return 0;
        AtomicReference<Integer> count = new AtomicReference<>(0);
        issueLinks

                .forEach(iss -> {
                    if (iss.getIssueLinkType().getName().equals("Bugs")) {
                        count.getAndSet(count.get() + 1);
                    }
                });

        return count.get();
    }

    public String getIssueId(String link) throws IntegrationException {
        Pattern pattern = Pattern.compile("/browse/([A-Z0-9-]+)");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            String pageId = matcher.group(1);
            log.info("ID задачи Jira: {}", pageId);
            return pageId;
        }
        throw new IntegrationException(SYSTEM, String.format("Не найдена задача для анализа в %s", link));
    }
}
