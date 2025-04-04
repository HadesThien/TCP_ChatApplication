package ChatClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class ChatClient {
    private JFrame frame;
    private JPanel contentPane;
    private JTextArea chatTextArea;
    private JPanel controlPanel;
    private JTextArea typingTextArea;
    private JButton fileButton;
    private JPanel avatar;
    private Container titlePanel;
    private JLabel clientJLabel;
    private JButton sendButton;

    private PrintWriter out;
    private Socket socket;
    private String username;

    public ChatClient() {
        // Hiển thị trang đăng nhập
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        // Nếu đăng nhập thành công, lấy thông tin từ LoginDialog
        if (loginDialog.getUsername() != null) {
            username = loginDialog.getUsername();
            socket = loginDialog.getSocket();
            out = loginDialog.getPrintWriter();

            // Khởi tạo giao diện chat
            initializeUI(loginDialog);
            startMessageListener();
        } else {
            System.exit(0); // Thoát nếu không đăng nhập thành công
        }
    }

    private void initializeUI(LoginDialog loginDialog) {
        // Tạo giao diện Swing
        frame = new JFrame("Chat Client - " + username);
        chatTextArea = new JTextArea(20, 50);
        chatTextArea.setEditable(false);

        sendButton = new JButton("Gửi");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Custom
		
		frame.setBounds(100, 100, 600, 400);
		frame.setSize(918,613);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(214, 214, 214));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		this.chatTextArea = new JTextArea();
		chatTextArea.setBounds(5, 52, 879, 371);
		chatTextArea.setBackground(Color.WHITE);
		chatTextArea.setEditable(false);
		chatTextArea.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 15));
		chatTextArea.setText("Server: Welcome "+ loginDialog.getUsername()+"! You would give me a message and I'm going to \r\nsend you a crypted message\r\nServer: Let's talk!\r\n");
		chatTextArea.setForeground(SystemColor.desktop);
		
		controlPanel = new JPanel();
		controlPanel.setBounds(5, 426, 879, 130);
		controlPanel.setBackground(new Color(255, 255, 255));
		controlPanel.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 13));
		
		typingTextArea = new JTextArea();
		typingTextArea.setBounds(10, 5, 609, 114);
		typingTextArea.setMargin(new Insets(10,10,10,10));
		typingTextArea.setRows(3);
		typingTextArea.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 15));
        frame.pack();
        frame.setVisible(true);
		controlPanel.setLayout(null);
		controlPanel.add(typingTextArea);
		
		fileButton = new JButton("File");
		fileButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {sendFile();} });
		fileButton.setBounds(745, 77, 118, 42);
		BufferedImage originalIcon = null;
		try {
			originalIcon = ImageIO.read(ChatClient.class.getResource("/ChatClient/FileIcon.png"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		Image scaledIcon = originalIcon.getScaledInstance(25 , 25, Image.SCALE_SMOOTH);
		Icon icon = new ImageIcon(scaledIcon);
		
		fileButton.setIcon(icon);
		fileButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		fileButton.setVerticalTextPosition(SwingConstants.CENTER);
		fileButton.setIconTextGap(10);
		fileButton.setBackground(new Color(8, 150, 229));
		fileButton.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 15));
		controlPanel.add(fileButton);
		
		titlePanel = new JPanel();
		titlePanel.setBounds(5, 5, 879, 41);
		titlePanel.setBackground(new Color(8, 150, 219));
		titlePanel.setLayout(null);
		
		JLabel nameChatterLabel = new JLabel("Server Chatter");
		nameChatterLabel.setForeground(new Color(255, 255, 255));
		nameChatterLabel.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 22));
		nameChatterLabel.setBounds(73, 11, 185, 19);
		titlePanel.add(nameChatterLabel);
		
		avatar = new JPanel() {
			
			private BufferedImage image;
			{
				try {
					image = ImageIO.read(getClass().getResourceAsStream("robot2.jpg"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			@Override
            protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				int diameter = Math.min(getWidth(), getHeight());
				int scaleFactor = 1;
				g.setColor(Color.WHITE);
				g.fillOval(0, 0, diameter, diameter);

				if (image != null) {
					Graphics2D g2d = (Graphics2D) g;
					g2d.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, diameter, diameter));

					int x = (getWidth() - diameter * scaleFactor) / 2;
					int y = (getHeight() - diameter * scaleFactor) / 2;
					g2d.drawImage(image, x, y, diameter, diameter, this);
					
				}
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 200);
            
		}
		};
		avatar.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		avatar.setBackground(new Color(8, 150, 219));
		avatar.setBounds(10, 0, 42, 41);
		titlePanel.add(avatar);
		clientJLabel = new JLabel("Client");
		clientJLabel.setBounds(0, 91, 87, 28);
		clientJLabel.setForeground(new Color(255, 255, 255));
		clientJLabel.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 20));
		controlPanel.add(clientJLabel);
		
		sendButton = new JButton("Send");
		sendButton.setBounds(745, 11, 118, 42);
		originalIcon = null;
		try {
			originalIcon = ImageIO.read(ChatClient.class.getResource("/ChatClient/SendIcon.png"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		scaledIcon = originalIcon.getScaledInstance(25 , 25, Image.SCALE_SMOOTH);
		icon = new ImageIcon(scaledIcon);
		controlPanel.add(sendButton);
		sendButton.setIcon(icon);
		sendButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		sendButton.setVerticalTextPosition(SwingConstants.CENTER);
		sendButton.setIconTextGap(10);
		sendButton.setBackground(new Color(8, 150, 229));
		sendButton.setFont(new Font("JetBrains Mono SemiBold", Font.PLAIN, 15));
		
		contentPane.setLayout(null);
		contentPane.add(titlePanel);
		contentPane.add(controlPanel);
		contentPane.add(chatTextArea);
		frame.setTitle("Chat To Server");
		frame.setSize(905, 605);
		frame.setLocationRelativeTo(null);
        // ----------------------
        
        sendButton.addActionListener(e -> sendMessage());
//        typingTextArea.addActionListener(e -> sendMessage());
        typingTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) {
                        typingTextArea.append("\n");
                    } else {
                        sendMessage();
                        e.consume();
                    }
                }
            }
        });
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                InputStream is = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(is);
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String message;
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    if (msg.startsWith("#FILE#")) {
                        receiveFile(dataInputStream, msg);
                    } else {
                        SwingUtilities.invokeLater(() -> chatTextArea.append(msg + "\n"));
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> chatTextArea.append("Mất kết nối đến server!\n"));
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        String message = typingTextArea.getText().trim();
        if (!message.isEmpty() && out != null) {
            out.println(message);
            typingTextArea.setText("");
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String target = typingTextArea.getText().trim();
                if (target.startsWith("@")) {
                    target = target.substring(1).split(":", 2)[0];
                } else {
                    target = "ALL";
                }
                out.println("#FILE#" + target);
                out.flush();

                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                dataOut.writeUTF(file.getName());
                dataOut.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dataOut.write(buffer, 0, bytesRead);
                    }
                }
                dataOut.flush();
            } catch (IOException e) {
                typingTextArea.append("Lỗi khi gửi file: " + e.getMessage() + "\n");
            }
        }
    }

    private void receiveFile(DataInputStream dataIn, String fileHeader) throws IOException {
        String sender = fileHeader.split("#")[2];
        String fileName = dataIn.readUTF();
        long fileSize = dataIn.readLong();
        byte[] fileData = new byte[(int) fileSize];
        dataIn.readFully(fileData);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile())) {
                fos.write(fileData);
            }
            SwingUtilities.invokeLater(() -> chatTextArea.append("Đã nhận file " + fileName + " từ " + sender + "\n"));
        }
    }

    public static void main(String[] args) {
        new ChatClient();
    }
}