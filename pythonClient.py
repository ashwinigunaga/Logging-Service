import socket
import sys

def send_log_message(host, port):
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((host, port))
    print("Connected to logging server.")

if __name__ == "__main__":
    host = sys.argv[1]
    port = int(sys.argv[2])
    send_log_message(host, port)
