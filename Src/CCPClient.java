package Src;

import java.io.IOException;
import java.net.*;

public class CCPClient {

    DatagramSocket socket;
    private volatile Boolean listen;
    private InetAddress sendAddress;
    private Integer sendPort;
    private String myID;

    // Creates a client on specified port and send to specified address
    public CCPClient(Integer port, InetAddress addLoc, Integer snedPort, String id) {

        try {
            myID = id;
            sendPort = snedPort;
            sendAddress = addLoc;
            socket = new DatagramSocket(port);

            this.beginListen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Starts listening
    private void beginListen() throws IOException {
        listen = true;
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

                    // If the message is an AKIN msg will get the ID
                    if (c[1].equals("\"AKIN\"")) {
                        String[] temp2 = temp[3].split(":");
                        String[] temp3 = temp2[1].split("\"");
                        myID = temp3[1];

                    }

                    // If the message is a STAT msg will respond with a stat
                    if (c[1].equals("\"STAT\"")) {
                        this.sendStatMsg();
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
    public void sendInitialiseConnectionMsg() {
        byte[] buffer = ("{\"client_type\":\"ccp\", \"message\":\"CCIN\", \"client_id\":\"" + myID
                + "\", \"timestamp\":\"2019-09-07T15:50+00Z\"}")
                .getBytes();
        sendMsg(buffer);
    }

    // Sends a stat message
    public void sendStatMsg() {
        byte[] buffer = ("{\"client_type\":\"ccp\", \"message\":\"STAT\", \"client_id\":\"" + myID
                + "\", \"timestamp\":\"2019-09-07T15:50+00Z\", \"status\":\"ON\"}").getBytes();
        sendMsg(buffer);
    }

    public void sendMsg(byte[] buffer) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, sendAddress, sendPort);
            socket.send(sendPacket);
            System.out.println(myID + " Sent msg " + new String(buffer));
        } catch (Exception e) {
            System.out.println("Failed to send packet");
        }

    }

}
