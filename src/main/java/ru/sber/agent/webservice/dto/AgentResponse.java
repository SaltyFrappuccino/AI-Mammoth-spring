package ru.sber.agent.webservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgentResponse {

    @JsonProperty("final_report")
    private String finalReport;

    private Integer bugs;
}
