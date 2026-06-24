package com.example.client.module.modules.misc;

import com.example.client.module.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketLogger extends Module {

    // ---------------------------------------------------------------
    // LOKALER ZUSTAND
    // ---------------------------------------------------------------

    private static PacketLogger instance;

    private static final Logger LOGGER = Logger.getLogger("PacketLogger");

    /**
     * Pakettypen, die beim Empfang (S2C) geloggt werden sollen.
     * Hier einfach die gewünschten Packet-Klassen eintragen.
     */
    private final Set<Class<? extends Packet>> s2cPacketsToLog = new HashSet<>();

    /**
     * Pakettypen, die beim Senden (C2S) geloggt werden sollen.
     */
    private final Set<Class<? extends Packet>> c2sPacketsToLog = new HashSet<>();

    /** Ob nur die Klasse oder zusätzlich der toString()-Inhalt geloggt wird */
    private boolean verbose = false;

    /**
     * Pakettypen, die beim Empfang (S2C) zusätzlich BLOCKIERT werden sollen.
     * Nur zum Debuggen gedacht (z. B. testen wie der Client ohne ein
     * bestimmtes Paket reagiert).
     */
    private final Set<Class<? extends Packet>> s2cPacketsToBlock = new HashSet<>();

    /**
     * Pakettypen, die beim Senden (C2S) zusätzlich BLOCKIERT werden sollen.
     */
    private final Set<Class<? extends Packet>> c2sPacketsToBlock = new HashSet<>();

    // ---------------------------------------------------------------
    // KONSTRUKTOR
    // ---------------------------------------------------------------

    public PacketLogger() {
        super("PacketLogger", "Bestimmte Pakete zum Debuggen loggen", Category.MISC);
        instance = this;
        s2cPacketsToBlock.add(ChunkDeltaUpdateS2CPacket.class);
        // Beispiel-Einträge – hier eigene Packet-Klassen eintragen:
        // s2cPacketsToLog.add(ChunkDeltaUpdateS2CPacket.class);
        // c2sPacketsToLog.add(PlayerMoveC2SPacket.class);
        //
        // s2cPacketsToBlock.add(ChunkDeltaUpdateS2CPacket.class);
        // c2sPacketsToBlock.add(PlayerMoveC2SPacket.class);
    }

    public static PacketLogger getInstance() {
        return instance;
    }

    // ---------------------------------------------------------------
    // MODUL EIN/AUS
    // ---------------------------------------------------------------

    @Override
    public void onEnable() {
        LOGGER.log(Level.INFO, "[PacketLogger] aktiviert – beobachtete Typen: "
                + s2cPacketsToLog.size() + " S2C, " + c2sPacketsToLog.size() + " C2S");
    }

    @Override
    public void onDisable() {
        LOGGER.log(Level.INFO, "[PacketLogger] deaktiviert");
    }

    // ---------------------------------------------------------------
    // EINSTELLUNGEN (LAUFZEIT)
    // ---------------------------------------------------------------

    public void addS2CPacket(Class<? extends Packet> packetClass) {
        s2cPacketsToLog.add(packetClass);
    }

    public void addC2SPacket(Class<? extends Packet> packetClass) {
        c2sPacketsToLog.add(packetClass);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** Trägt einen S2C-Pakettyp ein, der ab jetzt blockiert wird. */
    public void addS2CBlock(Class<? extends Packet> packetClass) {
        s2cPacketsToBlock.add(packetClass);
    }

    /** Trägt einen C2S-Pakettyp ein, der ab jetzt blockiert wird. */
    public void addC2SBlock(Class<? extends Packet> packetClass) {
        c2sPacketsToBlock.add(packetClass);
    }

    public void removeS2CBlock(Class<? extends Packet> packetClass) {
        s2cPacketsToBlock.remove(packetClass);
    }

    public void removeC2SBlock(Class<? extends Packet> packetClass) {
        c2sPacketsToBlock.remove(packetClass);
    }

    // ---------------------------------------------------------------
    // PAKET-HOOKS
    // ---------------------------------------------------------------

    /**
     * Vom Netzwerk-Mixin beim Empfang eines Pakets aufzurufen.
     * Loggt das Paket, blockiert es aber NICHT (nur Beobachtung).
     */
    public void onReceivePacket(Packet packet) {
        if (!isEnabled()) return;
        if (s2cPacketsToLog.contains(packet.getClass())) {
            logPacket("S2C", packet);
        }
    }

    /**
     * Vom Netzwerk-Mixin beim Senden eines Pakets aufzurufen.
     * Loggt das Paket, blockiert es aber NICHT (nur Beobachtung).
     */
    public void onSendPacket(Packet packet) {
        if (!isEnabled()) return;
        if (c2sPacketsToLog.contains(packet.getClass())) {
            logPacket("C2S", packet);
        }
    }

    /**
     * Vom Netzwerk-Mixin beim Empfang eines Pakets aufzurufen, um zu prüfen,
     * ob es verworfen werden soll. Loggt den Block zusätzlich.
     * Beispiel-Aufruf im Mixin:
     *   if (PacketLogger.getInstance().shouldBlockReceive(packet)) {
     *       ci.cancel();
     *       return;
     *   }
     */
    public boolean shouldBlockReceive(Packet packet) {
        if (!isEnabled()) return false;
        boolean block = s2cPacketsToBlock.contains(packet.getClass());
        if (block) {
            logPacket("S2C-BLOCKED", packet);
        }
        return block;
    }

    /**
     * Vom Netzwerk-Mixin beim Senden eines Pakets aufzurufen, um zu prüfen,
     * ob es verworfen werden soll. Loggt den Block zusätzlich.
     * Beispiel-Aufruf im Mixin:
     *   if (PacketLogger.getInstance().shouldBlockSend(packet)) {
     *       ci.cancel();
     *       return;
     *   }
     */
    public boolean shouldBlockSend(Packet packet) {
        if (!isEnabled()) return false;
        boolean block = c2sPacketsToBlock.contains(packet.getClass());
        if (block) {
            logPacket("C2S-BLOCKED", packet);
        }
        return block;
    }

    private void logPacket(String direction, Packet packet) {
        if (verbose) {
            LOGGER.log(Level.INFO, "[PacketLogger][" + direction + "] " + packet);
        } else {
            LOGGER.log(Level.INFO, "[PacketLogger][" + direction + "] " + packet.getClass().getSimpleName());
        }
    }
}