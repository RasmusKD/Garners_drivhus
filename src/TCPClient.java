import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) {
        final String hostname = "localhost"; // Serverens adresse
        final int port = 5000;               // Serverens port

        try (Socket socket = new Socket(hostname, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            String sensorData;
            System.out.println("Forbindelse til server oprettet. Indtast 'exit' for at afslutte.");

            // Fortsæt med at sende data, indtil brugeren skriver "exit"
            while (true) {
                // Læs data fra brugeren
                System.out.println("Indtast sensortype og værdi (f.eks. 'luftfugtighed:71.5','temperatur:9' el.'jordfugtighed:15' ): ");
                sensorData = scanner.nextLine();

                // Tjek om brugeren ønsker at afslutte
                if ("exit".equalsIgnoreCase(sensorData)) {
                    System.out.println("Afslutter forbindelse til serveren...");
                    break;
                }

                // Validering af inputformat (f.eks. "luftfugtighed:71.0")
                if (!sensorData.matches("[a-zA-Z]+:[0-9]+(\\.[0-9]+)?")) {
                    System.out.println("Fejl: Ugyldigt format. Brug formatet 'sensortype:værdi' (f.eks. 'temperatur:25.5')");
                    continue;
                }

                // Send data til serveren
                writer.println(sensorData);

                // Modtag svar fra serveren
                String response = reader.readLine();
                System.out.println("Server respons: " + response);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
