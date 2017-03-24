import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    static String[] hostAddresses;
    static int[] tcpPorts;
    private static final int TIMEOUT = 100;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int numServer = sc.nextInt();

        hostAddresses = new String[numServer];
        tcpPorts = new int[numServer];
        for (int i = 0; i < numServer; i++) {
            String addTokens[] = sc.next().split(":");
            hostAddresses[i] = addTokens[0];
            tcpPorts[i] = Integer.parseInt(addTokens[1]);
        }

        while (sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");

            String response;
            if (tokens[0].equals("purchase")) {
                String purchase = tokens[0];
                String userName = tokens[1];
                String productName = tokens[2];
                String quantity = tokens[3];
                String dataToSend = purchase + " " + userName + " " + productName + " " + quantity;
                response = sendData(dataToSend);
            } else if (tokens[0].equals("cancel")) {
                String cancel = tokens[0];
                String orderId = tokens[1];
                String dataToSend = cancel + " " + orderId;
                response = sendData(dataToSend);
            } else if (tokens[0].equals("search")) {
                String search = tokens[0];
                String userName = tokens[1];
                String dataToSend = search + " " + userName;
                response = sendData(dataToSend);
            } else if (tokens[0].equals("list")) {
                String list = tokens[0];
                String dataToSend = list;
                response = sendData(dataToSend);
            } else {
                response = "ERROR: No such command";
            }
            System.out.println(response);

        }
    }

    public static String sendData(String message){
        int i = 0;
        Socket s = new Socket();
        while(true){
            String strAddress = hostAddresses[i];
            int port = tcpPorts[i];
            try {
                s.connect(new InetSocketAddress(strAddress,port));
                s.setSoTimeout(TIMEOUT);
                DataOutputStream stdout = new DataOutputStream(s.getOutputStream());
                DataInputStream stdin = new DataInputStream(s.getInputStream());
                stdout.writeUTF(message);
                stdout.flush();
                s.close();
                return stdin.readUTF();
            } catch (IOException e1) {
                i = i == hostAddresses.length - 1 ? 0: i + 1;
            }
        }
    }

}
