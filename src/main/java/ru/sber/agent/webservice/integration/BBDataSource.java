package ru.sber.agent.webservice.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BBDataSource implements SberWorksDataSource {

    public static final String SYSTEM = "Bitbucket";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth.token.bb}")
    private String token;
    @Value("${connection.bb}")
    private String url;
    @Value("${auth.login}")
    private String login;
    @Value("${auth.pass}")
    private String pass;


    @Override
    public String getData(String link) throws IntegrationException {

        String projectId = getProjectId(link);
        String repoId = getRepoId(link);
        String prId = getPrId(link);


        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        RequestEntity<Object> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(String.format("%s/rest/api/latest/projects/%s/repos/%s/pull-requests/%s.diff", url, projectId, repoId, prId)));
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(requestEntity, String.class);
        } catch (Exception e) {
            throw new IntegrationException(SYSTEM, e.getMessage());
        }


        if (response.getStatusCode().isError()) {
            throw new IntegrationException(SYSTEM, String.format("Ошибочный статус ответа %s", response.getStatusCode()));
        }
        if (response.getBody() == null)
            throw new IntegrationException(SYSTEM, "Пустое тело ответа");

        return response.getBody();
    }

    private String getRepoId(String link) throws IntegrationException {
        Pattern pattern = Pattern.compile("repos/([^/]+)/pull-requests");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            String pageId = matcher.group(1);
            log.info("Репозиторий bitbucket: {}", pageId);
            return pageId;
        }
        throw new IntegrationException(SYSTEM, String.format("Не найден репозиторий для анализа в %s", link));
    }
    private String getPrId(String link) throws IntegrationException {
        Pattern pattern = Pattern.compile("/pull-requests/([^/]+)/overview");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            String pageId = matcher.group(1);
            log.info("Pull request bitbucket: {}", pageId);
            return pageId;
        }
        throw new IntegrationException(SYSTEM, String.format("Не найден pull request для анализа в %s", link));
    }

    private String getProjectId(String link) throws IntegrationException {
        Pattern pattern = Pattern.compile("/projects/([^/]+)/repos");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            String pageId = matcher.group(1);
            log.info("Проектная область bitbucket: {}", pageId);
            return pageId;
        }
        throw new IntegrationException(SYSTEM, String.format("Не найдена проектная область bitbucket для анализа в %s", link));
    }
}
