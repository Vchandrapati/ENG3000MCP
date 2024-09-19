package Src;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    Integer port = 6666;
    InetAddress add = null;
    Integer CCPglobal = 0;
    Integer Checkglobal = 0;

    Integer numCCPClinets;
    Integer numcheckClinets;

    ArrayList<CCPClient> CCPclients = new ArrayList<>();
    ArrayList<CheckpointClient> Checkclients = new ArrayList<>();

    Scanner s = new Scanner(System.in);

    public void Run() {
        System.out.print("How many CCP clients: ");
        numCCPClinets = s.nextInt();

        System.out.print("How many checkpoints clients: ");
        numcheckClinets = s.nextInt();

        Text t = new Text(this);
        t.display();

        try {

            add = InetAddress.getByName("localhost");
            for (int i = 0; i < numCCPClinets; i++) {
                // Starts every CCP off messageing to this adddress
                String ID = "";

                if (i < 9) {
                    ID = "BR0" + (i + 1);
                } else {
                    ID = "BR" + (i + 1);
                }

                CCPclients.add(new CCPClient(5001 + CCPglobal, add, port, ID));
                CCPglobal++;
            }

            for (int i = 0; i < numcheckClinets; i++) {
                // Starts every client off messageing to this adddress
                String ID = "";

                if (i < 9) {
                    ID = "CP0" + (i + 1);
                } else {
                    ID = "CP" + (i + 1);
                }
                Checkclients.add(new CheckpointClient(3001 + Checkglobal, add, port, ID));
                Checkglobal++;
            }

        } catch (Exception e) {
            System.out.println("should never be here");
        }

        // Starts by having every client send a fake connection msg
        for (int i = 0; i < numCCPClinets; i++) {
            CCPclients.get(i).sendInitialiseConnectionMsg();
        }

        // Starts by having every client send a fake connection msg
        for (int i = 0; i < numcheckClinets; i++) {
            Checkclients.get(i).sendInitialiseConnectionMsg(i);
        }

    }

    public void Commands(String input) {
        String[] inputs = input.split(" ");
        if (inputs.length == 2 && inputs[0].equals("trip")) {
            try {
                Checkclients.get(Integer.parseInt(inputs[1])).sendTRIPMsg();
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
            CCPclients.add(new CCPClient(5001 + CCPglobal, add, port, ID));
            CCPclients.get(CCPclients.size() - 1).sendInitialiseConnectionMsg();
            CCPglobal++;
            numCCPClinets++;
        }

        if (inputs.length == 1 && inputs[0].equals("cp")) {
            String ID = "";
            if (Checkglobal < 9) {
                ID = "CP0" + (Checkglobal + 1);
            } else {
                ID = "CP" + (Checkglobal + 1);
            }
            Checkclients.add(new CheckpointClient(3001 + Checkglobal, add, port, ID));
            Checkclients.get(Checkclients.size() - 1).sendInitialiseConnectionMsg(Checkclients.size() - 1);
            Checkglobal++;
            numcheckClinets++;
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
