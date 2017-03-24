import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
  private static final int TIMEOUT = 1500;

  private static final List<ServerInfo> servers = new ArrayList<>();

  private static int currentServerNumber = 0;
  private static Socket currentConnection;

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    if (args.length != 1) {
      System.out.println("You need a cfg file for client");
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

        servers.add(new ServerInfo(address, port));
      } catch (UnknownHostException e) {
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
            System.out.println("Try again: No such command");
        }
      }
      currentConnection.close();
    } catch (IOException e) {
    }
  }

  private static void performCommand(String message) {
    boolean done = false;
    StringBuilder reply = new StringBuilder();

    while (!done) {
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
            done = true;

            reply.deleteCharAt(reply.lastIndexOf("\n"));
            break;
          } else {
            reply.append(line);
            reply.append("\n");
          }

          line = receiver.readLine();
        }
      } catch (SocketTimeoutException e) {
        connectToNewServer(true);
      } catch (IOException e) {
        System.exit(-1);
      }
    }

    System.out.println(reply);
  }
  private static void connectToNewServer(boolean serverDown) {
    currentConnection = null;

    if (serverDown) {
      servers.remove(currentServerNumber);
    }

    for (int i = 0; i < servers.size(); i++) {
      ServerInfo information = servers.get(i);

      try {
        currentConnection = new Socket(information.getAddress(), information.getPort());
        PrintWriter protocol = new PrintWriter(currentConnection.getOutputStream());
        protocol.println("hello");
        protocol.flush();
        currentServerNumber = i;
        return;
      } catch (IOException e) {
      }
    }

    if (currentConnection == null) {
      System.exit(-1);
    }
  }
    public static void waitOrTimeout(BufferedReader receiver, int timeout) throws SocketTimeoutException {
      try {
        long timestamp = System.currentTimeMillis();
        while (!receiver.ready()) {
          long time = System.currentTimeMillis();
          if (time - timestamp >= timeout) {
            throw new SocketTimeoutException();
          }
        }
      } catch (SocketTimeoutException e) {
        throw new SocketTimeoutException();
      } catch (IOException e) {
        System.out.println();
        System.exit(-1);
      }
    }

  private static class ServerInfo {
    private final InetAddress address;
    private final int port;

    ServerInfo(InetAddress address, int port) {
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
}