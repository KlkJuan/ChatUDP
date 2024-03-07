package chatUDP;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private List<User> users;
    private List<String> messages;
    public Room(String name) {
        this.name = name;
        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public synchronized void addUser(User user) {
        users.add(user);
    }

    public synchronized void removeUser(User user) {
        users.remove(user);
    }

    public synchronized void sendMessage(String message, User sender) {
        for (User user : users) {
            if (!user.equals(sender)) {
                user.sendMessage(message);
            }
        }
        messages.add(message);
    }

    public synchronized List<User> listUsers() {
        return new ArrayList<>(users);
    }
    
    public List<String> getMessages() {
        return messages;
    }

    public String getName() {
        return name;
    }
}
