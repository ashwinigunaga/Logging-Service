import socket
import sys
import os
import time
from datetime import datetime

def connect_to_server(host, port):
    try:
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((host, port))
        print("Connected to logging server. Type messages to send, or type 'exit' to quit.")
        return client_socket
    except Exception as e:
        print(f"[ERROR] Unable to connect: {e}")
        return None

def send_log_message(client_socket, message=None):
    try:  
             if message: #for automated messages
                 if message.lower() == 'exit':
                       print("Closing connection.")
                       client_socket.sendall(b'CLIENT_DISCONNECT\n') #log when the client disconnects
                       client_socket.close()
                       return
                 else:
                       client_socket.sendall(message.encode('utf-8') + b'\n')
                       return
    except Exception as e:
        print(f"Error sending Log Message: {e}")


def test_logging_service(host, port, log_file):
    test_message = "Automated test log entry"
    print("Starting automated test...")
    client_socket = connect_to_server(host, port)
    if client_socket:
        send_log_message(client_socket, test_message)
        time.sleep(2)
        send_log_message(client_socket, "exit")
    

def test_rate_limit(host, port, count, delay):
    print("Starting rate limit test...")
    client_socket = connect_to_server(host, port)
    if client_socket:
        for i in range(count):
            send_log_message(client_socket, f"Test message {i+1}")
            time.sleep(delay)
        print("Rate limit test completed. Check server logs to verify rate limiting behavior.")
        send_log_message(client_socket, "exit")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python test_client.py <host> <port> [auto/rate (optional)]")
        sys.exit(1)
        
    host = sys.argv[1]
    port = int(sys.argv[2])
    #log_format = sys.argv[3]
    log_file = "log.txt"
    
    if len(sys.argv) > 3 and sys.argv[3].lower() == "auto":
        test_logging_service(host, port, log_file)
    elif len(sys.argv) > 3 and sys.argv[3].lower() == "rate":
        test_rate_limit(host, port, count=100, delay=0.01)
    else:
        client_socket = connect_to_server(host, port)
        if not client_socket:
            sys.exit(1)
        while client_socket and not client_socket._closed:
            message = input("Enter log message: ")
            send_log_message(client_socket, message)
