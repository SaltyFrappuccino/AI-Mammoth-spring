package ru.sber.agent.webservice.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.sber.agent.webservice.dto.confluence.Content;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ConfluenceDataSource implements SberWorksDataSource {

    @Value("${auth.token.confluence}")
    private String token;

    @Value("${connection.conf}")
    private String url;

    public static final String SYSTEM = "Confluence";
    private RestTemplate restTemplate = new RestTemplate();


    @Override
    public String getData(String link) throws IntegrationException {
        String pageId;
        if (checkLink(link)) {
            pageId = getPageId(link);
        } else {
            pageId = link;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        RequestEntity<Object> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(String.format("%s/rest/api/content/%s?expand=body.storage", url, pageId)));
        ResponseEntity<Content> response;
        try {
            response = restTemplate.exchange(requestEntity, Content.class);
        } catch (Exception e) {
            throw new IntegrationException(SYSTEM, e.getMessage());
        }


        if (response.getStatusCode().isError()) {
            throw new IntegrationException(SYSTEM, String.format("Ошибочный статус ответа %s", response.getStatusCode()));
        }
        if (response.getBody() == null)
            throw new IntegrationException(SYSTEM, "Пустое тело ответа");
        if (response.getBody().getBody().getStorage() == null)
            throw new IntegrationException(SYSTEM, "Не найдена тело страницы confluence");

        return extractPlainTextFromXml(wrapMainTag(response.getBody().getBody().getStorage().getValue()));
    }

    private String getPageId(String link) throws IntegrationException {
        Pattern pattern = Pattern.compile("pageId=(\\d+)");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            String pageId = matcher.group(1);
            log.info("ID страницы confluence: {}", pageId);
            return pageId;
        }
        throw new IntegrationException(SYSTEM, String.format("Не найдена страница для анализа в %s", link));
    }

    private String wrapMainTag(String xml) {
        return "<body>" + xml + "</body>";
    }

    public String extractPlainTextFromXml(String xml) throws IntegrationException {
        try {
            String normalizedXml = xml.replaceAll("&[a-zA-Z0-9#]+;", "");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(normalizedXml)));

            NodeList nodeList = document.getElementsByTagName("*");
            StringBuilder plainText = new StringBuilder();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    plainText.append(element.getTextContent());
                    plainText.append("\n");
                }
            }

            return plainText.toString();
        } catch (Exception e) {
            throw new IntegrationException(ConfluenceDataSource.SYSTEM, "Ошибка при извлечении текста из XML", e);
        }
    }
}
