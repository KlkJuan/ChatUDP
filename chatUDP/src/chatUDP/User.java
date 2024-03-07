package chatUDP;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private String username;
    private Socket socket;
    private Room currentRoom;

    public User(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }
    

    public void sendMessage(String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void joinRoom(Room room) {
        if (currentRoom != null) {
            currentRoom.removeUser(this);
        }
        currentRoom = room;
        room.addUser(this);
    }

    public void leaveRoom() {
        if (currentRoom != null) {
            currentRoom.removeUser(this);
            currentRoom = null;
        }
    }
}
