package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class ChunkFinder extends Module {

    public static ChunkFinder INSTANCE;

    public final SliderSetting minHeight   = addSetting(new SliderSetting("MinHeight",
            "Minimale Säulenhöhe um als verdächtig zu gelten", 5, 3, 30));
    public final SliderSetting chunkRadius = addSetting(new SliderSetting("Radius",
            "Such-Radius in Chunks", 3, 1, 6));
    public final BoolSetting   showCobble  = addSetting(new BoolSetting("Cobblestone",
            "Cobblestone-Säulen suchen", true));
    public final BoolSetting   showDeep    = addSetting(new BoolSetting("Deepslate",
            "Deepslate-Säulen suchen", true));
    public final BoolSetting   showBox     = addSetting(new BoolSetting("Box",
            "Box um Säule zeichnen", true));
    public final BoolSetting   showFill    = addSetting(new BoolSetting("Fill",
            "Box füllen", true));
    public final ColorSetting  colorCobble = addSetting(new ColorSetting("CobbleColor",
            "Farbe Cobblestone", new Color(180, 130, 90, 255)));
    public final ColorSetting  colorDeep   = addSetting(new ColorSetting("DeepColor",
            "Farbe Deepslate", new Color(100, 100, 200, 255)));

    public ChunkFinder() {
        super("ChunkFinder", "Findet versteckte Basiseingänge durch Säulenerkennung", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        // FIX: Math.round statt intValue() um Float-Rundungsfehler zu vermeiden
        // (z.B. 4.999... würde sonst zu 4 statt 5 abgeschnitten werden)
        int radius = Math.round(chunkRadius.getValue().floatValue());
        int minH   = Math.round(minHeight.getValue().floatValue());

        boolean cobbleOn = showCobble.getValue();
        boolean deepOn   = showDeep.getValue();
        boolean box      = showBox.getValue();
        boolean fill     = showFill.getValue();

        // Kein Block-Typ aktiv -> sofort raus, spart die komplette Welt-Loop
        if (!cobbleOn && !deepOn) return;

        Color cCobble = colorCobble.getValue();
        Color cDeep   = colorDeep.getValue();

        ChunkPos center = mc.player.getChunkPos();
        int worldMinY   = mc.world.getBottomY();
        int worldMaxY   = worldMinY + mc.world.getHeight();

        Set<Long> drawn = new HashSet<>();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                WorldChunk chunk = mc.world.getChunk(center.x + cx, center.z + cz);
                if (chunk == null) continue;

                int sx = chunk.getPos().getStartX();
                int sz = chunk.getPos().getStartZ();

                for (int x = sx; x < sx + 16; x++) {
                    for (int z = sz; z < sz + 16; z++) {
                        long xzKey = ((long)(x + 30000000)) << 32 | (z + 30000000);
                        if (drawn.contains(xzKey)) continue;

                        int cobbleRun = 0, cobbleTop = -1;
                        int deepRun   = 0, deepTop   = -1;

                        for (int y = worldMaxY - 1; y >= worldMinY; y--) {
                            pos.set(x, y, z);
                            Block block = chunk.getBlockState(pos).getBlock();

                            boolean isCobble = cobbleOn &&
                                    (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE);
                            boolean isDeep = deepOn &&
                                    (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE
                                            || block == Blocks.POLISHED_DEEPSLATE);

                            if (isCobble) {
                                if (cobbleRun == 0) cobbleTop = y;
                                cobbleRun++;
                            } else {
                                if (cobbleRun >= minH) {
                                    drawColumn(ms, vcp, x, cobbleTop - cobbleRun + 1, cobbleTop, z, cCobble, box, fill);
                                    drawn.add(xzKey);
                                }
                                cobbleRun = 0;
                            }

                            if (isDeep) {
                                if (deepRun == 0) deepTop = y;
                                deepRun++;
                            } else {
                                if (deepRun >= minH) {
                                    drawColumn(ms, vcp, x, deepTop - deepRun + 1, deepTop, z, cDeep, box, fill);
                                    drawn.add(xzKey);
                                }
                                deepRun = 0;
                            }
                        }

                        // Säule reicht bis zum Weltboden durch -> letzten Run noch prüfen
                        if (cobbleRun >= minH) {
                            drawColumn(ms, vcp, x, worldMinY, cobbleTop, z, cCobble, box, fill);
                            drawn.add(xzKey);
                        }
                        if (deepRun >= minH) {
                            drawColumn(ms, vcp, x, worldMinY, deepTop, z, cDeep, box, fill);
                            drawn.add(xzKey);
                        }
                    }
                }
            }
        }
    }

    private void drawColumn(MatrixStack ms, VertexConsumerProvider vcp,
                            int x, int yBottom, int yTop, int z,
                            Color c, boolean box, boolean fill) {
        Box bb = new Box(x, yBottom, z, x + 1, yTop + 1, z + 1);
        Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
        if (fill) RenderUtil.drawFilledBox(ms, vcp, bb, f);
        if (box)  RenderUtil.drawBox(ms, vcp, bb, c);
    }
}