import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class Utilities {

  /**
   * Waits for a BufferedReader to be ready or times out after the specified time.
   *
   * @param receiver The reader to wait on.
   * @param timeout The amount of time to wait before throwing a SocketTimeoutException.
   * @throws SocketTimeoutException When the reader has not become ready before the timeout.
   */
  public static void waitOrTimeout(BufferedReader receiver, int timeout) throws SocketTimeoutException {
    try {
      long timestamp = System.currentTimeMillis();
      while (!receiver.ready()) {
        long time = System.currentTimeMillis();
        if (time - timestamp >= timeout) {
          throw new SocketTimeoutException("Timeout occurred");
        }
      }
    }catch (SocketTimeoutException e) {
      // Timeout, connect to new server and try again
      throw new SocketTimeoutException("Timeout occurred");
    } catch (IOException e) {
      System.out.println("A fatal error occurred.");
      System.exit(-1);
    }
  }

}
