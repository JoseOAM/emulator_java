package br.faustech.comum;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConfigFile {

    private static final String HISTORY_FILE = "configFile.txt";
    private static final int MAX_RECENT_FILES = 5;

    public void loadHistory(List<String> recentFiles, AtomicBoolean darkModeEnabled) {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            if (!jsonString.isEmpty()) {
                JSONObject json = new JSONObject(jsonString.toString());

                if (json.has("darkMode")) {
                    darkModeEnabled.set(json.getBoolean("darkMode"));
                }

                if (json.has("recentFiles")) {
                    JSONArray jsonArray = json.getJSONArray("recentFiles");
                    for (int i = 0; i < jsonArray.length() && i < MAX_RECENT_FILES; i++) {
                        recentFiles.add(jsonArray.getString(i));
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading JSON config file: " + e.getMessage());
        }
    }

    public void saveHistory(List<String> recentFiles, AtomicBoolean darkModeEnabled) {
        JSONObject json = new JSONObject();
        json.put("darkMode", darkModeEnabled.get());
        json.put("recentFiles", new JSONArray(recentFiles));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            writer.write(json.toString(4)); // Pretty print with indent
        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
        }
    }
}
