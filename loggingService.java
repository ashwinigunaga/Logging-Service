import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            logMessage(clientAddress, message, logFilePath, logFormat);
        }
    } catch (IOException e) {
        System.err.println("Error handling client: " + e.getMessage());
    }
}