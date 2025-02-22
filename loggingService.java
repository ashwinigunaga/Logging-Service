    // PROJECT       : SENG204O - Assignment #3
    // STUDENT 1 NAME : Jyot Shah
    // STUDENT 2 NAME : Ashwini Gunaga
    // STUDENT 1 ID : 8871717
    // STUDENT 2 ID : 8888180
    // FILE NAME : loggingService.java
    // DATE : 21/02/2025

    import java.io.*;
    import java.net.*;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Map;
    import java.util.concurrent.*;

    // Class name : LoggingService
    // Class Description : This class implements a logging server that listens for client connections,
    //                     implements rate limits, and logs messages to a file in a given format.
    //                     It handles multiple clients simultaneously.

    public class LoggingService{
        private static final int THREAD_POOL_SIZE = 10; // Maximum number of threads
        private static final long GLOBAL_RATE_LIMIT = 1_00; // Rate limit: 1 log per 0.1 second
        private static final Map<String, Long> clientTimestamps = new ConcurrentHashMap<>();

        //  Function name: main
        //  Function description: Main method to start the logging service.
        //  Function Parameters: 
        //      String[] args - contains port, log file path, and log format.
        //  Function Returns: void

        public static void main(String[] args) {
            if (args.length < 3) {
                System.out.println("Usage: java LoggingService <port> <logFilePath> <logFormat>");
                return;
            }
            int port = Integer.parseInt(args[0]);
            String logFilePath = args[1];
            String logFormat = args[2];

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

        //  Function name : handleClient
        //  Function description : Handles client connections and logs messages.
        //  Function Parameters :
        //      Socket clientSocket- The client socket connection.
        //      String logFilePath - The file path where logs should be stored.
        //      String logFormat - The format of the logs.
        //  Function Returns : void

        private static void handleClient(Socket clientSocket, String logFilePath, String logFormat) {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            logMessage(clientAddress, "INFO", "Client " + clientAddress + " connected.", logFilePath, logFormat);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String message;
                while ((message = reader.readLine()) != null) {
                    if ("CLIENT_DISCONNECT".equals(message)) {
                        //System.out.println("Client " + clientAddress + " disconnected.");
                        logMessage(clientAddress, "INFO", "Client " + clientAddress + " disconnected.", logFilePath, logFormat);
                        break;
                    }
                    if (allowGlobalRateLimit(clientAddress)) {
                        logMessage(clientAddress, "INFO", message, logFilePath, logFormat);
                    } 
                    else {
                        //System.out.println("Rate limit exceeded for client: " + clientAddress);
                        logMessage(clientAddress, "WARNING", "Rate limit exceeded for client: " + clientAddress, logFilePath, logFormat);
                    }            }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            }
        }

        //  Function name : handleClient
        //  Function description : Applies a global rate limit for logging.
        //  Function Parameters :
        //      String clientIp - The IP address of the client.
        //  Function Returns : boolean

        private static boolean allowGlobalRateLimit(String clientIp) {
            long now = System.currentTimeMillis();
            return clientTimestamps.merge(clientIp, now, (oldVal, newVal) -> (newVal - oldVal) > GLOBAL_RATE_LIMIT ? newVal : oldVal) == now;
        }

        //  Function name : logmessage
        //  Function description : Logs a message to the specified log file.
        //  Function Parameters :
        //      String clientAddress - The IP address of the client which sends the log message.
        //      String logLevel      - The level of the log message (INFO, WARNING, ERROR, etc.).
        //      String message       - The message content.
        //      String logFilePath   - The file path where logs should be saved.
        //      String logFormat     - The format for logging messages.
        //  Function Returns : void

        private static void logMessage(String clientAddress, String logLevel, String message, String logFilePath, String logFormat) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logEntry;
            logEntry = logFormat.replace("{timestamp}", timestamp)
                                .replace("{client}", clientAddress)
                                .replace("{level}", logLevel)
                                .replace("{message}", message);
            
            try (FileWriter writer = new FileWriter(logFilePath, true)) {
                writer.write(logEntry + "\n");
            } catch (IOException e) {
                System.err.println("Error writing log: " + e.getMessage());
                logEntry = logEntry.replace("{message}", e.getMessage())
                                    .replace("{level}","ERROR");
            }
        }
    }