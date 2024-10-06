package org.example;

public class MessageEnums {
    // Enum for CCP Actions
    public enum CCPAction {
        STOPC, STOPO, FSLOWC, FFASTC, RSLOWC, DISCONNECT
    }

    // Enum for CCP Status
    public enum CCPStatus {
        STOPC, STOPO, FSLOWC, FFASTC, RSLOWC, ERR
    }

    // Enum for CPC Actions
    public enum CPCAction {
        OFF, ON, BLINK
    }

    // Enum for CPC Status
    public enum CPCStatus {
        ON, OFF, ERR
    }

    // Enum for STC Actions
    public enum STCAction {
        OFF, ON, BLINK, OPEN, CLOSE
    }

    // Enum for STC Status
    public enum STCStatus {
        ON, ONOPEN, OFF, ERR
    }

    //Enums for types of AK
    public enum AKType {
        AKIN, AKEX, AKST
    }
}

