import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.time.LocalDateTime;

class ClientHandler implements Runnable {
    private Socket socket;
    final double HIGH_TEMP_THRESHOLD = 30.0;
    final double LOW_TEMP_THRESHOLD = 10.0;
    final double HIGH_HUMIDITY_THRESHOLD = 70.0;
    final double LOW_HUMIDITY_THRESHOLD = 30.0;
    final double LOW_SOIL_MOISTURE_THRESHOLD = 20.0;
    final String LOG_FILE = "greenhouse_data.log";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Håndter kommunikationen med klienten
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {

            String text;
            // Læs beskeder fra klienten og håndter dem
            while ((text = reader.readLine()) != null) {
                System.out.println("Modtaget fra klient: " + text);
                // Antag at beskeder fra klienten er formateret som "type:værdi" f.eks. "temperatur:25.4"
                String[] parts = text.split(":");
                if (parts.length == 2) {
                    String sensorType = parts[0].toLowerCase();
                    double sensorValue = Double.parseDouble(parts[1]);

                    // Tjek for tærskeloverskridelser og send en alarm, hvis nødvendigt
                    String alarmMessage = checkThresholds(sensorType, sensorValue);

                    // Log sensordata med eller uden alarm
                    logSensorData(sensorType, sensorValue, alarmMessage);

                    // Send respons til klienten
                    if (alarmMessage != null) {
                        writer.println(alarmMessage);
                    } else {
                        writer.println("Data modtaget: " + text);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Server fejl: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void logSensorData(String sensorType, double value, String alarmMessage) {
        String logMessage = LocalDateTime.now() + " - " + sensorType + ": " + value;
        if (alarmMessage != null) {
            logMessage += " - ALARM: " + alarmMessage;
        }
        try {
            Files.write(Paths.get(LOG_FILE), (logMessage + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Fejl ved logning af data: " + e.getMessage());
        }
    }

    private String checkThresholds(String sensorType, double value) {
        switch (sensorType) {
            case "temperatur":
                if (value > HIGH_TEMP_THRESHOLD) {
                    return "Temperaturen overstiger den høje grænse! (" + value + "°C)";
                } else if (value < LOW_TEMP_THRESHOLD) {
                    return "Temperaturen er under den lave grænse! (" + value + "°C)";
                }
                break;
            case "luftfugtighed":
                if (value > HIGH_HUMIDITY_THRESHOLD) {
                    return "Luftfugtigheden overstiger den høje grænse! (" + value + "%)";
                } else if (value < LOW_HUMIDITY_THRESHOLD) {
                    return "Luftfugtigheden er under den lave grænse! (" + value + "%)";
                }
                break;
            case "jordfugtighed":
                if (value < LOW_SOIL_MOISTURE_THRESHOLD) {
                    return "Jordfugtigheden er under grænsen! (" + value + "%)";
                }
                break;
            default:
                return "Ukendt sensortype";
        }
        return null;
    }
}
