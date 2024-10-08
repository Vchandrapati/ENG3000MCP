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
            return switch (this) {
                case STOPC -> CCPStatus.STOPC;
                case STOPO -> CCPStatus.STOPO;
                case FSLOWC -> CCPStatus.FSLOWC;
                case FFASTC -> CCPStatus.FFASTC;
                case RSLOWC -> CCPStatus.RSLOWC;
                case DISCONNECT -> null;
            };
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
            return switch (this) {
                case OFF -> CPCStatus.OFF;
                case ON -> CPCStatus.ON;
                case BLINK -> null;
            };
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
            return switch (this) {
                case OFF -> STCStatus.OFF;
                case ON -> STCStatus.ON;
                case OPEN -> STCStatus.ONOPEN;
                case BLINK, CLOSE -> null;
            };
        }
    }

    // Enum for STC Status
    public enum STCStatus {
        ON, ONOPEN, OFF, ERR
    }

    // Enums for types of AK
    public enum AKType {
        AKIN, AKEX, AKST, AKTR
    }

    public static <T> T convertActionToStatus(ActionToStatus<T> action) {
        return action.getStatus();
    }
}


