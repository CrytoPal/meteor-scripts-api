package uk.cryo.scripts.utils;

import java.util.ArrayList;

public class Mappings {

    public static ArrayList<String> obfuscatedMap = new ArrayList<String>();
    public static ArrayList<String> nonObfuscatedMap = new ArrayList<String>();

    public static void addMappings() {
        obfuscatedMap.add("class_310.method_1551()");
        nonObfuscatedMap.add("mc");
        obfuscatedMap.add("field_1724");
        nonObfuscatedMap.add("player");
        obfuscatedMap.add("net.minecraft");
        nonObfuscatedMap.add("net.minecraft.client");
        obfuscatedMap.add("class_310");
        nonObfuscatedMap.add("MinecraftClient");
        obfuscatedMap.add("net.minecraft");
        nonObfuscatedMap.add("net.minecraft.text");
        obfuscatedMap.add("class_2561.method_30163");
        nonObfuscatedMap.add("Text.of");
        obfuscatedMap.add("class_2561");
        nonObfuscatedMap.add("Text");
        obfuscatedMap.add("method_43496");
        nonObfuscatedMap.add("sendMessage");
        obfuscatedMap.add("method_5728");
        nonObfuscatedMap.add("setSprinting");
    }
}
