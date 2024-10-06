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
    @JsonProperty("sequence_number")
    public int sequenceNumber;
    public String action;
}
