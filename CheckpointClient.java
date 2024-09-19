
import java.io.IOException;
import java.net.*;

public class CheckpointClient {

    DatagramSocket socket;
    volatile Boolean listen = false;
    private InetAddress sendAddress;
    private Integer sendPort;
    private String myID;

    // Creates a client on specified port and send to specified address
    public CheckpointClient(Integer port, InetAddress addLoc, Integer snedPort, String ID) {

        try {
            myID = ID;
            sendPort = snedPort;
            sendAddress = addLoc;
            socket = new DatagramSocket(port);

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

                    String message = new String(recievePacket.getData(), 0, recievePacket.getLength(), "UTF-8");
                    if (!message.isEmpty()) {
                        message = message.replaceAll("[^\\x20-\\x7E]", " ");
                        // Print every message received
                        System.out.println(myID + " Recieved msg: " + message);
                    }

                    String[] temp = message.split(",");
                    String[] c = temp[0].split(":");

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
    public void sendInitialiseConnectionMsg(Integer num) {
        byte[] buffer = ("{\"client_type\":\"checkpoint\", \"message\":\"CHIN\", \"client_id\":\"" + myID
                + "\", \"timestamp\":\"2019-09-07T15:50+00Z\", \"location\":\""
                + num + "\"}")
                .getBytes();
        sendMsg(buffer);
    }

    // Sends a stat message
    public void sendStatMsg() {
        byte[] buffer = ("{\"client_type\":\"checkpoint\", \"message\":\"STAT\", \"client_id\":\"" + myID
                + "\", \"timestamp\":\"2019-09-07T15:50+00Z\", \"status\":\"ON\"}").getBytes();
        sendMsg(buffer);
    }

    // Sends a stat message
    public void sendTRIPMsg() {
        byte[] buffer = ("{\"client_type\":\"checkpoint\", \"message\":\"TRIP\", \"client_id\":\"" + myID
                + "\", \"timestamp\":\"2019-09-07T15:50+00Z\", \"status\":\"ERR\"}").getBytes();
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
