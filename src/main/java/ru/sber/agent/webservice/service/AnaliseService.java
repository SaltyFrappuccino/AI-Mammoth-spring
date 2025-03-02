package ru.sber.agent.webservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import ru.sber.agent.webservice.dto.AgentRequest;
import ru.sber.agent.webservice.dto.AgentResponse;
import ru.sber.agent.webservice.dto.AnaliseResults;
import ru.sber.agent.webservice.dto.ArtifactLink;
import ru.sber.agent.webservice.integration.BBDataSource;
import ru.sber.agent.webservice.integration.ConfluenceDataSource;
import ru.sber.agent.webservice.integration.JiraDataSource;
import ru.sber.agent.webservice.integration.ZephyrDataSource;

import java.util.Objects;

@Slf4j
@Service
public class AnaliseService {

    @Autowired
    private ConfluenceDataSource confluenceDataSource;
    @Autowired
    private JiraDataSource jiraDataSource;
    @Autowired
    private BBDataSource bbDataSource;
    @Autowired
    private ZephyrDataSource zephyrDataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Dictionary dictionary;

    @Value("${agent.host}")
    private String agentHost;

    RestTemplate restTemplate = new RestTemplate();


    public ResponseEntity<AnaliseResults> analise(@RequestBody ArtifactLink artifactLink) {
        log.info("/analise, {}", artifactLink);
        AnaliseResults result = new AnaliseResults();
        HttpStatus status = HttpStatus.OK;
        AgentRequest request = new AgentRequest();
        try {
            request.setRequirements(confluenceDataSource.getData(artifactLink.getConfLink()));
        } catch (Exception e) {
            result.setError(ConfluenceDataSource.SYSTEM, e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            request.setDocumentation(jiraDataSource.getData(artifactLink.getJiraLink()));
        } catch (Exception e) {
            result.setError(JiraDataSource.SYSTEM, e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            String testCases = zephyrDataSource.getData(artifactLink.getTestCases());
            request.setTestCases(Objects.requireNonNullElse(testCases, ""));
        } catch (Exception e) {
            result.setError(ZephyrDataSource.SYSTEM, e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            request.setCode(bbDataSource.getData(artifactLink.getBbLink()));
        } catch (Exception e) {
            result.setError(BBDataSource.SYSTEM, e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if(result.getErrors() == null) {
            try {
                request.setSemanticDb(dictionary.getDictionary());
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                ObjectMapper objectMapper = new ObjectMapper();

                objectMapper.writeValueAsString(request);
                HttpEntity<AgentRequest> requestEntity = new HttpEntity<>(request, headers);

                AgentResponse res = restTemplate.postForObject(agentHost + "/analyze", requestEntity, AgentResponse.class);
                log.info("Ответ агента: {}", res);
                if(res != null && res.getFinalReport() != null && res.getBugs() != null) {
                    result.setAnaliseResult(res.getFinalReport());
                    result.setAnaliseResultPercent(res.getBugs().toString());
                    saveAnaliseResult(result, jiraDataSource.getIssueId(artifactLink.getJiraLink()));
                } else {
                    log.error("Получен не полноценный ответ агента");
                    result.setError("AI агент", "Получен не полноценный ответ агента");
                }
            } catch (Exception e) {
                log.error("Ошибка при вызове агента: {}", e.getMessage());
                result.setError("AI агент", e.getMessage());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return new ResponseEntity<>(result, status);
    }

    private void saveAnaliseResult(AnaliseResults result, String issueKey) {
        String deleteSql = "DELETE FROM hac.verification WHERE issue_id = ?";
        jdbcTemplate.update(deleteSql, issueKey);
        String sql = "INSERT INTO hac.verification (issue_id, num_of_linked_bugs, ai_convergance) VALUES (?, ?, ?)";
        Integer analaizeResult = Integer.parseInt(result.getAnaliseResultPercent());
        jdbcTemplate.update(sql, issueKey, 0, analaizeResult);
    }
}
