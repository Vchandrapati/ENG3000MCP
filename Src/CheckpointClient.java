package Src;

import java.io.IOException;
import java.net.*;

public class CheckpointClient {

    public enum Status {
        ON, OFF, ERR
    }

    DatagramSocket socket;
    volatile Boolean listen = false;
    private InetAddress sendAddress;
    private Integer sendPort;
    String myID;
    private boolean living;
    private Integer sequenceNum;
    private Status curStat;
    private boolean tripped = false;

    // Creates a client on specified port and send to specified address
    public CheckpointClient(Integer port, InetAddress addLoc, Integer snedPort, String ID) {

        try {
            living = true;
            myID = ID;
            sendPort = snedPort;
            sendAddress = addLoc;
            socket = new DatagramSocket(port);

            curStat = Status.OFF;
            sequenceNum = 10;

            listen = true;
            this.beginListen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Starts listening
    private void beginListen() {

        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (listen) {
                try {
                    DatagramPacket recievePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(recievePacket);

                    String message = new String(recievePacket.getData(), 0,
                            recievePacket.getLength(), "UTF-8");
                    if (!message.isEmpty()) {
                        message = message.replaceAll("[^\\x20-\\x7E]", " ");
                        // Print every message received
                        System.out.println(myID + " Recieved msg: " + message);
                    }

                    String[] temp = message.split(",");
                    String[] c = temp[0].split(":");

                    // If the message is a STAT msg will respond with a stat
                    if (c[1].equals("\"STRQ\"")) {
                        this.sendStatMsg();
                    }

                    if (c[1].equals("\"EXEC\"")) {
                        this.sendAKEXMsg();
                    }

                } catch (IOException e) {
                    System.out.println(myID + " Failed to get message or unpack");
                }
            }
        }).start();

    }

    // Call to stop listening
    public void stopListen() {
        listen = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    // Sends an initialise connection message
    public void sendInitialiseConnectionMsg(Integer num) {
        byte[] buffer = ("{\"client_type\":\"CPC\", \"message\":\"CPIN\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\"}").getBytes();
        sendMsg(buffer);
    }

    // Sends a stat message
    public void sendStatMsg() {
        byte[] buffer = ("{\"client_type\":\"CPC\", \"message\":\"STAT\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\", \"status\":\""
                + curStat.toString() + "\"}").getBytes();
        sendMsg(buffer);
    }

    // Sends a AKEX message
    public void sendAKEXMsg() {
        byte[] buffer = ("{\"client_type\":\"CPC\", \"message\":\"AKEX\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\"}").getBytes();
        sendMsg(buffer);
    }

    // Sends a stat message
    public void sendTRIPMsg() {
        if(!tripped) {
            tripped = true;
            this.curStat = Status.ON;
        }
        else {
            tripped = false;
            this.curStat = Status.OFF;
        }
        byte[] buffer = ("{\"client_type\":\"CPC\", \"message\":\"TRIP\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\", \"status\":\"" + curStat.toString()
                + "\"}").getBytes();
        sendMsg(buffer);
    }

    public void sendMsg(byte[] buffer) {
        try {
            if (!living) {
                return;
            }

            DatagramPacket sendPacket =
                    new DatagramPacket(buffer, buffer.length, sendAddress, sendPort);
            socket.send(sendPacket);
            System.out.println(myID + " Sent msg " + new String(buffer));
            sequenceNum++;
        } catch (Exception e) {
            System.out.println("Failed to send packet");
        }

    }

    public void setLivingStatus(boolean status) {
        living = status;
    }

}
