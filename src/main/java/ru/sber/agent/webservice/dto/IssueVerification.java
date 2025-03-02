package ru.sber.agent.webservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class IssueVerification {
    private String issueId;
    private Integer numOfLinkedBugs;
    private Integer aiConvergance;
    private Timestamp createdAt;

}
