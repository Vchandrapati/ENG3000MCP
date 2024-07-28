import java.net.*;
import java.io.*;


public class TestClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    double speed;

    public void startConnection(String ip, int port) throws Exception {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        print("Connected to " + ip + " : " + port);

        String r = sendMessage("trainInit");
        if(r.equals("ACK trainInit")) sendMessage("train,0,0,1,HELLO!");

        r = sendMessage("stationInit");
        if(r.equals("ACK stationInit")) sendMessage("station,1,HELLO!!!");

        while (true) {
            if (readMessage().equals("STATUS")) {

            }
        }
    }

    public String sendMessage(String msg) {
        try {
            out.print(msg+"\n");
            out.flush();
            String resp = in.readLine();
            System.out.println("Client sends " + msg);
            System.out.println("Client received " + resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readMessage() {
        try {
            if(in != null && in.ready()) return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void print(String msg) {
        System.out.println(msg);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        TestClient client = new TestClient();
        try {
            client.startConnection("localhost", 6666);
        } catch (Exception e) {
            System.exit(64);
            System.out.println("Could not connect :(");
        }
    }
}