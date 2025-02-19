import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

public class LoggingService{
    private static final int THREAD_POOL_SIZE = 10;
    private static final long GLOBAL_RATE_LIMIT = 1_00; // 1 log 0.1 second
    private static final Map<String, Long> clientTimestamps = new ConcurrentHashMap<>();
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java LoggingService <port> <logFilePath> <logFormat>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        String logFilePath = args[1];
        String logFormat = "";

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Logging Service started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket, logFilePath, logFormat));
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }   


    private static void handleClient(Socket clientSocket, String logFilePath, String logFormat) {
        String clientAddress = clientSocket.getInetAddress().getHostAddress();
    
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) {
                if ("CLIENT_DISCONNECT".equals(message)) {
                    //System.out.println("Client " + clientAddress + " disconnected.");
                    logMessage(clientAddress, "Client " + clientAddress + " disconnected.", logFilePath, logFormat);
                    break;
                }
                if (allowGlobalRateLimit(clientAddress)) {
                    System.out.println("within limit");
                    logMessage(clientAddress, message, logFilePath, logFormat);
                } else {
                    System.out.println("Rate limit exceeded for client: " + clientAddress);
                    logMessage(clientAddress, "Rate limit exceeded for client: " + clientAddress, logFilePath, logFormat);
                }            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private static boolean allowGlobalRateLimit(String clientIp) {
        long now = System.currentTimeMillis();
        return clientTimestamps.merge(clientIp, now, (oldVal, newVal) -> (newVal - oldVal) > GLOBAL_RATE_LIMIT ? newVal : oldVal) == now;
    }

    private static void logMessage(String clientAddress, String message, String logFilePath, String logFormat) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
 
        String logEntry = message.replace("{timestamp}", timestamp)
                            .replace("{client}", clientAddress);
                            
        
        //System.out.println(logEntry);
        
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(logEntry + "\n");
        } catch (IOException e) {
            System.err.println("Error writing log: " + e.getMessage());
        }
    }
}