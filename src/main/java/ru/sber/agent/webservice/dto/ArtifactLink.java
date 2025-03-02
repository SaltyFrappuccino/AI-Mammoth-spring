package ru.sber.agent.webservice.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArtifactLink {
    @NotNull(message = "confluence link is required")
    private String confLink;
    @NotNull(message = "Bitbucket PR link is required")
    private String bbLink;
    @NotNull(message = "Jira task link is required")
    private String jiraLink;

    private List<String> testCases;

}
