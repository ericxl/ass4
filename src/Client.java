import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
  private static final int TIMEOUT = 1500;

  private static final List<ServerInformation> servers = new ArrayList<>();

  private static int currentServerNumber = 0;
  private static Socket currentConnection;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    if (args.length != 1) {
      System.out.println("Input needs cfg file");
      System.exit(-1);
    }

    try {
      String file = args[0];
      sc = new Scanner(new FileReader(file));
    } catch (IOException e) {
    }

    int numServers = Integer.parseInt(sc.nextLine());

    for (int i = 0; i < numServers; i++) {
      String server = sc.nextLine();
      String[] info = server.split(":");

      try {
        InetAddress address = InetAddress.getByName(info[0]);
        int port = Integer.parseInt(info[1]);

        servers.add(new ServerInformation(address, port));
      } catch (UnknownHostException e) {
        System.out.println("An error occurred adding server: " + server);
        System.out.println("Ignoring server and continuing...");
      }
    }
    sc.close();

    sc = new Scanner(System.in);
    try {
      connectToNewServer(false);

      while (sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        switch (tokens[0]) {
          case "purchase":
          case "cancel":
          case "search":
          case "list":
            performCommand(cmd);
            break;

          default:
            System.out.println("ERROR: No such command");
        }
      }
      currentConnection.close();
    } catch (IOException e) {
      System.err.println("ERROR: Unknown error occurred: " + e);
    }
  }

  private static void performCommand(String message) {
    boolean complete = false;
    StringBuilder reply = new StringBuilder();

    while (!complete) {
      try {
        PrintWriter writer = new PrintWriter(currentConnection.getOutputStream());
        BufferedReader receiver = new BufferedReader(new InputStreamReader(currentConnection.getInputStream()));

        writer.println(message);
        writer.flush();

        boolean waiting = true;
        String line = null;
        while (waiting) {
          waitOrTimeout(receiver, TIMEOUT);

          line = receiver.readLine();
          if (!line.equals("working")) {
            waiting = false;
          }
        }

        while (line != null) {
          if (line.equals("done")) {
            complete = true;

            reply.deleteCharAt(reply.lastIndexOf("\n"));
            break;
          } else {
            reply.append(line);
            reply.append("\n");
          }

          line = receiver.readLine();
        }
      } catch (SocketTimeoutException e) {
        System.out.println("TIMEOUT.");
        connectToNewServer(true);
      } catch (IOException e) {
        System.out.println("Error");
        System.exit(-1);
      }
    }

    System.out.println(reply);
  }
  private static void connectToNewServer(boolean oldServerDead) {
    currentConnection = null;

    if (oldServerDead) {
      servers.remove(currentServerNumber);
    }

    for (int i = 0; i < servers.size(); i++) {
      ServerInformation information = servers.get(i);

      try {
        currentConnection = new Socket(information.getAddress(), information.getPort());
        PrintWriter protocol = new PrintWriter(currentConnection.getOutputStream());
        protocol.println("yes");
        protocol.flush();
        currentServerNumber = i;
        return;
      } catch (IOException e) {
        System.out.println("Server " + information + " is unavailable.");
      }
    }

    if (currentConnection == null) {
      System.out.println("Unable to connect to any servers.");
      System.out.println("Ending execution...");

      System.exit(-1);
    }
  }

  private static class ServerInformation {
    private final InetAddress address;
    private final int port;

    ServerInformation(InetAddress address, int port) {
      this.address = address;
      this.port = port;
    }

    InetAddress getAddress() {
      return address;
    }

    int getPort() {
      return port;
    }

    public String toString() {
      return address.getHostName() + ":" + port;
    }
  }
    public static void waitOrTimeout(BufferedReader receiver, int timeout) throws SocketTimeoutException {
      try {
        long timestamp = System.currentTimeMillis();
        while (!receiver.ready()) {
          long time = System.currentTimeMillis();
          if (time - timestamp >= timeout) {
            throw new SocketTimeoutException("Timeout occurred");
          }
        }
      } catch (SocketTimeoutException e) {
        // Timeout, connect to new server and try again
        throw new SocketTimeoutException("Timeout occurred");
      } catch (IOException e) {
        System.out.println("A fatal error occurred.");
        System.exit(-1);
      }
    }
}