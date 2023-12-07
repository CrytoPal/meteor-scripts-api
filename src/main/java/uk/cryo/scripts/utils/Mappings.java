package uk.cryo.scripts.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Mappings {

    public static ArrayList<String> obfuscatedMap = new ArrayList<>();
    public static ArrayList<String> nonObfuscatedMap = new ArrayList<>();

    public static void addMappings() throws IOException {
        try {
            URL mappings = new URL("https://raw.githubusercontent.com/CrytoPal/mappings/main/mapping.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(mappings.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String colune = inputLine.trim();
                String obf = colune.split(":",99999)[1];
                String nonObf = colune.split(":",99999)[0];
                obfuscatedMap.add(obf);
                nonObfuscatedMap.add(nonObf);
            }
        } catch (Exception ignored) {
        }
    }
}
