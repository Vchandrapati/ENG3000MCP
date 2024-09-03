package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessage {
    @JsonProperty("client_type")
    public String clientType;
    public String message;
    @JsonProperty("client_id")
    public String clientID;
    public Long timestamp;
    public String status;
    public String action;
}
