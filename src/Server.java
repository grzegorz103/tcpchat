import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private List<ClientThread> users = new ArrayList<>();
    private ServerSocket serverSocket;
    private Socket socket;

    public static void main(String... args) throws IOException {
        Server server = new Server();
        server.serverSocket = new ServerSocket(1500);
        while (true) {
            server.socket = server.serverSocket.accept();
            System.out.println("Client accepted");
            ClientThread client = new ClientThread(server.socket, server::getUsers);
            var thread = new Thread(client);
            server.users.add(client);
            thread.start();
        }
    }

    private List<ClientThread> getUsers() {
        return users;
    }
}

@FunctionalInterface
interface GetList {
    List<ClientThread> getUserList();
}

class ClientThread implements Runnable {

    private Socket socket;
    private List<ClientThread> userList;
    private OutputStream os;
    private String nick;

    ClientThread(Socket socket, GetList list) throws IOException {
        this.socket = socket;
        this.userList = list.getUserList();
        this.os = socket.getOutputStream();
    }

    private void sendAll(String... message) {
        this.userList.stream()
                .filter(x -> !x.socket.isClosed())
                .forEachOrdered(x -> x.send(message[0] + ": " + message[1] + "\n"));
    }

    private void send(String message) {
        try {
            os.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToRecipient(String... message) {
        this.userList.stream()
                .filter(x -> x.nick.equals(message[1]))
                .filter(x -> !x.socket.isClosed())
                .forEachOrdered(x -> x.send("Message from: " + message[0] + ": " + message[1] + "\n"));
    }

    private void sendFile(String... message) {
        this.userList.stream()
                .filter(x -> x.nick.equals(message[1]))
                .forEachOrdered(x -> x.startSending(message[3]));
    }

    private void startSending(String link) {
        try (FileInputStream fileStream = new FileInputStream(link);
             BufferedInputStream fileBuffer = new BufferedInputStream(fileStream)) {
            os.write("#\n".getBytes());
            os.write((link + "\n").getBytes());

            var count = 0;
            byte[] data = new byte[2048];
            while ((count = fileBuffer.read(data)) > 0) {
                os.write(data, 0, count);
                os.flush();
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader bis = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.nick = bis.readLine();
            while (true) {
                String line = bis.readLine();
                String[] temp = line.split(": ");
                if (temp.length == 3) {
                    sendToRecipient(temp);
                } else if (temp.length == 4) {
                    sendFile(temp);
                } else {
                    sendAll(temp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

