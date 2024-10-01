package org.example;

// reasons why a client or the system could go into emergency mode which will fix each
public enum ReasonEnum {
    COLLISION, // if a BR detects a collision
    NOSTAT, // if a client has not returned their stat message
    INVALCONNECT, // if a clients connects while not in waiting state
    INCORTRIP, // if the system detects that this trip is wrong or inconsistent
    CLIENTERR, // if the client reports an error
    WRONGMESSAGE, // if the client sends the wrong message
    MAPTIMEOUT, // blade runner failed to map in time
    SYSTEMOVER, // if the system cannot keep up in processing packets
    INTERNAL, // if the MCP has had an error
}
