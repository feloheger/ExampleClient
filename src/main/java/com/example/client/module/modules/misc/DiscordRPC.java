package com.example.client.module.modules.misc;

import com.example.client.module.Module;
import com.google.gson.JsonObject;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zeigt in Discord per Rich Presence an, dass ExampleClient genutzt wird
 * (Server-Name, Spielzeit, etc.) – analog zur Discord-Anzeige bekannter
 * Minecraft-Clients.
 *
 * Nutzt: io.github.CDAGaming:DiscordIPC:0.10.2 (Maven Central)
 * Benötigt: eine eigene Discord Application ID (discord.com/developers/applications).
 */
public class DiscordRPC extends Module {

    private static DiscordRPC instance;
    private static final Logger LOGGER = Logger.getLogger("DiscordRPC");

    // ── HIER DEINE EIGENE DISCORD APPLICATION ID EINTRAGEN ──
    private static final String CLIENT_ID = "1519301570469170000";

    private IPCClient client;
    private long startTimestamp;
    private boolean connected = false;
    private boolean tickRegistered = false;

    /** Wie oft (in Ticks) die Presence aktualisiert wird, um Discord nicht zu spammen */
    private static final int UPDATE_INTERVAL_TICKS = 200; // ca. alle 10s bei 20 TPS
    private int tickCounter = 0;

    public DiscordRPC() {
        super("DiscordRPC", "Zeigt ExampleClient-Nutzung im Discord-Status", Category.MISC);
        instance = this;
    }

    public static DiscordRPC getInstance() {
        return instance;
    }

    // ---------------------------------------------------------------
    // MODUL EIN/AUS
    // ---------------------------------------------------------------

    @Override
    public void onEnable() {
        if (CLIENT_ID == null || CLIENT_ID.isBlank() || CLIENT_ID.equals("DEINE_APPLICATION_ID_HIER")) {
            LOGGER.log(Level.WARNING, "[DiscordRPC] Keine gültige CLIENT_ID gesetzt.");
            disableSelf();
            return;
        }

        startTimestamp = System.currentTimeMillis() / 1000L;

        client = new IPCClient(Long.parseLong(CLIENT_ID));
        client.setListener(new IPCListener() {

            @Override
            public void onReady(IPCClient ipcClient) {
                connected = true;
                LOGGER.log(Level.INFO, "[DiscordRPC] Verbunden mit Discord.");
                updatePresence();
            }

            @Override
            public void onClose(IPCClient ipcClient, JsonObject jsonObject) {
                connected = false;
                LOGGER.log(Level.INFO, "[DiscordRPC] Discord-Verbindung geschlossen.");
            }

            @Override
            public void onDisconnect(IPCClient ipcClient, Throwable throwable) {
                connected = false;
                LOGGER.log(Level.WARNING, "[DiscordRPC] Discord-Verbindung verloren.");
            }

            @Override
            public void onPacketSent(IPCClient ipcClient, Packet packet) {
                // Nicht benötigt, aber vom Interface gefordert.
            }

            @Override
            public void onPacketReceived(IPCClient ipcClient, Packet packet) {
                // Nicht benötigt, aber vom Interface gefordert.
            }

            @Override
            public void onActivityJoin(IPCClient ipcClient, String s) {
                // Nicht benötigt, aber vom Interface gefordert.
            }

            @Override
            public void onActivitySpectate(IPCClient ipcClient, String s) {
                // Nicht benötigt, aber vom Interface gefordert.
            }

            @Override
            public void onActivityJoinRequest(IPCClient ipcClient, String s, User user) {
                // Nicht benötigt, aber vom Interface gefordert.
            }
        });

        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            LOGGER.log(Level.WARNING, "[DiscordRPC] Kein laufender Discord-Client gefunden.");
            disableSelf();
            return;
        }

        if (!tickRegistered) {
            ClientTickEvents.END_CLIENT_TICK.register(this::onPresenceTick);
            tickRegistered = true;
        }
    }

    @Override
    public void onDisable() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {
            }
        }
        connected = false;
    }

    /**
     * Schaltet das Modul aus, ohne es versehentlich einzuschalten,
     * falls es bereits aus ist (toggle() würde sonst umschalten).
     */
    private void disableSelf() {
        if (isEnabled()) {
            toggle();
        }
    }

    // ---------------------------------------------------------------
    // TICK – PRESENCE PERIODISCH AKTUALISIEREN
    // ---------------------------------------------------------------

    private void onPresenceTick(net.minecraft.client.MinecraftClient mcInstance) {
        if (!isEnabled() || !connected) return;

        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL_TICKS) {
            tickCounter = 0;
            updatePresence();
        }
    }

    // ---------------------------------------------------------------
    // PRESENCE BAUEN
    // ---------------------------------------------------------------

    private void updatePresence() {
        if (client == null || !connected) return;

        String details = "Nutzt ExampleClient";
        String state;

        if (mc.player != null && mc.world != null) {
            if (mc.isInSingleplayer()) {
                state = "Singleplayer";
            } else if (mc.getCurrentServerEntry() != null) {
                state = "Server: " + mc.getCurrentServerEntry().address;
            } else {
                state = "Im Spiel";
            }
        } else {
            state = "Im Hauptmenü";
        }

        RichPresence presence = new RichPresence.Builder()
                .setDetails(details)
                .setState(state)
                .setStartTimestamp(startTimestamp)
                .setLargeImage("exampleclient_logo", "ExampleClient")
                .setSmallImage("minecraft_logo", "Minecraft")
                .build();

        try {
            client.sendRichPresence(presence);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[DiscordRPC] Konnte Presence nicht senden: " + e.getMessage());
        }
    }
}