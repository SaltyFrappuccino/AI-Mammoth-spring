package ru.sber.agent.webservice.dto.confluence;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class Content {
    @JsonProperty
    private String id;
    @JsonProperty
    private String type;
    @JsonProperty
    private String status;
    @JsonProperty
    private String title;

    @JsonProperty
    private ContentBody body;


    @Data
    @NoArgsConstructor
    public static class ContentBody {
        @JsonProperty
        private Storage storage;

    }

    @Data
    @NoArgsConstructor
    public static class Storage {
        @JsonProperty
        private String value;

    }


}
