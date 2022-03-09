package de.presti.osz.arl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.file.Files;

public class FileUtil {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveUserData(String username, String password) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);

        try {
            Files.writeString(new File("login.creds").toPath(), gson.toJson(jsonObject));
        } catch (Exception exception) {
            System.out.println("ERROR > Couldn't save User data.");
        }
    }

    public String[] getUserData() {
        try {
            String read = Files.readString(new File("login.creds").toPath());

            JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();

            return new String[] { jsonObject.get("username").getAsString(), jsonObject.get("password").getAsString() };
        } catch (Exception exception) {
            System.out.println("ERROR > Couldn't get User data.");
        }

        return new String[] {};
    }
}
