package org.example;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class BladeRunnerClient extends Client<MessageEnums.CCPStatus, MessageEnums.CCPAction> {
    private final AtomicInteger zone = new AtomicInteger();
    private volatile boolean isCurrentlyMapped;
    private volatile boolean collision;

    public BladeRunnerClient(InetAddress clientAddress, int clientPort, String id,
            int sequenceNumber) {
        super(clientAddress, clientPort, id, sequenceNumber);
        // Everyone starts like this but maybe they dont is the thing
        this.updateStatus(MessageEnums.CCPStatus.STOPC);
        this.isCurrentlyMapped = false;
        this.type = "BR";
    }

    public Integer getZone() {
        return zone.get();
    }

    public void changeZone(int zone) {
        this.zone.set(zone);
        isCurrentlyMapped = true;
    }

    public void unmap() {
        isCurrentlyMapped = false;
    }

    public boolean isUnmapped() {
        return !isCurrentlyMapped;
    }

    public boolean collision(boolean hasCollide, Object o) {
        if (o != null) {
            collision = hasCollide;
        }
        return collision;
    }
}
