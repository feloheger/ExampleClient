package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import java.awt.Color;

public class SpawnerESP extends Module {

    public static SpawnerESP INSTANCE;

    public final SliderSetting    chunkRadius  = addSetting(new SliderSetting("Radius", "Such-Radius in Chunks", 4, 1, 8));
    public final BoolSetting  showBox     = addSetting(new BoolSetting("Box",     "Wireframe-Box zeichnen",  true));
    public final BoolSetting  showFill    = addSetting(new BoolSetting("Fill",    "Gefüllte Box zeichnen",   true));
    public final BoolSetting  showTracers = addSetting(new BoolSetting("Tracers", "Tracer-Linien zeichnen",  true));
    public final ColorSetting color       = addSetting(new ColorSetting("Color",  "ESP-Farbe",               new Color(255, 80, 80, 255)));

    public SpawnerESP() {
        super("SpawnerESP", "Zeigt Mob-Spawner durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Color c = color.getValue();
        Color f = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
        int radius = chunkRadius.getValue().intValue();
        ChunkPos center = mc.player.getChunkPos();

        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                WorldChunk chunk = mc.world.getChunk(center.x + cx, center.z + cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof MobSpawnerBlockEntity)) continue;

                    BlockPos pos = be.getPos();
                    Box bb = new Box(pos);

                    // Boxen zeichnen
                    if (showFill.getValue())    RenderUtil.drawFilledBox(ms, vcp, bb, f);
                    if (showBox.getValue())     RenderUtil.drawBox(ms, vcp, bb, c);

                    // FIX: Tracer-Linie direkt zum BlockPos des Spawners zeichnen!
                    if (showTracers.getValue()) {
                        RenderUtil.drawTracerToBlock(ms, vcp, pos, c);
                    }
                }
            }
        }
    }
}