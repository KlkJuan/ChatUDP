package chatUDP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private static String username;
    private Socket socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;

    public ChatClient(String username) {
        this.username = username;
    }

    public void connectToServer(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new PrintWriter(socket.getOutputStream(), true);
            outputStream.println(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command) {
        outputStream.println(command);
    }

    public String receiveMessage() {
        try {
            return inputStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Ingrese su nombre de usuario: ");
        try {
            username = consoleInput.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChatClient client = new ChatClient(username);
        client.connectToServer("127.0.0.1", 8088);

        Thread messageReceiverThread = new Thread(() -> {
            while (true) {
                String message = client.receiveMessage();
                if (message != null) {
                    System.out.println(message);
                }
            }
        });
        messageReceiverThread.start();

        try {
            while (true) {
                System.out.print("Mensaje: ");
                String message = consoleInput.readLine();
                client.sendCommand(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                consoleInput.close();
                messageReceiverThread.interrupt();
                client.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

