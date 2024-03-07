package chatUDP;
import java.io.*;
import java.net.*;
import java.util.*;
public class ChatServer {
    private ServerSocket serverSocket;
    private List<User> users;
    private List<Room> rooms;

    public ChatServer() {
        users = new ArrayList<>();
        rooms = new ArrayList<>();
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor de chat iniciado en el puerto " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde: " + clientSocket.getInetAddress().getHostAddress());
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private User user;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("Conexión establecida. Bienvenido al chat!");

                String username = null;
                while (username == null || username.isEmpty()) {
                    username = in.readLine();  // Espera a que se ingrese el nombre de usuario
                    user = new User(username, clientSocket);
                    users.add(user);
                }
                
                

                out.println("Bienvenido, " + username + "! Para ver la lista de comandos, escriba /help");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("/")) {
                        handleCommand(inputLine);
                    } else {
                        broadcastMessage(user.getUsername() + " - " + inputLine, user.getCurrentRoom());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (user != null) {
                        users.remove(user);
                        user.leaveRoom();
                        if (user.getCurrentRoom() == null) {
                            out.println("Primero debe unirse a una sala.");
                        } 
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        private void handleCommand(String command) {
            String[] tokens = command.split("\\s+");
            
            switch (tokens[0]) {
                case "/help":
                    out.println("Lista de comandos:");
                    out.println("/lista - Lista todas las salas disponibles");
                    out.println("/crear [nombre_sala] - Crea una nueva sala");
                    out.println("/unirse [nombre_sala] - Únete a una sala existente");
                    out.println("/salir - Salir del chat");
                    out.println("/eleminarUsuario [nombre_usuario]- Elimina usuario");
                    out.println("/eleminarSala [nombre_sala]- Elimina sala");
                    break;
                case "/lista":
                    if (rooms.isEmpty()) {
                        out.println("No hay salas Creadas.");
                    } else {
                        out.println("Salas disponibles:");
                        for (Room room : rooms) {
                            out.println("- " + room.getName());
                        }
                        out.println("/unirse para unirse o /eleminarSala para eleminar");
                    }
                    break;
                case "/crear":
                    if (tokens.length < 2) {
                        out.println("Uso: /crear [nombre_sala]");
                    } else {
                        String roomName = tokens[1];
                        Room room = createRoom(roomName);
                        if (room != null) {
                            user.joinRoom(room);
                            out.println("Sala '" + roomName + "' creada y unido exitosamente.");
                            out.println("Escriba el mensaje:");
                            
                        } else {
                            out.println("La sala '" + roomName + "' ya existe.");
                        }
                    }
                    break;
                case "/unirse":
                    if (tokens.length < 2) {
                        out.println("Uso: /unirse [nombre_sala]");
                        out.println("Escriba el mensaje:");
                    } else {
                        String roomName = tokens[1];
                        Room room = findRoom(roomName);
                        if (room != null) {
                            user.joinRoom(room);
                            out.println("Unido exitosamente a la sala '" + roomName + "'.");
                        } else {
                            out.println("La sala '" + roomName + "' no existe.");
                        }
                    }
                    break;
                
                case "/eleminarUsuario":
                    if (tokens.length < 2) {
                        out.println("Uso: /eleminarUsuario [nombre_usuario]");
                    } else {
                        String usernameToRemove = tokens[1];
                        boolean userRemoved = false;
                        for (Room room : rooms) {
                            for (User roomUser : room.listUsers()) {
                                if (roomUser.getUsername().equals(usernameToRemove)) {
                                    room.removeUser(roomUser);
                                    out.println("Usuario '" + usernameToRemove + "' eliminado de la sala '" + room.getName() + "'.");
                                    userRemoved = true;
                                    break;
                                }
                            }
                            if (userRemoved) {
                                break;
                            }
                        }
                        if (!userRemoved) {
                            out.println("El usuario '" + usernameToRemove + "' no se encontró en ninguna sala.");
                        }
                    }
                    break;

                case "/eleminarSala":
                    if (tokens.length < 2) {
                        out.println("Uso: /eleminarSala [nombre_sala]");
                    } else {
                        String roomNameToRemove = tokens[1];
                        Room roomToRemove = null;
                        for (Room room : rooms) {
                            if (room.getName().equals(roomNameToRemove)) {
                                roomToRemove = room;
                                break;
                            }
                        }
                        if (roomToRemove != null) {
                            rooms.remove(roomToRemove);
                            out.println("Sala '" + roomNameToRemove + "' eliminada correctamente.");
                        } else {
                            out.println("La sala '" + roomNameToRemove + "' no existe.");
                        }
                    }
                    break;
               
                case "/salir":
                    if (user.getCurrentRoom() != null) {
                        user.leaveRoom();
                        out.println("Desconectado del chat y salió de la sala.");
                    } else {
                        out.println("Desconectado del chat.");
                    }
                    break;

                default:
                    out.println("Comando no reconocido. Escriba /help para ver la lista de comandos.");
                    
                    
            }
        }
    }

    public synchronized void broadcastMessage(String message, Room room) {
        if (room != null) {
            List<User> roomUsers = room.listUsers();
            for (User user : roomUsers) {
                user.sendMessage(message);
            }
        } else {
            System.out.println("La sala es nula. No se puede enviar el mensaje.");
        }
    }

    public Room findRoom(String roomName) {
        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(roomName)) {
                return room;
            }
        }
        return null; // Retorna null si no se encuentra la sala
    }

    public synchronized Room createRoom(String roomName) {
        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(roomName)) {
                return null; // La sala ya existe
            }
        }
        // Si la sala no existe, crea una nueva
        Room newRoom = new Room(roomName);
        rooms.add(newRoom);
        return newRoom;
    }

	public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.startServer(8088);
    }
}


