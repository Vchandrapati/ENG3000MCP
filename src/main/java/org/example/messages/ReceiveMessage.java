package org.example.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiveMessage {
    @JsonProperty("client_type")
    public String clientType;
    public String message;
    @JsonProperty("client_id")
    public String clientID;
    @JsonProperty("sequence_number")
    public int sequenceNumber;
    public String action;
    public String status;
}