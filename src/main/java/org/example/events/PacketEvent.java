package org.example.events;

import java.net.DatagramPacket;

public record PacketEvent(DatagramPacket packet) implements Event {
}
