import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;

public class App {

    public static void main(String args[]) {
        Scanner s = new Scanner(System.in);
        System.out.print("How many CCP clients: ");
        Integer numCCPClinets = s.nextInt();

        System.out.print("How many checkpoints clients: ");
        Integer numcheckClinets = s.nextInt();

        ArrayList<CCPClient> CCPclients = new ArrayList<>();
        ArrayList<CheckpointClient> Checkclients = new ArrayList<>();

        Integer port = 6666;
        InetAddress add = null;
        Integer global = 0;
        try {
                
            add = InetAddress.getByName("10.20.30.193");
            for (int i = 0; i < numCCPClinets; i++) {
                // Starts every CCP off messageing to this adddress
                String ID = "";
                
                if(i < 10){
                    ID = "BR0" + (i + 1);
                }else{
                    ID = "BR" + (i + 1);
                }

                CCPclients.add(new CCPClient(5001 + global, add, port, ID));
                global++;
            }

            for (int i = 0; i < numcheckClinets; i++) {
                // Starts every client off messageing to this adddress
                String ID = "";
                
                if(i < 10){
                    ID = "CP0" + i + 1;
                }else{
                    ID = "CP" + i + 1;
                }
                Checkclients.add(new CheckpointClient(3001 + global, add, port, ID));
                global++;
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

        Boolean running = true;
        while (running) {
            String input = s.nextLine();

            String[] inputs = input.split(" ");
            if (inputs.length == 2 && inputs[0].equals("trip")) {
                try {
                    Checkclients.get(Integer.parseInt(inputs[1])).sendTRIPMsg();
                } catch (Exception e) {
                    System.out.println("NO! bad user");
                }

            }

            if(inputs.length == 1 && inputs[0].equals("ccp")){
                String ID = "";
                if(global < 10){
                    ID = "BR0" + (global + 1);
                }else{
                    ID = "BR" + (global + 1);
                }
                CCPclients.add(new CCPClient(5001 + global, add, port, ID));
                CCPclients.get(CCPclients.size() - 1).sendInitialiseConnectionMsg();
                global++;
            }

            if (input.equals("q")) {
                System.out.println("Exiting...");
                running = false;
            }
        }

        for (int i = 0; i < numCCPClinets; i++) {
            CCPclients.get(i).stopListen();
        }

        for (int i = 0; i < numcheckClinets; i++) {
            Checkclients.get(i).stopListen();
        }
        s.close();
    }

}
