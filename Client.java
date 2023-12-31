import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.image.BufferedImage;
//import javax.swing.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.event.*;
import java.awt.*;
import com.github.sarxos.webcam.*; // for geting webcam Videos

class Client extends JFrame {
    static String IP_ADDRESS_STRING = "localhost";
    static int PORT = 4000;
    static String CURRENT_USER = "Client";
    static String PASSWORD = "1234";
    static boolean isSetupDone;
    static boolean runCam;
    static Socket videoSocket;
    static Socket audioSocket;
    static JFrame videoFrame = new JFrame();
    static final int VIDEO_HEIGHT = 320, VIDEO_WIDTH = 240;
    static Encryption enc = new Encryption();
    static Decryption dec = new Decryption();

    Socket clientSocket;
    JLabel groupName;
    JButton send, fileSend, videoStream;
    JTextField msg;
    JPanel chat;
    JScrollPane scrollPane;
    JFileChooser jfc;

    static {
        loginInterface();
    }

    private static void loginInterface() {
        Client.isSetupDone = false;

        JLabel nameLabel, portLabel, passwordLabel;
        JTextField nameTextField, ipTextField, portTextField;
        JPasswordField passwordTextField;
        JButton connect;
        JFrame frame = new JFrame();
        frame.setTitle("Login");
        nameLabel = new JLabel(" Usename   :");
        passwordLabel = new JLabel(" Password  :");
        portLabel = new JLabel("4000");
        nameTextField = new JTextField(20);
        ipTextField = new JTextField(20);
        portTextField = new JTextField(15);
        passwordTextField = new JPasswordField(20);
        connect = new JButton("Join");
        ipTextField.setText("localhost");
        portTextField.setText(PORT + "");

        Container contentPane = frame.getContentPane();
        SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
        contentPane.add(nameLabel);
        contentPane.add(nameTextField);


        contentPane.add(passwordLabel);
        contentPane.add(passwordTextField);
        contentPane.add(connect);

        layout.putConstraint(SpringLayout.WEST, nameLabel, 10, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.NORTH, nameLabel, 10, SpringLayout.NORTH, contentPane);
        layout.putConstraint(SpringLayout.WEST, nameTextField, 10, SpringLayout.EAST, nameLabel);
        layout.putConstraint(SpringLayout.NORTH, nameTextField, 10, SpringLayout.NORTH, contentPane);

        layout.putConstraint(SpringLayout.WEST, passwordLabel, 10, SpringLayout.WEST, contentPane);
        layout.putConstraint(SpringLayout.NORTH, passwordLabel, 10, SpringLayout.SOUTH, nameTextField);
        layout.putConstraint(SpringLayout.WEST, passwordTextField, 10, SpringLayout.EAST, passwordLabel);
        layout.putConstraint(SpringLayout.NORTH, passwordTextField, 10, SpringLayout.SOUTH, nameTextField);

        layout.putConstraint(SpringLayout.WEST, connect, 15, SpringLayout.EAST, passwordLabel);
        layout.putConstraint(SpringLayout.NORTH, connect, 15, SpringLayout.SOUTH, passwordTextField);

        layout.putConstraint(SpringLayout.EAST, contentPane, 15, SpringLayout.EAST, passwordTextField);
        layout.putConstraint(SpringLayout.SOUTH, contentPane, 15, SpringLayout.SOUTH, connect);

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        connect.addActionListener(
                e -> ConnectToServer(nameTextField, ipTextField, portTextField, passwordTextField, frame));
        passwordTextField.addActionListener(
                e -> ConnectToServer(nameTextField, ipTextField, portTextField, passwordTextField, frame));

    }

    private static void ConnectToServer(JTextField nameTextField, JTextField ipTextField, JTextField portTextField,
            JPasswordField passwordTextField, JFrame frame) {
        if (nameTextField.getText().toString().isBlank() || ipTextField.getText().toString().isBlank()
                || new String(passwordTextField.getPassword()).isBlank()
                || portTextField.getText().toString().isBlank()) {
            String tPass = ((new String(passwordTextField.getPassword())).isBlank()) ? " Password Field" : "";
            String tName = (nameTextField.getText().toString().isBlank()) ? "Name Field" : "";
            JOptionPane.showMessageDialog(null, tName + tPass + " cannot be Empty", "Note",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {
            CURRENT_USER = nameTextField.getText().toString();
            IP_ADDRESS_STRING = ipTextField.getText().toString();
            PORT = Integer.parseInt(portTextField.getText().toString());
            PASSWORD = new String(passwordTextField.getPassword());
            Client.isSetupDone = true;
            frame.dispose();
        }
    }

    Client() {
        super("Chat Box");
        setLayout(new BorderLayout());
        setUI();
        setSize(350, 400);
        setVisible(true);
        setDefaultCloseOperation(3);

        listeners();

    }

    private void listeners() {
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (msg.getText() == null || msg.getText().toString().trim().length() == 0) {
                    } else {
                        String content = msg.getText().toString();
                        msg.setText("");
                        DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                        dout.writeUTF(Client.CURRENT_USER + ":::" + Client.enc.encrypt(content, Client.PASSWORD));
                    }
                } catch (Exception e1) {

                    e1.printStackTrace();
                }
            }
        });
        msg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (msg.getText() == null || msg.getText().toString().trim().length() == 0) {
                    } else {
                        String content = msg.getText().toString();
                        msg.setText("");
                        DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                        dout.writeUTF(Client.CURRENT_USER + ":::" + Client.enc.encrypt(content, Client.PASSWORD));
                    }
                } catch (Exception e1) {

                    e1.printStackTrace();
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
                    dout.writeUTF("GRP_INFO" + ":::" + Client.CURRENT_USER + " left the Chat.");
                    dout.writeUTF("END");
                    ObjectOutputStream oout = new ObjectOutputStream(videoSocket.getOutputStream());
                    oout.writeObject(new ImageIcon("images\\endImage.png", "END"));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
        fileSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    jfc.showOpenDialog(null);
                    if (jfc.getSelectedFile() != null) {

                        File file = jfc.getSelectedFile();
                        FileInputStream fis = new FileInputStream(file.getPath());
                        int fileLen = (int) file.length();
                        String transferINFO = "FILE_TRANS:::" + file.getName() + ":::" + fileLen + ":::"
                                + Client.CURRENT_USER;
                        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                        dos.writeUTF(transferINFO);
                        byte b[] = new byte[fileLen];
                        fis.read(b, 0, b.length);
                        fis.close();
                        dos.write(b, 0, b.length);
                        dos.flush();
                        addMessages("GRP_INFO", "You Send A File");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        videoStream.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                Webcam cam = Webcam.getDefault();
                Client.runCam = true;
                cam.setViewSize(new Dimension(Client.VIDEO_HEIGHT, Client.VIDEO_WIDTH));
                try {
                    ImageIcon ic = null;
                    BufferedImage br = null;
                    ObjectOutputStream vstream = new ObjectOutputStream(Client.videoSocket.getOutputStream());
                    cam.open();
                    new VideoOutstreamThread(ic, br, vstream, cam).start();
                    new AudioOutStreamThread().start();

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                videoStreamStopUI();

            }
        });
    }

    void videoStreamStopUI() {
        JFrame stopFrame = new JFrame();
        stopFrame.setTitle("Pack()");
        stopFrame.setLayout(new FlowLayout());
        JButton stopButton = new JButton("Stop");
        stopFrame.add(stopButton);
        stopFrame.pack();
        stopFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        stopFrame.setLocationRelativeTo(null);
        stopFrame.setVisible(true);
        stopButton.addActionListener(ae -> {
            Client.runCam = false;
            stopFrame.dispose();
        });
        stopFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                Client.runCam = false;
                stopFrame.dispose();
            }
        });

    }

    private void setUI() {
        groupName = new JLabel("wait..");
        send = new JButton();
        fileSend = new JButton();
        videoStream = new JButton();
        videoStream.setIcon(new ImageIcon("images\\v1.png"));
        send.setIcon(new ImageIcon("images\\sendicon.png"));
        fileSend.setIcon(new ImageIcon("images\\s1.png"));
        fileSend.setToolTipText("File Transfer");
        videoStream.setToolTipText("Video Stream");
        send.setToolTipText("Send");
        msg = new JTextField(25);
        chat = new JPanel();
        scrollPane = new JScrollPane(chat);
        jfc = new JFileChooser();


        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(top, BorderLayout.NORTH);

        Border roundedBorder = new LineBorder(Color.BLACK, 2, true); 
        top.setBorder(new CompoundBorder(new EmptyBorder(5, 10, 5, 10), roundedBorder));


        top.add(groupName, BorderLayout.WEST);

        add(scrollPane, BorderLayout.CENTER);
        chat.setLayout(new BorderLayout());

        JPanel p1 = new JPanel(new BorderLayout(10, 10));
        JPanel p2 = new JPanel(new BorderLayout(10, 10));
        JPanel p3 = new JPanel(new BorderLayout(10, 10));
        add(p1, BorderLayout.SOUTH);
        p1.add(p2, BorderLayout.CENTER);
        p1.add(send, BorderLayout.EAST);
        p1.setBorder(new EmptyBorder(10, 10, 10, 10));
        p2.add(p3, BorderLayout.CENTER);
        p2.add(fileSend, BorderLayout.EAST);
        p3.add(msg, BorderLayout.CENTER);
        p3.add(videoStream, BorderLayout.EAST);


    }

    private void handleFileTransfer(String fileName, String fileLen, String sender, DataInputStream din) {
        try {
            File directory = new File("FTP Recieved");
            if (!directory.exists())
                directory.mkdir();
            int len = Integer.parseInt(fileLen);
            FileOutputStream fout = new FileOutputStream("FTP Recieved" + fileName);
            byte bytes[] = new byte[len];
            din.readFully(bytes, 0, bytes.length);
            fout.write(bytes, 0, bytes.length);
            fout.flush();
            fout.close();
            addMessages("GRP_INFO", fileName + " recieved from " + sender);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addMessages(String user, String msg) {
        Color textColor, bgColor;
        FlowLayout layout = new FlowLayout();
        JPanel row = new JPanel();
        JLabel content = new JLabel(msg);
        JLabel sender = new JLabel(user + "                        ");

        JPanel message = new RoundedPanel();

        if (user.equals("GRP_INFO")) {

            sender.setVisible(false);
            layout.setAlignment(FlowLayout.CENTER);
            textColor = new Color(200, 200, 200);
            bgColor = new Color(100, 100, 100);
        } else if (user.equals(Client.CURRENT_USER)) {
            layout.setAlignment(FlowLayout.RIGHT);
            textColor = new Color(200, 200, 200);
            bgColor = new Color(100, 100, 100);
        } else {
            layout.setAlignment(FlowLayout.LEFT);
            textColor = new Color(0, 0, 0);
            bgColor = new Color(197, 197, 197);
        }

        row.setLayout(layout);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        sender.setFont(new Font("times new roman", Font.BOLD, 11));
        content.setFont(new Font("times new roman", Font.PLAIN, 12));

        message.setBorder(new EmptyBorder(10, 10, 10, 10));

        message.setBackground(bgColor);
        sender.setForeground(textColor);
        content.setForeground(textColor);


        message.add(sender);
        message.add(content);

        row.add(message);
        chat.add(row, BorderLayout.NORTH);

        JPanel newChat = new JPanel();
        newChat.setLayout(new BorderLayout());
        chat.add(newChat, BorderLayout.CENTER);
        chat = newChat;
        chat.revalidate();

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
        JScrollBar vertica = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertica.getMaximum());

    }

   

    public static void main(String[] args) {

        while (!Client.isSetupDone) {
            System.out.print("");
        }

        Client client = new Client();

        try {
            client.clientSocket = new Socket(IP_ADDRESS_STRING, PORT);
            DataInputStream din = new DataInputStream(client.clientSocket.getInputStream());
            String groupName = din.readUTF();
            client.groupName.setText(groupName);
            DataOutputStream dout = new DataOutputStream(client.clientSocket.getOutputStream());


            String request = din.readUTF();
            if (request.startsWith("RequestSecretText")) {
                dout.writeUTF(enc.encrypt(Client.PASSWORD, Client.PASSWORD));
            } else {
                try {
                    String str = dec.decrypt(request, Client.PASSWORD);
                    if (!str.equals(Client.PASSWORD)) {
                        JOptionPane.showMessageDialog(client, "You Have entred Wrong Password", "Invalid Password",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    client.dispose();
                    JOptionPane.showMessageDialog(client, "You Have entred Wrong Password", "Invalid Password",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            new ClientVideoStreamThread().start();
            new ClientAudioStreamThread().start();
            dout.writeUTF("GRP_INFO" + ":::" + Client.CURRENT_USER + " joined");
            while (true) {
                String response = din.readUTF();
                String[] str = response.split(":::");
                if (str[0].equals("FILE_TRANS")) {
                    client.handleFileTransfer(str[1], str[2], str[3], din);
                } else if (str[0].equals("GRP_INFO"))
                    client.addMessages(str[0], str[1]);
                else
                    client.addMessages(str[0], Client.dec.decrypt(str[1], Client.PASSWORD));
            }

        } catch (java.net.ConnectException e) {
            client.groupName.setText("FAILED !");
            JOptionPane.showMessageDialog(client, "Server doesn't exist : Invalid IP Address", "Server Not Found",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (java.io.EOFException e) {
            System.out.println("Ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ClientVideoStreamThread extends Thread {
    Socket videoSocket;

    public void run() {
        try {
            videoSocket = new Socket(Client.IP_ADDRESS_STRING, Client.PORT + 1);
            Client.videoSocket = videoSocket;

            JFrame videoFrame = Client.videoFrame;
            ImageIcon ic;
            JLabel videoFeed = new JLabel();
            videoFrame.setTitle("Client :" + Client.CURRENT_USER);
            videoFrame.add(videoFeed);
            videoFrame.setVisible(false);
            videoFrame.setSize(Client.VIDEO_HEIGHT, Client.VIDEO_WIDTH);
            videoFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            videoFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    videoFrame.setVisible(false);
                }
            });
            while (true) {
                ObjectInputStream oin = new ObjectInputStream((videoSocket.getInputStream()));
                ic = (ImageIcon) oin.readObject();
                videoFeed.setIcon(ic);
                if (!videoFrame.isVisible())
                    videoFrame.setVisible(true);
                if (ic != null && ic.getDescription() != null && ic.getDescription().equals("END_VIDEO")) {
                    videoFrame.setVisible(false);
                }
            }

        } catch (java.io.EOFException e) {
            System.out.println("Ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class VideoOutstreamThread extends Thread {
    ImageIcon ic;
    BufferedImage br;
    ObjectOutputStream stream;
    Webcam cam;

    VideoOutstreamThread(ImageIcon ic, BufferedImage br, ObjectOutputStream stream, Webcam cam) {
        this.ic = ic;
        this.br = br;
        this.stream = stream;
        this.cam = cam;
    }

    public void run() {
        try {
            while (Client.runCam) {
                br = cam.getImage();
                ic = new ImageIcon(br);
                stream.writeObject(ic);
                stream.flush();
            }
            ic = new ImageIcon("images\\endVideo.png", "END_VIDEO");
            stream.writeObject(ic);
            stream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cam.close();
    }
}

class ClientAudioStreamThread extends Thread {
    Socket audioSocket;
    ObjectInputStream ois;
    AudioFormat format;
    DataLine.Info info;
    SourceDataLine speakers;
    byte[] data;

    public void run() {
        try {
            audioSocket = new Socket(Client.IP_ADDRESS_STRING, Client.PORT + 2);
            Client.audioSocket = audioSocket;
            data = new byte[1024];
            format = new AudioFormat(48000.0f, 16, 2, true, false);
            info = new DataLine.Info(SourceDataLine.class, format);
            data = new byte[1024];

            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();
            ois = new ObjectInputStream(audioSocket.getInputStream());
            while (true) {
                int dsize = ois.read(data);
                if (dsize == 1024) {
                    speakers.write(data, 0, dsize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class AudioOutStreamThread extends Thread {
    private ObjectOutputStream oos;
    private AudioFormat format;
    private DataLine.Info info;
    private TargetDataLine microphone;
    private byte[] data;
    private int dsize;

    AudioOutStreamThread() {

    }

    public void run() {
        try {
            format = new AudioFormat(48000.0f, 16, 2, true, false);
            microphone = AudioSystem.getTargetDataLine(format);
            info = new DataLine.Info(TargetDataLine.class, format);
            data = new byte[1024];

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            oos = new ObjectOutputStream(Client.audioSocket.getOutputStream());
            while (Client.runCam) {
                dsize = microphone.read(data, 0, data.length);
                oos.write(data, 0, dsize);
                oos.reset();
            }
            System.out.println("[ Client ] : Attempting to stop ");
            oos.write(data, 0, 512);
            oos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
        microphone.stop();
        microphone.close();
    }
}