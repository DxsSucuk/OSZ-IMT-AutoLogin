package de.presti.osz.arl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class FileUtil {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveUserData(String username, String password) {
        File directory = new File(System.getProperty("user.home") + "/osz-imt");
        if (!directory.exists()) directory.mkdirs();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));

        try {
            Files.write(new File(directory,"login.creds").toPath(), gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            System.out.println("ERROR > Couldn't save User data.");
        }
    }

    public String[] getUserData() {
        try {
            File directory = new File(System.getProperty("user.home") + "/osz-imt");
            if (!directory.exists()) directory.mkdirs();

            String read = String.join("\n", Files.readAllLines(new File(directory,"login.creds").toPath()));

            JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();

            return new String[] { jsonObject.get("username").getAsString(), new String(Base64.getDecoder().decode(jsonObject.get("password").getAsString())) };
        } catch (Exception exception) {
            System.out.println("ERROR > Couldn't get User data.");
        }

        return new String[] {};
    }
}
