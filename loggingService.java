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
        private static final int LOG_LIMIT = 40;
        private static final long REFILL_INTERVAL_MS = 1000;

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
            LogBucket logBucket = new LogBucket(LOG_LIMIT, REFILL_INTERVAL_MS);
            logMessage(clientAddress, "INFO", "Client " + clientAddress + " connected.", logFilePath, logFormat);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String message;
                while ((message = reader.readLine()) != null) {
                    if ("CLIENT_DISCONNECT".equals(message)) {
                        //Log a message when the client disconnects
                        logMessage(clientAddress, "INFO", "Client " + clientAddress + " disconnected.", logFilePath, logFormat);
                        break;
                    }
                    if (logBucket.consumeToken()) { //check for rate limit
                        logMessage(clientAddress, "INFO", message, logFilePath, logFormat);
                    } 
                    else {
                        logMessage(clientAddress, "WARNING", "Rate limit exceeded for client: " + clientAddress, logFilePath, logFormat);
                        break;
                    }            }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
                logMessage(clientAddress, "ERROR", e.getMessage(), logFilePath, logFormat);

            }
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
            }
        }
    }

    class LogBucket {
    private int noOfLogs;
    private final int maxLogs;
    private final long refillIntervalMs;
    private long lastRefillTime;

    public LogBucket(int maxLogs, long refillIntervalMs) {
        this.maxLogs = maxLogs;
        this.refillIntervalMs = refillIntervalMs;
        this.noOfLogs = maxLogs;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean consumeToken() {
        resetLogLimit();
        return noOfLogs > 0 && (noOfLogs-- > 0);
    }

    private void resetLogLimit() {
        long now = System.currentTimeMillis();
        if (now - lastRefillTime > refillIntervalMs) {
            noOfLogs = Math.min(maxLogs, noOfLogs + (int) ((now - lastRefillTime) / refillIntervalMs));
            lastRefillTime = now;
        }
    }
}