package com.example.client.module.modules.render;

import com.example.client.module.Module;
import com.example.client.module.settings.*;
import com.example.client.render.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import java.awt.Color;

public class StorageESP extends Module {

    public static StorageESP INSTANCE;

    public final BoolSetting  showChests   = addSetting(new BoolSetting("Chests",       "Kisten anzeigen",              true));
    public final BoolSetting  showShulkers = addSetting(new BoolSetting("Shulkers",     "Shulkerboxen anzeigen",        true));
    public final BoolSetting  showOther    = addSetting(new BoolSetting("Other",         "Barrel/Hopper/etc anzeigen",   true));
    public final BoolSetting  showBox      = addSetting(new BoolSetting("Box",           "Wireframe-Box zeichnen",       true));
    public final BoolSetting  showFill     = addSetting(new BoolSetting("Fill",          "Gefüllte Box zeichnen",        true));
    public final BoolSetting  showTracers  = addSetting(new BoolSetting("Tracers",       "Tracer-Linien zeichnen",       false));
    public final ColorSetting colorChest   = addSetting(new ColorSetting("ChestColor",   "Kisten-Farbe",                 new Color(210, 140, 40,  255)));
    public final ColorSetting colorShulker = addSetting(new ColorSetting("ShulkerColor", "Shulker-Farbe",                new Color(180, 80,  255, 255)));
    public final ColorSetting colorOther   = addSetting(new ColorSetting("OtherColor",   "Sonstige Farbe",               new Color(200, 200, 200, 200)));

    public StorageESP() {
        super("StorageESP", "Zeigt Lagerblöcke durch Wände an", Category.RENDER);
        INSTANCE = this;
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}

    public void render(MatrixStack ms, VertexConsumerProvider vcp, Vec3d camPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        // Render-Distanz aus den Optionen holen
        int renderDistance = mc.options.getClampedViewDistance();
        BlockPos playerPos = mc.player.getBlockPos();

        // Chunks im Bereich der Render-Distanz berechnen
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;

        // Wir gehen aktiv durch alle geladenen sichtbaren Chunks.
        // Das ist zu 100% zuverlässig auf Servern!
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                WorldChunk chunk = mc.world.getChunk(playerChunkX + x, playerChunkZ + z);
                if (chunk == null) continue;

                // Für jeden Chunk holen wir uns die BlockEntities
                chunk.getBlockEntities().forEach((pos, be) -> {
                    Color c = null;

                    if ((be instanceof ChestBlockEntity || be instanceof TrappedChestBlockEntity) && showChests.getValue())
                        c = colorChest.getValue();
                    else if (be instanceof ShulkerBoxBlockEntity && showShulkers.getValue())
                        c = colorShulker.getValue();
                    else if ((be instanceof BarrelBlockEntity || be instanceof HopperBlockEntity
                            || be instanceof DispenserBlockEntity || be instanceof DropperBlockEntity) && showOther.getValue())
                        c = colorOther.getValue();

                    if (c == null) return;

                    // Absolute Box um den Block herum erstellen
                    Box absoluteBox = new Box(pos);
                    Color fillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 28);

                    // Zeichnen über die korrigierte RenderUtil
                    if (showFill.getValue()) {
                        RenderUtil.drawFilledBox(ms, vcp, absoluteBox, fillColor);
                    }
                    if (showBox.getValue()) {
                        RenderUtil.drawBox(ms, vcp, absoluteBox, c);
                    }
                    if (showTracers.getValue()) {
                        RenderUtil.drawTracerToBlock(ms, vcp, pos, c);
                    }
                });
            }
        }
    }
}