package org.example.events;

import org.example.messages.ReceiveMessage;

import java.net.InetAddress;

public record ClientIntialiseEvent(ReceiveMessage receiveMessage, InetAddress address,
                                   int port) implements Event {
}
