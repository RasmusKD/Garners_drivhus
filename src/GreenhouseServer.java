import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class GreenhouseServer {
    private static final int PORT = 5000;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        // Opret en trådpool med et fast antal tråde
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Opret en server socket, der lytter på en bestemt port
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Lyt efter klientforbindelser i en evig løkke
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Håndter klienten ved hjælp af trådpool
                threadPool.submit(new ClientHandler(socket));
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
