import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client {
    private static JTextArea textArea;
    private static PrintWriter out;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Чат");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.PINK); // Устанавливаем фон JTextArea на розовый
        textArea.setForeground(Color.CYAN); // Устанавливаем цвет текста на голубой
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.PINK); // Устанавливаем фон панели ввода на розовый
        JTextField inputField = new JTextField(25);
        JButton sendButton = new JButton("повелеваю");
        inputPanel.add(inputField);
        inputPanel.add(sendButton);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        sendButton.addActionListener(e -> {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                sendMessage(message);
                inputField.setText("");
            }
        });

        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    appendMessage("Другой клиент: " + serverMessage, false);
                }
            } catch (IOException e) {
                appendMessage("Ошибка при подключении к серверу: " + e.getMessage(), false);
            }
        }).start();
    }

    private static void sendMessage(String message) {
        out.println(message);
        appendMessage(": " + message, true);
    }

    private static void appendMessage(String message, boolean isOwnMessage) {
        SwingUtilities.invokeLater(() -> {
            String rectangle = isOwnMessage ? "^*.*^ " : " ";
            textArea.append(rectangle + message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
