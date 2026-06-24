package com.example.client.config;

import com.example.client.ExampleClientMod;
import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getConfigPath() {
        Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("config");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            ExampleClientMod.LOGGER.info("Konnte Config-Ordner nicht erstellen", e);
        }
        return dir.resolve("exampleclient.json");
    }

    /** Speichert alle Module + deren Settings in die Config-Datei. */
    public static void save(List<Module> modules) {
        JsonObject root = new JsonObject();

        for (Module module : modules) {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("enabled", module.isEnabled());

            JsonObject settingsJson = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                settingsJson.add(setting.getName(), settingToJson(setting));
            }
            modJson.add("settings", settingsJson);

            root.add(module.getName(), modJson);
        }

        try (Writer writer = new FileWriter(getConfigPath().toFile())) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            ExampleClientMod.LOGGER.info("Config konnte nicht gespeichert werden", e);
        }
    }

    /** Lädt alle Module + deren Settings aus der Config-Datei, falls vorhanden. */
    public static void load(List<Module> modules) {
        Path path = getConfigPath();
        if (!Files.exists(path)) return;

        JsonObject root;
        try (Reader reader = new FileReader(path.toFile())) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            ExampleClientMod.LOGGER.info("Config konnte nicht geladen werden", e);
            return;
        }

        for (Module module : modules) {
            if (!root.has(module.getName())) continue;
            JsonObject modJson = root.getAsJsonObject(module.getName());

            // enabled-Status laden, aber onEnable/onDisable nicht doppelt feuern lassen
            if (modJson.has("enabled")) {
                boolean shouldBeEnabled = modJson.get("enabled").getAsBoolean();
                if (shouldBeEnabled != module.isEnabled()) {
                    module.toggle();
                }
            }

            if (modJson.has("settings")) {
                JsonObject settingsJson = modJson.getAsJsonObject("settings");
                for (Setting<?> setting : module.getSettings()) {
                    if (settingsJson.has(setting.getName())) {
                        applyJsonToSetting(setting, settingsJson.get(setting.getName()));
                    }
                }
            }
        }
    }

    // ── Serialisierung pro Setting-Typ ──────────────────────────────────

    private static JsonElement settingToJson(Setting<?> setting) {
        if (setting instanceof BoolSetting bs) {
            return new JsonPrimitive(bs.getValue());
        }
        if (setting instanceof SliderSetting ss) {
            return new JsonPrimitive(ss.getValue());
        }
        if (setting instanceof ColorSetting cs) {
            Color c = cs.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("r", c.getRed());
            obj.addProperty("g", c.getGreen());
            obj.addProperty("b", c.getBlue());
            obj.addProperty("a", c.getAlpha());
            return obj;
        }
        if (setting instanceof BlockListSetting bls) {
            JsonArray arr = new JsonArray();
            for (Block b : bls.getValue()) {
                Identifier id = Registries.BLOCK.getId(b);
                arr.add(id.toString());
            }
            return arr;
        }
        // Fallback: einfach toString() falls ein unbekannter Setting-Typ auftaucht
        return new JsonPrimitive(String.valueOf(setting.getValue()));
    }

    @SuppressWarnings("unchecked")
    private static void applyJsonToSetting(Setting<?> setting, JsonElement json) {
        try {
            if (setting instanceof BoolSetting bs) {
                bs.setValue(json.getAsBoolean());

            } else if (setting instanceof SliderSetting ss) {
                ss.setValue(json.getAsDouble());

            } else if (setting instanceof ColorSetting cs) {
                JsonObject obj = json.getAsJsonObject();
                Color c = new Color(
                        obj.get("r").getAsInt(),
                        obj.get("g").getAsInt(),
                        obj.get("b").getAsInt(),
                        obj.get("a").getAsInt()
                );
                cs.setValue(c);

            } else if (setting instanceof BlockListSetting bls) {
                List<Block> blocks = new ArrayList<>();
                for (JsonElement el : json.getAsJsonArray()) {
                    Identifier id = Identifier.tryParse(el.getAsString());
                    if (id == null) continue;
                    Block b = Registries.BLOCK.get(id);
                    blocks.add(b);
                }
                bls.getValue().clear();
                bls.getValue().addAll(blocks);
            }
        } catch (Exception e) {
            ExampleClientMod.LOGGER.info("Setting '" + setting.getName() + "' konnte nicht geladen werden", e);
        }
    }
}