import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    // Список подключенных клиентов
    private static final Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен и ожидает подключения...");

            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка на сервере: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Добавляем клиента в список
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Получено от клиента: " + message);
                    // Рассылка сообщения другим клиентам
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            if (writer != out) { // Не отправляем сообщение отправителю
                                writer.println(message);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка при обработке клиента: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Ошибка при закрытии сокета: " + e.getMessage());
                }
                // Удаляем клиента из списка при отключении
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}