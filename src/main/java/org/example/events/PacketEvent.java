package org.example.events;

import java.net.DatagramPacket;

public class PacketEvent implements Event {
    private final DatagramPacket packet;

    public PacketEvent(final DatagramPacket packet) {
        this.packet = packet;
    }

    public DatagramPacket getPacket() {
        return packet;
    }
}
