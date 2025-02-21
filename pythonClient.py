import socket
import sys
import os
import time
from datetime import datetime

def send_log_message(host, port, message=None):
    try:
         with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
             client_socket.connect((host, port))
             print("Connected to logging server. Type messages to send, or type 'exit' to quit.")
             
             if message: #for automated messages
                 if message.lower() == 'exit':
                       print("Closing connection.")
                       client_socket.sendall(b'CLIENT_DISCONNECT\n') #log when the client disconnects
                       return
                 else:
                       client_socket.sendall(message.encode('utf-8') + b'\n')
                        #print("Test log message sent successfully.")
                       return
             
             while True:
                  message = input("Enter log message: ")
                  if message.lower() == 'exit':
                       print("Closing connection.")
                       client_socket.sendall(b'CLIENT_DISCONNECT\n') #log when the client disconnects

                       break
                  
                  timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                  client_socket.sendall(message.encode('utf-8') + b'\n')
                  print("Formatted log message sent successfully.")
                  
    except Exception as e:
        print(f"Error: {e}")

def verify_log_entry(log_file, expected_message):
    if os.path.exists(log_file):
        with open(log_file, "r") as file:
            logs = file.read()
            return expected_message in logs
    return False

def test_logging_service(host, port, log_file):
    test_message = "Automated test log entry"
    print("Starting automated test...")
    send_log_message(host, port, test_message)
    time.sleep(2)  # Allow server time to process the message
    send_log_message(host,port,"exit")
    

def test_rate_limit(host, port, count, delay):
    print("Starting rate limit test...")
    for i in range(count):
        send_log_message(host, port, f"Test message {i+1}")
        time.sleep(delay)
    print("Rate limit test completed. Check server logs to verify rate limiting behavior.")
    send_log_message(host, port, "exit")

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
        send_log_message(host, port)