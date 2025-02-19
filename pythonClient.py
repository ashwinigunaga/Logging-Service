import socket
import sys
import os
import time
from datetime import datetime

def send_log_message(host, port, log_format, message=None):
    try:
         client_ip = socket.gethostbyname(socket.gethostname())
         timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
         
         with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
             client_socket.connect((host, port))
             print("Connected to logging server. Type messages to send, or type 'exit' to quit.")

             if message: #for automated messages
                 timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                 formatted_message = log_format.replace("{timestamp}", timestamp)
                 formatted_message = formatted_message.replace("{client}", client_ip)
                 formatted_message = formatted_message.replace("{message}", message)
                
                 client_socket.sendall(formatted_message.encode('utf-8') + b'\n')
                 #print("Test log message sent successfully.")
                 return formatted_message
             
             while True:
                  message = input("Enter log message: ")
                  if message.lower() == 'exit':
                       print("Closing connection.")
                       client_socket.sendall(b'CLIENT_DISCONNECT\n') #log when the client disconnects

                       break
                  
                  timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                  formatted_message = log_format.replace("{timestamp}", timestamp)
                  formatted_message = formatted_message.replace("{client}", client_ip)
                  formatted_message = formatted_message.replace("{message}", message)
                  
                  client_socket.sendall(formatted_message.encode('utf-8') + b'\n')
                  print("Formatted log message sent successfully.")
                  
    except Exception as e:
        print(f"Error: {e}")

def verify_log_entry(log_file, expected_message):
    if os.path.exists(log_file):
        with open(log_file, "r") as file:
            logs = file.read()
            return expected_message in logs
    return False

def test_logging_service(host, port, log_format, log_file):
    test_message = "Automated test log entry"
    print("Starting automated test...")
    expected_log = send_log_message(host, port, log_format, test_message)
    time.sleep(2)  # Allow server time to process the message
    
    if expected_log and verify_log_entry(log_file, expected_log):
        print("Test passed: Log entry found.")
    else:
        print("Test failed: Log entry not found.")

def test_rate_limit(host, port, log_format, request_count, delay_between_requests):
    print("Starting rate limit test...")
    for i in range(request_count):
        send_log_message(host, port, log_format, f"Test message {i+1}")
        time.sleep(delay_between_requests)
    print("Rate limit test completed. Check server logs to verify rate limiting behavior.")

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python test_client.py <host> <port> <logFormat> [auto/rate (optional)]")
        sys.exit(1)
        
    host = sys.argv[1]
    port = int(sys.argv[2])
    log_format = sys.argv[3]
    log_file = "log.txt"

    if len(sys.argv) > 4 and sys.argv[4].lower() == "auto":
        test_logging_service(host, port, log_format, log_file)
    elif len(sys.argv) > 4 and sys.argv[4].lower() == "rate":
        test_rate_limit(host, port, log_format, request_count=10, delay_between_requests=0.01)
    else:
        send_log_message(host, port, log_format)
