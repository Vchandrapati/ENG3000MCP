package Src;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class CCPClient {
    private enum Status {
        STOPC, STOPO, FSLOWC, FFASTC, RSLOWC, ERR
    }

    DatagramSocket socket;
    private volatile Boolean listen;
    private InetAddress sendAddress;
    private Integer sendPort;
    private String myID;
    private boolean living;
    private Long CCINStartTime;
    private Text txt;
    private Integer sequenceNum;
    private Status curStat;
    private boolean expectingAKST = false;


    // Creates a client on specified port and send to specified address
    public CCPClient(Integer port, InetAddress addLoc, Integer snedPort, String id, Text txt) {

        try {
            this.txt = txt;
            living = true;
            myID = id;
            sendPort = snedPort;
            sendAddress = addLoc;
            socket = new DatagramSocket(port);
            sequenceNum = 10;
            curStat = Status.STOPC;

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
                        // System.out.println(myID + " Recieved msg: " + message);
                    }

                    String[] temp = message.split(",");
                    String[] c = temp[0].split(":");
                    String[] s = temp[1].split(":");

                    // If the message is an AKIN msg will get the ID
                    if (c[1].equals("\"AKIN\"")) {
                        String[] temp2 = temp[2].split(":");
                        String[] temp3 = temp2[1].split("\"");
                        myID = temp3[1];

                        txt.addtext("Time taken for " + myID + ": "
                                + (System.currentTimeMillis() - CCINStartTime));
                    }

                    // If the message is a STAT msg will respond with a stat
                    if (c[1].equals("\"STRQ\"")) {
                        this.sendStatMsg();
                    }

                    if (c[1].equals("\"EXEC\"")) {
                        stringToStatus(s[1].split("\"")[1]);
                        System.out.println(s[1]);
                        this.sendAKEX();

                        this.sendStatMsg();
                        expectingAKST = true;
                    }


                    if (c[1].equals("\"AKST\"")) {
                        System.out.println(expectingAKST);
                        expectingAKST = false;

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
        byte[] buffer = ("{\"client_type\":\"CCP\", \"message\":\"CCIN\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\"}").getBytes();
        sendMsg(buffer);
        CCINStartTime = System.currentTimeMillis();
    }

    // Sends a stat message
    public void sendStatMsg() {
        byte[] buffer = ("{\"client_type\":\"CCP\", \"message\":\"STAT\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\", \"status\":\"" + curStat
                + "\"}").getBytes();
        sendMsg(buffer);
    }

    public void sendAKEX() {
        byte[] buffer = ("{\"client_type\":\"CCP\", \"message\":\"AKEX\", \"client_id\":\"" + myID
                + "\", \"sequence_number\":\"" + sequenceNum + "\"}").getBytes();
        sendMsg(buffer);
    }

    public void sendMsg(byte[] buffer) {
        if (!living) {
            return;
        }

        try {
            DatagramPacket sendPacket =
                    new DatagramPacket(buffer, buffer.length, sendAddress, sendPort);
            socket.send(sendPacket);
            // System.out.println(myID + " Sent msg " + new String(buffer));
            sequenceNum++;
        } catch (Exception e) {
            System.out.println("Failed to send packet");
        }

    }

    public void setLivingStatus(boolean status) {
        living = status;
    }

    public void stringToStatus(String status) {
        switch (status) {
            case "STOPC":
                this.curStat = Status.STOPC;
                break;
            case "STOPO":
                this.curStat = Status.STOPO;
                break;
            case "FSLOWC":
                this.curStat = Status.FSLOWC;
                break;
            case "FFASTC":
                this.curStat = Status.FFASTC;
                break;
            case "RSLOWC":
                this.curStat = Status.RSLOWC;
                break;
            case "ERR":
                this.curStat = Status.ERR;
                break;
            default:
                System.out.println("ERROR in string to status");
                break;
        }
    }
}