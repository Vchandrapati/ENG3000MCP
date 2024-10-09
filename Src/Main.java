package Src;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.SwingWorker.StateValue;
import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors

public class Main {

    Integer port = 2000;
    InetAddress add = null;
    Integer CCPglobal = 0;

    Integer numCCPClinets = 0;
    Integer numcheckClinets = 0;
    Integer numSTNClients = 0;

    ArrayList<String> inputs = new ArrayList<>();

    ArrayList<CCPClient> CCPclients = new ArrayList<>();
    ArrayList<CheckpointClient> Checkclients = new ArrayList<>();
    ArrayList<STNClients> STNClients = new ArrayList<>();

    Scanner s = new Scanner(System.in);

    Text t;

    public void Run() {
        System.out.print("How many CCP clients: ");
        numCCPClinets = s.nextInt();

        t = new Text(this);
        t.display();

        try {

            File file = new File("SRC/inputs.txt");
            Scanner s = new Scanner(file);

            while (s.hasNextLine()) {
                inputs.add(s.nextLine());
            }

            add = InetAddress.getByName("localhost");
            for (int i = 0; i < numCCPClinets; i++) {
                // Starts every CCP off messageing to this adddress
                String ID = "";

                if (i < 9) {
                    ID = "BR0" + (i + 1);
                } else {
                    ID = "BR" + (i + 1);
                }

                CCPclients.add(new CCPClient(5001 + CCPglobal, add, port, ID, t));
                CCPglobal++;
            }

            for (int i = 0; i < inputs.size(); i++) {
                // Starts every client off messageing to this adddress
                String[] ID = inputs.get(i).split(":");
                Integer num = Integer.parseInt(ID[1]);
                String fullID = "";

                if (num <= 9) {
                    fullID = ID[0] + "0" + (num);
                } else {
                    fullID = ID[0] + (num);
                }

                if (ID[0].equals("ST")) {
                    numSTNClients++;
                    STNClients.add(new STNClients(4000 + num, add, port, fullID));
                }
                if (ID[0].equals("CP")) {
                    numcheckClinets++;
                    Checkclients.add(new CheckpointClient(3000 + num, add, port, fullID));
                }
            }


            s.close();
        } catch (Exception e) {
            System.out.println("should never be here");
            e.printStackTrace();
        }

        // Starts by having every client send a fake connection msg
        for (int i = 0; i < numCCPClinets; i++) {
            CCPclients.get(i).sendInitialiseConnectionMsg();
        }

        // Starts by having every client send a fake connection msg
        for (int i = 0; i < numcheckClinets; i++) {
            Checkclients.get(i).sendInitialiseConnectionMsg(i + 1);
        }

        // Starts by having every client send a fake connection msg
        for (int i = 0; i < numSTNClients; i++) {
            STNClients.get(i).sendInitialiseConnectionMsg(i + 1);
        }

    }

    public void Commands(String input) {
        String[] inputs = input.split(" ");
        if (inputs.length == 2 && inputs[0].equals("trip")) {
            for (CheckpointClient cp : Checkclients) {
                if (cp.myID.contains("CP0" + inputs[1]) || cp.myID.contains("CP" + inputs[1])) {
                    cp.sendTRIPMsg();
                    break;
                }
            }
            for (STNClients st : STNClients) {
                if (st.myID.contains("ST0" + inputs[1]) || st.myID.contains("ST" + inputs[1])) {
                    st.sendTRIPMsg();
                    break;
                }
            }
        }

        if (inputs.length == 2 && inputs[0].equals("untrip")) {
            try {
                Checkclients.get(Integer.parseInt(inputs[1]) - 1).sendTRIPMsg();
            } catch (Exception e) {
                System.out.println("NO! bad user");
            }
        }

        if (inputs.length == 1 && inputs[0].equals("ccp")) {
            String ID = "";
            if (CCPglobal < 9) {
                ID = "BR0" + (CCPglobal + 1);
            } else {
                ID = "BR" + (CCPglobal + 1);
            }
            CCPclients.add(new CCPClient(5001 + CCPglobal, add, port, ID, t));
            CCPclients.get(CCPclients.size() - 1).sendInitialiseConnectionMsg();
            CCPglobal++;
            numCCPClinets++;
        }

        if (inputs.length == 1 && inputs[0].equals("ccpccp")) {
            String ID = "";
            if (CCPglobal < 9) {
                ID = "BR0" + (CCPglobal + 1);
            } else {
                ID = "BR" + (CCPglobal + 1);
            }
            CCPclients.add(new CCPClient(5001 + CCPglobal, add, port, ID, t));
            CCPclients.get(CCPclients.size() - 1).sendInitialiseConnectionMsg();
            CCPglobal++;
            numCCPClinets++;

            if (CCPglobal < 9) {
                ID = "BR0" + (CCPglobal + 1);
            } else {
                ID = "BR" + (CCPglobal + 1);
            }
            CCPclients.add(new CCPClient(5001 + CCPglobal, add, port, ID, t));
            CCPclients.get(CCPclients.size() - 1).sendInitialiseConnectionMsg();
            CCPglobal++;
            numCCPClinets++;
        }

        // if (inputs.length == 1 && inputs[0].equals("cp")) {
        // String ID = "";
        // if (Checkglobal < 9) {
        // ID = "CP0" + (Checkglobal + 1);
        // } else {
        // ID = "CP" + (Checkglobal + 1);
        // }
        // Checkclients.add(new CheckpointClient(3001 + Checkglobal, add, port, ID));
        // Checkclients.get(Checkclients.size() - 1)
        // .sendInitialiseConnectionMsg(Checkclients.size());
        // Checkglobal++;
        // numcheckClinets++;
        // }

        // if (inputs.length == 2 && inputs[0].equals("cp") && inputs[1].matches("[1-9]|10")) {
        // String ID = inputs[1];
        // if (Integer.parseInt(ID) != 10) {
        // ID = "CP0" + ID;
        // } else {
        // ID = "CP" + ID;
        // }

        // Checkclients.add(new CheckpointClient(3001 + Checkglobal, add, port, ID));
        // Checkclients.get(Checkclients.size() - 1)
        // .sendInitialiseConnectionMsg(Integer.parseInt(inputs[1]));
        // Checkglobal++;
        // numcheckClinets++;
        // }

        if (inputs.length == 3 && (inputs[0].equals("kill") || inputs[0].equals("rez"))) {
            boolean status = false;
            if (inputs[0].equals("rez")) {
                status = true;
            }
            try {
                switch (inputs[1]) {
                    case "ccp":
                        CCPclients.get(Integer.parseInt(inputs[2]) - 1).setLivingStatus(status);
                        break;
                    case "cp":
                        Checkclients.get(Integer.parseInt(inputs[2]) - 1).setLivingStatus(status);
                        break;
                }
            } catch (Exception e) {
                System.out.println("NO! bad thomas");
            }
        }

        if (inputs.length == 3 && (inputs[0].equals("ccp"))) {
            try {
                CCPClient ccp = CCPclients.get(Integer.parseInt(inputs[1]) - 1);
                ccp.stringToStatus(inputs[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (input.equals("q")) {

            for (int i = 0; i < numCCPClinets; i++) {
                CCPclients.get(i).stopListen();
            }

            for (int i = 0; i < numcheckClinets; i++) {
                Checkclients.get(i).stopListen();
            }

            s.close();
            System.exit(0);
        }
    }
}
