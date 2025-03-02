package ru.sber.agent.webservice.integration;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface SberWorksDataSource {

    String LINK_REGEX = "^(https?|ftp):\\/\\/[^\s/$.?#].[^\s]*$";
    String getData(String link) throws IntegrationException, ExecutionException, InterruptedException;

    default Boolean checkLink(String link) {
        Pattern pattern = Pattern.compile(LINK_REGEX);
        Matcher matcher = pattern.matcher(link);
        return matcher.matches();
    }
}
