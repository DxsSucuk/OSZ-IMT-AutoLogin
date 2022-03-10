package de.presti.osz.arl.main;

import de.presti.osz.arl.utils.FileUtil;
import de.presti.osz.arl.utils.LogOutputStream;

import javax.swing.*;
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
    JTextField usernameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JTextArea logArea = new JTextArea();

    static Main instance;

    public Main() {

        setLayout(null);

        actionButton.setLocation(25, 450);
        actionButton.setSize(100, 50);
        actionButton.setBounds(25, 450, 100, 50);
        actionButton.addActionListener((actionEvent) -> {
            if (checkerThread != null && checkerThread.isAlive()) {
                Main.instance.actionButton.setText("Start");
                checkerThread.interrupt();
            } else {
                Main.instance.actionButton.setText("Stop");
                startChecker(Main.instance.usernameField.getText(), String.valueOf(Main.instance.passwordField.getPassword()));
            }
        });

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

        add(passwordField);

        logArea.setEditable(false);
        logArea.setLocation(200, 25);
        logArea.setSize(575, 525);
        logArea.setBounds(200, 25, 575, 525);

        add(logArea);

        setResizable(false);
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);
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

        System.setOut(new PrintStream(new LogOutputStream(instance.logArea)));
    }

    public static void startChecker(String username, String password) {
        fileUtil.saveUserData(username, password);
        checkerThread = new Thread(() -> {
            while (checkerThread != null && !checkerThread.isInterrupted() && Main.instance.actionButton.getText().equalsIgnoreCase("Stop")) {
                if (checkerThread.isAlive()) logMeIn(username, password);
                try {
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                } catch (Exception exception) {
                    System.out.println("ERROR > Got an error please report!");
                    exception.printStackTrace();
                }
            }
        });

        checkerThread.start();
    }

    public static void logMeIn(String username, String password) {
        try {
            System.out.println("INFO > Checking for Login Requirement!");
            HttpRequest firstDataRequest = HttpRequest.newBuilder().header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59").uri(new URL("https://wlan-login.oszimt.de/logon/cgi/index.cgi").toURI()).GET().build();

            HttpResponse<String> httpResponse = httpClient.send(firstDataRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.body().contains("<h3 class=\"headline\">Ticket Anmeldung</h3>")) {

                System.out.println("INFO > Not logged in. Started Login Attempt!");

                String content = httpResponse.body();

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
                        System.out.println("SUCCESS > Login successful!");
                    } else {
                        System.out.println("ERROR > Login try failed!");
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

}
