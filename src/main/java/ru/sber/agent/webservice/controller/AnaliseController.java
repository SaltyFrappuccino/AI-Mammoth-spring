package ru.sber.agent.webservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.sber.agent.webservice.dto.*;
import ru.sber.agent.webservice.integration.BBDataSource;
import ru.sber.agent.webservice.integration.ConfluenceDataSource;
import ru.sber.agent.webservice.integration.IntegrationException;
import ru.sber.agent.webservice.integration.JiraDataSource;
import ru.sber.agent.webservice.service.AnaliseService;
import ru.sber.agent.webservice.service.Dictionary;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@RestController
public class AnaliseController {


    @Autowired
    private AnaliseService analiseService;
    @PostMapping("/analise")
    public ResponseEntity<AnaliseResults> analise(@RequestBody ArtifactLink artifactLink) {
    return analiseService.analise(artifactLink);
    }


}
