package com.example.client.module.modules.world;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * AutoMine: Hält automatisch die linke Maustaste gedrückt solange
 * der Spieler auf einen Block schaut – kein Mixin nötig.
 */
public class AutoMine extends Module {

    public AutoMine() {
        super("AutoMine", "Baut automatisch angezielten Block ab", Category.WORLD);
    }

    @Override
    public void onEnable() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;

        // Attack (links) simulieren
        mc.options.attackKey.setPressed(true);
        mc.interactionManager.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
    }
}
