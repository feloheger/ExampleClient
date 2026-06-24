package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockESP extends Module {

    public static BlockESP INSTANCE;

    private final List<Block> activeBlocks = new ArrayList<>(List.of(
            Blocks.ANCIENT_DEBRIS, Blocks.NETHERITE_BLOCK
    ));

    public final BlockListSetting targetBlocks = addSetting(new BlockListSetting("Blocks", "Aktivierte Blöcke", activeBlocks));
    public final SliderSetting    chunkRadius  = addSetting(new SliderSetting("Radius", "Such-Radius in Chunks", 2, 1, 5));
    public final BoolSetting      showBox      = addSetting(new BoolSetting("Box",     "Wireframe-Box zeichnen",  true));
    public final BoolSetting      showFill     = addSetting(new BoolSetting("Fill",    "Gefüllte Box zeichnen",   true));
    public final BoolSetting      showTracers  = addSetting(new BoolSetting("Tracers", "Tracer-Linien zeichnen",  true));
    public final ColorSetting     color        = addSetting(new ColorSetting("Color",  "ESP-Farbe",               new Color(0, 200, 100, 255)));

    // Cache für schnelle Lookups - wird nur neu gebaut wenn sich die Liste ändert
    private Set<Block> targetsCache = new HashSet<>();
    private int lastListHash = -1;

    public BlockESP() {
        super("BlockESP", "Hebt konfigurierbare Blöcke hervor", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void addBlock(Block block) { if (!activeBlocks.contains(block)) activeBlocks.add(block); }
    public void removeBlock(Block block) { activeBlocks.remove(block); }
    public void clearBlocks() { activeBlocks.clear(); }
    public List<Block> getSelectedBlocks() { return activeBlocks; }

    /** Gibt das gecachte HashSet zurück, baut es nur neu wenn sich die Liste geändert hat */
    private Set<Block> getTargetsCached() {
        List<Block> current = targetBlocks.getValue();
        int currentHash = current.hashCode();
        if (currentHash != lastListHash) {
            targetsCache = new HashSet<>(current);
            lastListHash = currentHash;
        }
        return targetsCache;
    }

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        Set<Block> targets = getTargetsCached();
        if (targets.isEmpty()) return;

        Color c  = color.getValue();
        Color f  = new Color(c.getRed(), c.getGreen(), c.getBlue(), 25);
        int   r  = chunkRadius.getValue().intValue();
        ChunkPos cp = mc.player.getChunkPos();

        boolean fill    = showFill.getValue();
        boolean box     = showBox.getValue();
        boolean tracers = showTracers.getValue();

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int cx = -r; cx <= r; cx++) {
            for (int cz = -r; cz <= r; cz++) {
                WorldChunk chunk = mc.world.getChunk(cp.x + cx, cp.z + cz);
                if (chunk == null) continue;

                int sx = chunk.getPos().getStartX();
                int sz = chunk.getPos().getStartZ();
                int minY = mc.world.getBottomY();
                int maxHeight = minY + mc.world.getHeight();

                for (int x = sx; x < sx + 16; x++) {
                    for (int z = sz; z < sz + 16; z++) {
                        for (int y = minY; y < maxHeight; y++) {

                            mutablePos.set(x, y, z);
                            Block block = chunk.getBlockState(mutablePos).getBlock();

                            if (!targets.contains(block)) continue;

                            Box bb = new Box(mutablePos);

                            if (fill) RenderUtil.drawFilledBox(ms, vcp, bb, f);
                            if (box)  RenderUtil.drawBox(ms, vcp, bb, c);

                            if (tracers) {
                                BlockPos immutablePos = mutablePos.toImmutable();
                                RenderUtil.drawTracerToBlock(ms, vcp, immutablePos, c);
                            }
                        }
                    }
                }
            }
        }
    }
}