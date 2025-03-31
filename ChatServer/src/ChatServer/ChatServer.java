package ChatServer;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, Socket> clients = new HashMap<>(); // Lưu username và PrintWriter

    public static void main(String[] args) {
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

                // Yêu cầu client gửi username
                out.println("Nhập username của bạn:");
                username = in.readLine();
                if (username == null || username.trim().isEmpty()) {
                    username = "Anonymous_" + new Random().nextInt(1000); // Đặt tên mặc định nếu không hợp lệ
                }

                // Kiểm tra username trùng
                synchronized (clients) {
                    while (clients.containsKey(username)) {
                        out.println("Username đã tồn tại, vui lòng chọn tên khác:");
                        username = in.readLine();
                    }
                    clients.put(username, socket);
                }
                broadcast(username + " đã tham gia phòng chat!");

                // Nhận và xử lý tin nhắn từ client
                String message;
                while ((message = in.readLine()) != null) {
                	String keyword = "Hello world";
					String key = Vigenere.generateKey(message.toString(), Vigenere.LowerToUpper(keyword));
					String cipherMessage = Vigenere.cipherText(Vigenere.LowerToUpper(message.toString()), key);
					System.out.println("Message from" +username +": " +message + "\t"+ "Encrypted message:  "+ cipherMessage);
					System.out.println(dataStream.available());
                    if(message.startsWith("#FILE#")) {
                    	receiveAndForwardFile(dataStream, message);
                    }else if (message.startsWith("@")) {
                        sendPrivateMessage(message);
                    } else {
                        broadcast(username + ": " + message);
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

        // Gửi tin nhắn công khai đến tất cả client
        private void broadcast(String message) {
            synchronized (clients) {
                for (Socket client : clients.values()) {
                	try {
						PrintWriter printer = new PrintWriter(client.getOutputStream(),true);
						printer.println(message);
                	}catch(IOException e) {
                		e.printStackTrace();
                	}
                }
            }
        }

        // Gửi tin nhắn riêng
        private void sendPrivateMessage(String message) {
            String[] parts = message.split(":", 2);
            if (parts.length < 2) {
                out.println("Cú pháp sai! Dùng: @username:tin_nhắn");
                return;
            }
            String targetUsername = parts[0].substring(1).trim(); // Bỏ "@"
            String privateMessage = parts[1].trim();

            synchronized (clients) {
            	try {
					PrintWriter targetClient = new PrintWriter(clients.get(targetUsername).getOutputStream(),true);
					if (targetClient != null) {
						targetClient.println("[Tin riêng từ " + username + "]: " + privateMessage);
						out.println("[Đã gửi đến " + targetUsername + "]: " + privateMessage);
					} else {
						out.println("Không tìm thấy người dùng: " + targetUsername);
					}
            	}catch(IOException e) {
            		e.printStackTrace();
            	}
            }
        }
        private void receiveAndForwardFile(DataInputStream dataIn, String fileHeader) throws IOException {
            String[] parts = fileHeader.split("#", 4);
            String target = parts[2]; // Có thể là "ALL" hoặc username
            String fileName = dataIn.readUTF();
            long fileSize = dataIn.readLong();
            byte[] fileData = new byte[(int) fileSize];
            dataIn.readFully(fileData);

            synchronized (clients) {
                if (target.equals("ALL")) {
                    for (Map.Entry<String, Socket> entry : clients.entrySet()) {
                        if (!entry.getKey().equals(username)) { // Không gửi lại cho người gửi
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
				PrintWriter printer= new PrintWriter(target.getOutputStream(),true);
				printer.println("#FILE#" + sender);
				printer.flush();
				DataOutputStream dataOut = new DataOutputStream(target.getOutputStream());
				dataOut.writeUTF(fileName);
				dataOut.writeLong(fileData.length);
				dataOut.write(fileData);
				dataOut.flush();
        	}catch(IOException e) {
        		e.printStackTrace();
        	}
        }
    }
}