package com.example.client;

import com.example.client.config.ConfigManager;
import com.example.client.module.Module;
import com.example.client.module.modules.render.*;
import com.example.client.module.modules.movement.*;
import com.example.client.module.modules.combat.*;
import com.example.client.module.modules.world.*;
import com.example.client.module.modules.gui.ClickGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
// ── KORRIGIERTE IMPORTS FÜR 1.21.11 ─────────────────────────────────────
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
// ───────────────────────────────────────────────────────────────────────
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



@Environment(EnvType.CLIENT)
public class ExampleClientMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("ExampleClient");

    // ── Render ─────────────────────────────────────────────────
    public static final PlayerESP   playerEsp   = new PlayerESP();
    public static final MobESP      mobEsp      = new MobESP();
    public static final ItemESP     itemEsp     = new ItemESP();
    public static final StorageESP  storageEsp  = new StorageESP();
    public static final CrystalESP  crystalEsp  = new CrystalESP();
    public static final VehicleESP  vehicleEsp  = new VehicleESP();
    public static final SpawnerESP  spawnerEsp  = new SpawnerESP();
    public static final BlockESP    blockEsp    = new BlockESP();
    public static final Tracers     tracers     = new Tracers();
    public static final NameTags    nameTags    = new NameTags();
    public static final FullBright  fullBright  = new FullBright();
    public static final Freecam     freecam     = new Freecam();
    public static final ChunkFinder chunkFinder = new ChunkFinder();
    public static final StashFinder stashFinder = new StashFinder();

    // ── Movement ───────────────────────────────────────────────
    public static final Sprint sprint = new Sprint();
    public static final NoFall noFall = new NoFall();
    public static final Step   step   = new Step();
    public static final Jesus  jesus  = new Jesus();
    public static final Flight flight = new Flight();

    // ── Combat ─────────────────────────────────────────────────
    public static final AutoTotem autoTotem = new AutoTotem();
    public static final AutoEat   autoEat   = new AutoEat();

    // ── World ──────────────────────────────────────────────────
    public static final AutoMine autoMine = new AutoMine();

    // Alle Module in einer Liste für die GUI
    public static final List<Module> MODULES = new ArrayList<>();

    // ClickGUI Keybind (Standard: RSHIFT)
    private static KeyBinding guiKey;
    public static KeyBinding toggleKey;


    @Override
    public void onInitializeClient() {
        LOGGER.info("[ExampleClient] Initialisierung...");
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.freecam.toggle",        // Übersetzungs-Key
                GLFW.GLFW_KEY_F,             // Standard-Taste (F)
                KeyBinding.Category.create(Identifier.of("category.freecam"))           // Kategorie in den Einstellungen
        ));
        // Module registrieren
        MODULES.add(playerEsp);
        MODULES.add(mobEsp);
        MODULES.add(itemEsp);
        MODULES.add(storageEsp);
        MODULES.add(crystalEsp);
        MODULES.add(vehicleEsp);
        MODULES.add(spawnerEsp);
        MODULES.add(blockEsp);
        MODULES.add(tracers);
        MODULES.add(nameTags);
        MODULES.add(fullBright);
        MODULES.add(freecam);
        MODULES.add(chunkFinder);
        MODULES.add(stashFinder);
        MODULES.add(sprint);
        MODULES.add(noFall);
        MODULES.add(step);
        MODULES.add(jesus);
        MODULES.add(flight);
        MODULES.add(autoTotem);
        MODULES.add(autoEat);
        MODULES.add(autoMine);

        // ── Config laden (nachdem alle Module registriert wurden) ──
        ConfigManager.load(MODULES);
        LOGGER.info("[ExampleClient] Config geladen.");

// ── Automatisches Speichern beim Schließen des Spiels ───────
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ConfigManager.save(MODULES);
            LOGGER.info("[ExampleClient] Config gespeichert (Shutdown).");
        });

// ── Manuelles Speichern per Keybind ──────────────────────────
        KeyBinding saveConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.exampleclient.saveconfig",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // Standard: kein Key, Nutzer kann selbst belegen
                KeyBinding.Category.create(Identifier.of("exampleclient", "savegui"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (saveConfigKey.wasPressed()) {
                ConfigManager.save(MODULES);
                if (mc.player != null) {
                    mc.player.sendMessage(net.minecraft.text.Text.literal("§a[ExampleClient] Config gespeichert."), false);
                }
            }
        });

        // ── World-Render-Hook ──────────────────────────────────
        WorldRenderEvents.AFTER_ENTITIES.register((WorldRenderContext context) -> {
            MatrixStack ms = context.matrices();
            VertexConsumerProvider vcp = context.consumers();
            Vec3d camPos = context.gameRenderer().getCamera().getCameraPos();

            if (ms == null || vcp == null) return;

            // ── Render-Module aufrufen ─────────────────────────
            if (tracers.isEnabled())
                tracers.render(ms, vcp, camPos);

            if (playerEsp.isEnabled())
                playerEsp.renderESP(ms, vcp, camPos);

            if (mobEsp.isEnabled())
                mobEsp.render(ms, vcp, camPos);

            if (itemEsp.isEnabled())
                itemEsp.render(ms, vcp, camPos);

            if (storageEsp.isEnabled())
                storageEsp.render(ms, vcp, camPos);

            if (crystalEsp.isEnabled())
                crystalEsp.render(ms, vcp, camPos);

            if (vehicleEsp.isEnabled())
                vehicleEsp.render(ms, vcp, camPos);

            if (spawnerEsp.isEnabled())
                spawnerEsp.render(ms, vcp, camPos);

            if (blockEsp.isEnabled())
                blockEsp.render(ms, vcp, camPos);

            if (chunkFinder.isEnabled())
                chunkFinder.render(ms, vcp, camPos);

            if (stashFinder.isEnabled())
                stashFinder.render(ms, vcp, camPos);

            if (nameTags.isEnabled())
                nameTags.render(ms, vcp, context.gameRenderer().getCamera(), camPos);
        });

        // ClickGUI Keybind registrieren
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.exampleclient.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("exampleclient", "opengui"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) {
                if (mc.currentScreen == null) {
                    mc.setScreen(new ClickGui(MODULES));
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                Freecam freecam = Freecam.getInstance();
                if (freecam != null) {
                    if (freecam.isEnabled()) {
                        freecam.onDisable();
                    } else {
                        freecam.onEnable();
                    }
                }
            }
        });

        LOGGER.info("[ExampleClient] {} Module geladen.", MODULES.size());
    }
}