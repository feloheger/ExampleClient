package com.example.client.module.modules.movement;

import com.example.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

/**
 * Step: Erhöht die Stufenhöhe, sodass der Spieler 1-Block-Stufen
 * ohne Springen erklimmen kann.
 */
public class Step extends Module {

    private static final double STEP_HEIGHT = 1.0;
    private double originalHeight = 0.6;

    public Step() {
        super("Step", "Erklimmt 1-Block-Stufen ohne Springen", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // In 1.21.11 wird die Step-Height über Attribute gesteuert
        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (attribute != null) {
            originalHeight = attribute.getBaseValue();
            attribute.setBaseValue(STEP_HEIGHT);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        if (attribute != null) {
            attribute.setBaseValue(originalHeight);
        }
    }
}