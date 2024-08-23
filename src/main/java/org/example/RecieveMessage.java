package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecieveMessage {
    @JsonProperty("client_type")
    public String clientType;
    public String message;
    @JsonProperty("client_id")
    public String clientID;
    public Long timestamp;
    public String status;
    @JsonProperty("door_status")
    public String doorStatus;
    @JsonProperty("connection_info")
    public ConnectionInfo connectionInfo;

    public static class ConnectionInfo {
        private int port;
        private String ip;
    }
}
