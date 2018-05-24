import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    private String nick;
    private final static String HOST = "localhost";
    private final static int PORT = 1500;
    private InputStream is;
    private OutputStream os;
    private BufferedReader bis;

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        do {
            System.out.println("Enter your nick");
            main.nick = new Scanner(System.in).nextLine();
            System.out.println(main.nick.length() <= 0 ? "Nick cannot be empty : " : "Welcome " + main.nick);
        } while (main.nick.length() <= 0);

        main.startChat();
    }


    private void startChat() throws IOException {

        var socket = new Socket(HOST, PORT);
        this.os = socket.getOutputStream();
        this.is = socket.getInputStream();
        var thread = new Thread(this::startListening);

        thread.start();
        start();
    }

    private void startListening() {

        this.bis = new BufferedReader(new InputStreamReader(is));
        while (true) {
            try {
                String line = this.bis.readLine();
                if (line.equals("#")) {
                    receiveFile();
                } else {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveFile() throws IOException {
        var fileName = bis.readLine();
        FileOutputStream fos = new FileOutputStream("copy_" + fileName);
        BufferedOutputStream fileBuffer = new BufferedOutputStream(fos);

        var count = 0;
        var data = new byte[2048];
        while ((count = is.read(data)) > 0) {
            fileBuffer.write(data, 0, count);
        }

        fileBuffer.close();
        fos.close();
        System.out.println("File " + fileName + " has been received.");
        System.exit(0);
    }

    private void start() {
        try {
            os.write((this.nick + "\n").getBytes());

            while (true) {
                var line = new Scanner(System.in).nextLine();
                os.write((this.nick + ": " + line + "\n").getBytes());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
