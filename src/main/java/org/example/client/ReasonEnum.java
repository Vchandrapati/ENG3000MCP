package org.example.client;

// reasons why a client or the system could go into emergency mode which will fix each
@SuppressWarnings("unused")
public enum ReasonEnum {
    COLLISION, // if a BR detects a collision
    WRONGSTATUS, //Client returned a stat with an inconsistent status
    NOSTAT, // if a client has not returned their stat message
    INVALCONNECT, // if a clients connects while not in waiting state
    INCORTRIP, // if the system detects that this trip is wrong or inconsistent
    CLIENTERR, // if the client reports an error
    WRONGMESSAGE, // if the client sends the wrong message
    MAPTIMEOUT, // blade runner failed to map in time
    TODISCONNECT, //if the client is to disconnect, (given by the command handler)
}
