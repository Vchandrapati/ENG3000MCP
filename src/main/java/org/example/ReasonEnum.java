package org.example;

//reasons why a client or the system could go into emergency mode
//emergency mode will fix each
public enum ReasonEnum {
    COLLISION, //if a BR detects a collision
    NOSTAT, //if a client has not returned their stat message
    INVALDCONNECT, //if a clients connects while not in waiting state
    STATDISCREP, //if a stat given by a client seems wrong
    INCORTRIP, //if the system detects that this trip is wrong or inconsistent
    CLIENTERR, //if the client reports an error
    WRONGMESSAGE, //if the client sends the wrong message
}
