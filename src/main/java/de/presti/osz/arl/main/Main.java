package de.presti.osz.arl.main;

import de.presti.osz.arl.utils.FileUtil;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    static HttpClient httpClient = HttpClient.newHttpClient();
    static FileUtil fileUtil = new FileUtil();
    static Thread checkerThread;

    public static void main(String[] args) {
        String[] creds = fileUtil.getUserData();

        if (creds.length > 0) {
            startChecker(creds[0], creds[1]);
            return;
        }

        if (args.length != 2) {
            System.out.println("ERROR > We did not get any Parameters! Please enter your credits!");

            Scanner scanner = new Scanner(System.in);

            System.out.println("Please enter your Username!");

            String username = scanner.next();

            System.out.println("Please enter your Password!");

            String password = scanner.next();

            startChecker(username, password);
        } else {
            startChecker(args[0], args[1]);
        }
    }

    public static void startChecker(String username, String password) {
        fileUtil.saveUserData(username, password);
        checkerThread = new Thread(() -> {
            while (true) {
                logMeIn(username, password);
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
            HttpRequest firstDataRequest = HttpRequest.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59")
                    .uri(new URL("https://wlan-login.oszimt.de/logon/cgi/index.cgi").toURI())
                    .GET().build();

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
                    HttpRequest loginRequest = HttpRequest.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59")
                            .uri(new URL("https://wlan-login.oszimt.de/logon/cgi/index.cgi").toURI())
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(ofFormData(data)).build();

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
