package ru.sber.agent.webservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.sber.agent.webservice.dto.AgentRequest;
import ru.sber.agent.webservice.dto.AnaliseResults;
import ru.sber.agent.webservice.dto.ArtifactLink;
import ru.sber.agent.webservice.integration.BBDataSource;
import ru.sber.agent.webservice.integration.ConfluenceDataSource;
import ru.sber.agent.webservice.integration.JiraDataSource;
import ru.sber.agent.webservice.service.Dictionary;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
public class DictionaryController {

    @Autowired
    private Dictionary dictionary;


    @PostMapping("/saveDictionary")
    public void saveDict(@RequestBody Map<String, String> dict) {
        dictionary.saveDict(dict);
    }

    @GetMapping("/getDictionary")
    public Map<String, String> getDictionary() {
       return dictionary.getDictionary();
    }

}
