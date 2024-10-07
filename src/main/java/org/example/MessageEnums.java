package org.example;

public class MessageEnums {

    // There are certain actions which can not be mapped 1 to 1 to a status
    // Maybe a hashmap implementation?
    public interface ActionToStatus<T> {
        T getStatus();
    }


    // Enum for CCP Actions
    public enum CCPAction implements ActionToStatus<CCPStatus> {
        STOPC, STOPO, FSLOWC, FFASTC, RSLOWC, DISCONNECT;

        @Override
        public CCPStatus getStatus() {
            switch (this) {
                case STOPC:
                    return CCPStatus.STOPC;
                case STOPO:
                    return CCPStatus.STOPO;
                case FSLOWC:
                    return CCPStatus.FSLOWC;
                case FFASTC:
                    return CCPStatus.FFASTC;
                case RSLOWC:
                    return CCPStatus.RSLOWC;
                default:
                    return CCPStatus.ERR;
            }
        }

    }

    // Enum for CCP Status
    public enum CCPStatus {
        STOPC, STOPO, FSLOWC, FFASTC, RSLOWC, ERR
    }

    // Enum for CPC Actions
    public enum CPCAction implements ActionToStatus<CPCStatus> {
        OFF, ON, BLINK;

        @Override
        public CPCStatus getStatus() {
            switch (this) {
                case OFF:
                    return CPCStatus.OFF;
                case ON:
                    return CPCStatus.ON;
                default:
                    return CPCStatus.ERR;
            }
        }
    }

    // Enum for CPC Status
    public enum CPCStatus {
        ON, OFF, ERR
    }

    // Enum for STC Actions
    public enum STCAction implements ActionToStatus<STCStatus> {
        OFF, ON, BLINK, OPEN, CLOSE;

        @Override
        public STCStatus getStatus() {
            switch (this) {
                case OFF:
                    return STCStatus.OFF;
                case ON:
                    return STCStatus.ON;
                case OPEN:
                    return STCStatus.ONOPEN;
                default:
                    return STCStatus.ERR;
            }
        }
    }

    // Enum for STC Status
    public enum STCStatus {
        ON, ONOPEN, OFF, ERR
    }

    // Enums for types of AK
    public enum AKType {
        AKIN, AKEX, AKST
    }

    public static <T> T convertActionToStatus(ActionToStatus<T> action) {
        return action.getStatus();
    }
}


