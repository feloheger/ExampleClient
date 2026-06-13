package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class StashFinder extends Module {

    public static StashFinder INSTANCE;

    public final SliderSetting threshold  = addSetting(new SliderSetting("MinStorage",
            "Mindestanzahl Lagerblöcke pro Chunk", 5, 1, 50));
    public final SliderSetting chunkRadius = addSetting(new SliderSetting("Radius",
            "Such-Radius in Chunks", 3, 1, 8));
    public final BoolSetting   showBox    = addSetting(new BoolSetting("Box",
            "Chunk-Umriss zeichnen", true));
    public final BoolSetting   showFill   = addSetting(new BoolSetting("Fill",
            "Chunk-Fläche füllen", true));
    public final BoolSetting   showTracer = addSetting(new BoolSetting("Tracer",
            "Linie zum Chunk zeichnen", false));
    public final ColorSetting  color      = addSetting(new ColorSetting("Color",
            "Farbe", new Color(255, 165, 0, 255)));

    // Speicher, damit wir pro Chunk nur EINMAL benachrichtigt werden
    private final Set<Long> notifiedChunks = new HashSet<>();

    public StashFinder() {
        super("StashFinder", "Findet Chunks mit vielen Lagerblöcken", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Beim Aktivieren des Moduls die alten Benachrichtigungen zurücksetzen
        notifiedChunks.clear();
    }

    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        int radius = chunkRadius.getValue().intValue();
        int min    = threshold.getValue().intValue();
        ChunkPos center = mc.player.getChunkPos();
        Color c = color.getValue();
        Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 30);

        Set<Long> drawn = new HashSet<>();

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                ChunkPos cp = new ChunkPos(center.x + cx, center.z + cz);
                long key = cp.toLong();
                if (drawn.contains(key)) continue;

                WorldChunk chunk = mc.world.getChunk(cp.x, cp.z);
                if (chunk == null) continue;

                int count = 0;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof ChestBlockEntity
                            || be instanceof TrappedChestBlockEntity
                            || be instanceof ShulkerBoxBlockEntity
                            || be instanceof BarrelBlockEntity
                            || be instanceof HopperBlockEntity
                            || be instanceof DispenserBlockEntity
                            || be instanceof DropperBlockEntity) {
                        count++;
                    }
                }

                if (count < min) continue;
                drawn.add(key);

                // ─── ADVANCEMENT TOAST LOGIK ─────────────────────────────────────────
                if (!notifiedChunks.contains(key)) {
                    notifiedChunks.add(key);

                    // Wir senden den Toast an den Minecraft ToastManager (oben rechts im Eck)
                    mc.getToastManager().add(new SystemToast(
                            SystemToast.Type.PERIODIC_NOTIFICATION, // Nutzt das saubere Standard-Layout
                            Text.literal("§6§lStashFinder"),        // Titel in Gold & Fett
                            Text.literal("§aStash Found at: §e" + cp.getCenterX() + ", " + cp.getCenterZ()) // Sub-Text mit Koordinaten
                    ));
                }
                // ─────────────────────────────────────────────────────────────────────

                int minY = mc.world.getBottomY();
                int maxY = minY + mc.world.getHeight();
                double x1 = cp.getStartX();
                double z1 = cp.getStartZ();
                double x2 = cp.getEndX() + 1;
                double z2 = cp.getEndZ() + 1;
                double y1 = minY;
                double y2 = maxY;

                Box bb = new Box(x1, y1, z1, x2, y2, z2);

                if (showFill.getValue())   RenderUtil.drawFilledBox(ms, vcp, bb, f);
                if (showBox.getValue())    RenderUtil.drawBox(ms, vcp, bb, c);
            }
        }
    }
}