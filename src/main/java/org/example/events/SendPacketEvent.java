package org.example.events;

import java.net.InetAddress;

public record SendPacketEvent(InetAddress clientAddress, int clientPort, String id, String message,
                              String type) implements Event {
}
