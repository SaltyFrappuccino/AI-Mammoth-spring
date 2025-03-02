package ru.sber.agent.webservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnaliseResults {
    private Map<String, String> errors;
    private String analiseResult;
    private String analiseResultPercent;


    public void setError(String system, String error) {
        if(errors == null) {
            errors = new HashMap<>();
            errors.put(system, error);
        } else {
        errors.put(system, error);}
    }
}
