import socket
import sys
from datetime import datetime

def send_log_message(host, port, log_format):
    client_ip = socket.gethostbyname(socket.gethostname())
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
        client_socket.connect((host, port))
        print("Connected to logging server.")

while True:
    message = input("Enter log message: ")
    if message.lower() == 'exit':
        print("Closing connection.")
        break

    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    formatted_message = log_format.replace("{timestamp}", timestamp)
    formatted_message = formatted_message.replace("{client}", client_ip)
    formatted_message = formatted_message.replace("{message}", message)
    
    client_socket.sendall(formatted_message.encode('utf-8'))
    print("Formatted log message sent successfully.")
    
if __name__ == "__main__":
    host = sys.argv[1]
    port = int(sys.argv[2])
    log_format = sys.argv[3]

    send_log_message(host, port, log_format)
