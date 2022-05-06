package de.presti.osz.arl.main;

import de.presti.osz.arl.utils.FileUtil;
import de.presti.osz.arl.utils.LogOutputStream;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.synth.SynthLookAndFeel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Main extends JFrame {

    JButton actionButton = new JButton("Start");
    JButton switchButton = new JButton("W");
    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JTextArea logArea = new JTextArea();

    final PopupMenu popupMenu = new PopupMenu();
    final SystemTray systemTray = SystemTray.getSystemTray();

    MenuItem showItem = new MenuItem("Show GUI");
    MenuItem menuItem = new MenuItem("Current Application State:");
    MenuItem stateItem = new MenuItem("Offline");
    MenuItem exitItem = new MenuItem("Exit");

    TrayIcon trayIcon;

    static Main instance;

    public Main() {
        if (trayIcon == null) {
            try {
                ImageIcon imageIcon = new ImageIcon(new URL("https://findicons.com/files/icons/2831/mono_business_2/256/thumbs_up.png"));
                trayIcon = new TrayIcon(imageIcon.getImage(), "On God?");
                trayIcon.setImageAutoSize(true);
            } catch (Exception ignored) {}
        }

        try {
            UIManager.setLookAndFeel(new SynthLookAndFeel());
            UIManager.put("background", new Color(0x121212));
            UIManager.put("info", new Color(0x121212));
            UIManager.put("nimbusBase", new Color(18, 30, 49));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
            UIManager.put("text", new Color(230, 230, 230));
            this.setTitle("OSZ-IMT AutoReLogin");
            this.setIconImage(Toolkit.getDefaultToolkit().createImage(new URL("https://cdn-icons-png.flaticon.com/512/25/25297.png")));
            /* Turn off metal's use of bold fonts */
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {
        }
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                setExtendedState(JFrame.ICONIFIED);
            }
        });

        updateTrayIcon(true);
        try {
            systemTray.add(trayIcon);
        } catch (Exception ignored) {
        }
    }

    public void updateTrayIcon(boolean creation) {
        stateItem.setLabel(checkerThread != null && checkerThread.isAlive() && !checkerThread.isInterrupted() ? "Running" : "Idle");
        if (creation) {
            popupMenu.add(showItem);
            popupMenu.addSeparator();
            popupMenu.add(menuItem);
            popupMenu.add(stateItem);
            popupMenu.addSeparator();
            popupMenu.add(exitItem);
            trayIcon.setPopupMenu(popupMenu);
            exitItem.addActionListener(listener -> {
                System.exit(-1);
            });
            showItem.addActionListener(listener -> {
                showUI();
            });
        }
    }

    public void showUI() {
        setLayout(null);

        actionButton.setLocation(25, 450);
        actionButton.setSize(100, 50);
        actionButton.setBounds(25, 450, 100, 50);
        actionButton.setBorder(new RoundedBorder(10));
        actionButton.setFocusPainted(false);
        actionButton.addActionListener((actionEvent) -> {
            startChecker(Main.instance.usernameField.getText(), String.valueOf(Main.instance.passwordField.getPassword()));
        });

        switchButton.setLocation(130, 450);
        switchButton.setSize(60, 50);
        switchButton.setBounds(130, 450, 60, 50);
        switchButton.setFocusPainted(false);
        switchButton.setBorder(new RoundedBorder(20));
        switchButton.addActionListener((actionEvent) -> {
            if (switchButton.getText().equalsIgnoreCase("w")) {
                switchButton.setText("D");
                useLightMode();
            } else {
                switchButton.setText("W");
                useDarkMode();
            }
        });

        add(switchButton);

        add(actionButton);

        JLabel jlabel = new JLabel("Username:");
        jlabel.setLocation(25, 25);
        jlabel.setSize(150, 25);
        jlabel.setBounds(25, 25, 150, 25);

        add(jlabel);

        usernameField.setLocation(25, 50);
        usernameField.setSize(150, 25);
        usernameField.setBounds(25, 50, 150, 25);

        add(usernameField);

        JLabel jLabel1 = new JLabel("Password:");
        jLabel1.setLocation(25, 75);
        jLabel1.setSize(150, 25);
        jLabel1.setBounds(25, 75, 150, 25);

        add(jLabel1);

        passwordField.setLocation(25, 100);
        passwordField.setSize(150, 25);
        passwordField.setBounds(25, 100, 150, 25);
        passwordField.setBackground(Color.DARK_GRAY.darker());
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setForeground(Color.WHITE);

        add(passwordField);

        logArea.setEditable(false);
        logArea.setLocation(200, 25);
        logArea.setSize(575, 525);
        logArea.setBounds(200, 25, 575, 525);
        logArea.setAutoscrolls(true);
        add(logArea);

        setResizable(false);
        setSize(800, 600);

        setVisible(true);

        useDarkMode();
    }

    static HttpClient httpClient = HttpClient.newHttpClient();
    static FileUtil fileUtil = new FileUtil();
    static Thread checkerThread;

    public static void main(String[] args) {
        instance = new Main();
        if (fileUtil.getUserData().length > 0) {
            instance.usernameField.setText(fileUtil.getUserData()[0]);
            instance.passwordField.setText(fileUtil.getUserData()[1]);
        }

        if ((args.length >= 1 && !args[0].equalsIgnoreCase("silent")) || args.length == 0) instance.showUI();

        if (args.length >= 2 && args[1].equalsIgnoreCase("autorun"))
            startChecker(instance.usernameField.getText(), new String(instance.passwordField.getPassword()));

        System.setOut(new PrintStream(new LogOutputStream(instance.logArea)));
        System.setErr(new PrintStream(new LogOutputStream(instance.logArea)));
    }

    public static void startChecker(String username, String password) {
        fileUtil.saveUserData(username, password);
        if (checkerThread != null && checkerThread.isAlive()) {
            Main.instance.actionButton.setText("Start");
            checkerThread.interrupt();
            instance.clearLogs();
        } else {
            Main.instance.actionButton.setText("Stop");
            checkerThread = new Thread(() -> {
                while (checkerThread != null && !checkerThread.isInterrupted() && Main.instance.actionButton.getText().equalsIgnoreCase("Stop")) {
                    if (checkerThread.isAlive()) {
                        logMeIn(username, password);
                    }
                    try {
                        Thread.sleep(Duration.ofSeconds(10).toMillis());
                    } catch (Exception exception) {
                        if (!(exception instanceof InterruptedException)) {
                            System.out.println("ERROR > Got an error please report!");
                            exception.printStackTrace();
                        }
                    }
                }
            });
            checkerThread.start();
        }

        instance.updateTrayIcon(false);
    }

    public static void logMeIn(String username, String password) {
        try {
            System.out.println("INFO > Checking for Login Requirement!");
            HttpRequest firstDataRequest = HttpRequest.newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59").uri(new URL("https://wlan-login.oszimt.de/logon/cgi/index.cgi").toURI()).GET().build();

            HttpResponse<String> httpResponse = httpClient.send(firstDataRequest, HttpResponse.BodyHandlers.ofString());

            String content = httpResponse.body();

            if (content.contains("<h3 class=\"headline\">Ticket Anmeldung</h3>")) {

                System.out.println("INFO > Not logged in. Started Login Attempt!");

                long startTime = System.currentTimeMillis();

                if (content.contains("name=\"ta_id\" value=\"")) {
                    Map<Object, Object> data = new HashMap<>();
                    data.put("ta_id", httpResponse.body().split("name=\"ta_id\" value=\"")[1].split("\"")[0]);
                    data.put("uid", username);
                    data.put("pwd", password);
                    data.put("device_infos", "1032:1920:1080:1920");
                    data.put("voucher_logon_btn", "Login");
                    HttpRequest loginRequest = HttpRequest.newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59").uri(new URL("https://wlan-login.oszimt.de/logon/cgi/index.cgi").toURI()).header("Content-Type", "application/x-www-form-urlencoded").POST(ofFormData(data)).build();

                    HttpResponse<String> httpLoginResponse = httpClient.send(loginRequest, HttpResponse.BodyHandlers.ofString());

                    if (httpLoginResponse.body().contains("<label class=\"ewc_s_label\"><span class=\"logged-in\">angemeldet</span></label>")) {
                        System.out.println("SUCCESS > Login successful! (" + (System.currentTimeMillis() - startTime) + "ms)");
                    } else {
                        System.out.println("ERROR > Login try failed! (" + (System.currentTimeMillis() - startTime) + "ms)");
                    }
                } else {
                    System.out.println("ERROR > Got an Invalid TID, will skip this try!");
                }
            } else {
                System.out.println("ERROR > You are logged in! No need for logging in again!");
            }
        } catch (Exception exception) {
            System.out.println("ERROR > Got an error please report!");
            exception.printStackTrace();
        }
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = Main.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public void useDarkMode() {
        this.setBackground(new Color(0x121212));
        this.setForeground(Color.WHITE);

        usernameField.setBackground(Color.DARK_GRAY.darker());
        usernameField.setBorder(BorderFactory.createEmptyBorder());
        usernameField.setForeground(Color.WHITE);

        passwordField.setBackground(Color.DARK_GRAY.darker());
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setForeground(Color.WHITE);

        logArea.setForeground(Color.GREEN.darker());
        logArea.setBackground(Color.DARK_GRAY.darker());
        logArea.setBorder(BorderFactory.createEmptyBorder());

        actionButton.setBackground(Color.DARK_GRAY.darker());
        actionButton.setForeground(Color.WHITE);

        switchButton.setBackground(Color.DARK_GRAY.darker());
        switchButton.setForeground(Color.WHITE);
    }

    public void useLightMode() {
        this.setBackground(Color.WHITE);
        this.setForeground(Color.BLACK);

        usernameField.setBackground(Color.GRAY.brighter());
        usernameField.setBorder(BorderFactory.createEmptyBorder());
        usernameField.setForeground(Color.BLACK);

        passwordField.setBackground(Color.GRAY.brighter());
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setForeground(Color.BLACK);

        logArea.setForeground(Color.gray);
        logArea.setBackground(Color.gray.brighter());
        logArea.setBorder(BorderFactory.createEmptyBorder());

        actionButton.setBackground(Color.gray.brighter());
        actionButton.setForeground(Color.BLACK);

        switchButton.setBackground(Color.gray.brighter());
        switchButton.setForeground(Color.BLACK);
    }

    public void clearLogs() {
        logArea.setText("");
    }
}
