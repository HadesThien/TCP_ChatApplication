package ChatServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, Socket> clients = new HashMap<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ChatServer";

    public static void main(String[] args) {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Địa chỉ IP của localhost: " + localhost.getHostAddress());
            System.out.println("PORT: " + PORT);
            ConnectionDB.connectDB(DB_URL);
        } catch (UnknownHostException e) {
            System.err.println("Không thể xác định địa chỉ localhost");
            e.printStackTrace();
        }
        System.out.println("Server đang chạy...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới đã kết nối: " + clientSocket);
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ConnectionDB.closeConnection(); // Đóng kết nối database khi server tắt
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                InputStream is = socket.getInputStream();
                DataInputStream dataStream = new DataInputStream(is);
                BufferedReader in = new BufferedReader(new InputStreamReader(is));

                // Xử lý đăng nhập
                String loginRequest;
                while ((loginRequest = in.readLine()) != null) {
                    if (loginRequest.startsWith("#LOGIN#")) {
                        String[] parts = loginRequest.split("#");
                        if (parts.length == 4) {
                            username = parts[2];
                            String md5Password = parts[3];

                            if (ConnectionDB.verifyLogin(username, md5Password)) {
                                synchronized (clients) {
                                    if (clients.containsKey(username)) {
                                        out.println("Username đang được sử dụng!");
                                        continue;
                                    }
                                    clients.put(username, socket);
                                }
                                out.println("LOGIN_SUCCESS");
                                broadcast(username + " đã tham gia phòng chat!");
                                break;
                            } else {
                                out.println("LOGIN_FAILED");
                            }
                        }
                    }
                }

                // Nhận và xử lý tin nhắn từ client
                String message;
                while ((message = in.readLine()) != null) {
                    String keyword = "Hello world";
                    String key = Vigenere.generateKey(message.toString(), Vigenere.LowerToUpper(keyword));
                    String cipherMessage = Vigenere.cipherText(Vigenere.LowerToUpper(message.toString()), key);
                    System.out.println("Message from " + username + ": " + message + "\tEncrypted message: " + cipherMessage);

                    if (message.startsWith("#FILE#")) {
                        receiveAndForwardFile(dataStream, message);
                    } else if (message.startsWith("@")) {
                        sendPrivateMessage(message);
                        // Lưu tin nhắn riêng vào database
                        String[] parts = message.split(":", 2);
                        if (parts.length == 2) {
                            String targetUsername = parts[0].substring(1).trim();
                            String privateMessage = parts[1].trim();
                            ConnectionDB.saveMessage(username, targetUsername, privateMessage);
                        }
                    } else {
                        broadcast(username + ": " + message);
                        // Lưu tin nhắn công khai vào database (receiver = null)
                        ConnectionDB.saveMessage(username, username, message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    synchronized (clients) {
                        clients.remove(username);
                    }
                    broadcast(username + " đã rời phòng chat!");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (Socket client : clients.values()) {
                    try {
                        PrintWriter printer = new PrintWriter(client.getOutputStream(), true);
                        printer.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void sendPrivateMessage(String message) {
            String[] parts = message.split(":", 2);
            if (parts.length < 2) {
                out.println("Cú pháp sai! Dùng: @username:tin_nhắn");
                return;
            }
            String targetUsername = parts[0].substring(1).trim();
            String privateMessage = parts[1].trim();

            synchronized (clients) {
                try {
                    Socket targetSocket = clients.get(targetUsername);
                    if (targetSocket != null) {
                        PrintWriter targetClient = new PrintWriter(targetSocket.getOutputStream(), true);
                        targetClient.println("[Tin riêng từ " + username + "]: " + privateMessage);
                        out.println("[Đã gửi đến " + targetUsername + "]: " + privateMessage);
                    } else {
                        out.println("Không tìm thấy người dùng: " + targetUsername);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void receiveAndForwardFile(DataInputStream dataIn, String fileHeader) throws IOException {
            String[] parts = fileHeader.split("#", 4);
            String target = parts[2];
            String fileName = dataIn.readUTF();
            long fileSize = dataIn.readLong();
            byte[] fileData = new byte[(int) fileSize];
            dataIn.readFully(fileData);

            synchronized (clients) {
                if (target.equals("ALL")) {
                    for (Map.Entry<String, Socket> entry : clients.entrySet()) {
                        if (!entry.getKey().equals(username)) {
                            sendFile(entry.getValue(), fileName, fileData, username);
                        }
                    }
                    out.println("\nĐã gửi file " + fileName + " đến tất cả!");
                } else {
                    Socket targetClient = clients.get(target);
                    if (targetClient != null) {
                        sendFile(targetClient, fileName, fileData, username);
                        out.println("\nĐã gửi file " + fileName + " đến " + target);
                    } else {
                        out.println("Không tìm thấy người dùng: " + target);
                    }
                }
            }
        }

        private void sendFile(Socket target, String fileName, byte[] fileData, String sender) throws IOException {
            try {
                PrintWriter printer = new PrintWriter(target.getOutputStream(), true);
                printer.println("#FILE#" + sender);
                printer.flush();
                DataOutputStream dataOut = new DataOutputStream(target.getOutputStream());
                dataOut.writeUTF(fileName);
                dataOut.writeLong(fileData.length);
                dataOut.write(fileData);
                dataOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}