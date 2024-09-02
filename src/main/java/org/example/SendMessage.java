package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessage {
    @JsonProperty("client_type")
    private String clientType;

    private String message;

    @JsonProperty("client_id")
    private String clientID;

    private Long timestamp;

    private String status;

    @JsonProperty("door_direction")
    private String doorDirection;

    private String action;

    // Constructors, Getters, and Setters
    public SendMessage() {
    }

    public SendMessage(String clientType, String message, String clientID, Long timestamp, String status,
            String doorDirection, String action) {
        this.clientType = clientType;
        this.message = message;
        this.clientID = clientID;
        this.timestamp = timestamp;
        this.status = status;
        this.doorDirection = doorDirection;
        this.action = action;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDoorDirection() {
        return doorDirection;
    }

    public void setDoorDirection(String doorDirection) {
        this.doorDirection = doorDirection;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
