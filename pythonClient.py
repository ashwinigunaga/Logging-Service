#   PROJECT        : SENG204O - Assignment #3
#   STUDENT 1 NAME : Jyot Shah
#   STUDENT 2 NAME : Ashwini Gunaga
#   STUDENT 1 ID   : 8871717
#   STUDENT 2 ID   : 8888180
#   FILE NAME      : pythonClient.py
#   DATE           : 21/02/2025

import socket
import sys
import os
import time
from datetime import datetime

# Function name : connect_to_server
# Function description : Function to establish a connection to the logging server.
# Function Parameters :
#   host - The IP address of the server.
#   port - The port number of the logging server.
# Function Returns : None or client_socket

def connect_to_server(host, port):
    try:
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((host, port))
        print("Connected to logging server. Type messages to send, or type 'exit' to quit.")
        return client_socket
    except Exception as e:
        print(f"[ERROR] Unable to connect: {e}")
        return None

# Function name : send_log_message
# Function description : Function to automate sending a test log message.
# Function Parameters :
#   client_socket - The connected client socket.
#   message - The log message to send.
# Function Returns : None 

def send_log_message(client_socket, message=None):
    try:  
             if message: #for automated messages
                 if message.lower() == 'exit':
                       print("Closing connection.")
                       client_socket.sendall(b'CLIENT_DISCONNECT\n') #log when the client disconnects
                       client_socket.close()
                       return
                 else:
                       client_socket.sendall(message.encode('utf-8') + b'\n') # Send log message
                       return
    except Exception as e:
        print(f"Error sending Log Message: {e}")

# Function name : test_logging_service
# Function description : Function to automate sending a test log message.
# Function Parameters :
#   host - The IP address of the server.
#   port - The port number of the logging server.
# Function Returns : None 

def test_logging_service(host, port):
    test_message = "Automated test log entry"
    print("Starting automated test...")
    client_socket = connect_to_server(host, port) # Establishes connection
    if client_socket:
        send_log_message(client_socket, test_message) # Send test message
        time.sleep(1) # Waits before exiting
        send_log_message(client_socket, "exit")
    
# Function name : test_rate_limit
# Function description : Function to test server's rate-limiting feature
# Function Parameters :
#   host - The IP address of the server.
#   port - The port number of the logging server.
#   count - The number of messages to send.
# Function Returns : None 
def test_rate_limit(host, port, count):
    print("Starting rate limit test...")
    client_socket = connect_to_server(host, port)  # Establishes connection
    if client_socket:
        for i in range(count):
            send_log_message(client_socket, f"Test message {i+1}")  # Sends multiple messages
        print("Rate limit test completed. Check server logs to verify rate limiting behavior.")
        send_log_message(client_socket, "exit") # Closes connection

# Function name : main
# Function description : Main program 
# Function Parameters : None
# Function Returns : None 
if __name__ == "__main__":
    # Checks if the correct number of arguments are provided
    if len(sys.argv) < 3:
        print("Usage: python test_client.py <host> <port> [auto/rate (optional)]")
        sys.exit(1)
        
    # Reads command-line arguments
    host = sys.argv[1]
    port = int(sys.argv[2])

    
    # Checks for optional test mode
    if len(sys.argv) > 3 and sys.argv[3].lower() == "auto":
        test_logging_service(host, port)
    elif len(sys.argv) > 3 and sys.argv[3].lower() == "rate":
        test_rate_limit(host, port, count=100)
    else:
        # Starts interactive mode for sending manual log messages
        client_socket = connect_to_server(host, port)
        if not client_socket:
            sys.exit(1)
        while client_socket and not client_socket._closed:
            message = input("Enter log message: ")
            send_log_message(client_socket, message)
