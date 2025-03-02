package ru.sber.agent.webservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentRequest {
    @JsonProperty("requirements")
    private String requirements;
    @JsonProperty("test_cases")
    private String testCases;
    @JsonProperty("code")
    private String code;
    @JsonProperty("documentation")
    private String documentation;
    @JsonProperty("semantic_db")
    private Map<String, String> semanticDb;
}
