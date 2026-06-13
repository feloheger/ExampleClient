package com.example.client.module.modules.movement;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Jesus: Lässt den Spieler auf Wasser laufen.
 *
 * Strategie: Wenn der Spieler im Wasser ist und keine Aufwärtsbewegung
 * eingibt, wird ein leichter Aufwärtsimpuls gesetzt um ihn auf der
 * Wasseroberfläche zu halten. Setzt Velocity.y auf einen kleinen
 * positiven Wert um Sinken zu verhindern.
 */
public class Jesus extends Module {

    public Jesus() {
        super("Jesus", "Laufen auf Wasser", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        if (mc.player.isTouchingWater() && !mc.player.isSwimming()) {
            BlockPos below = mc.player.getBlockPos().down();
            boolean waterBelow = mc.world != null &&
                (mc.world.getBlockState(below).getBlock() == Blocks.WATER ||
                 mc.world.getFluidState(below).isEmpty());

            if (waterBelow) {
                // Nach oben drücken wenn nicht Sneak-Taste
                if (!mc.options.sneakKey.isPressed()) {
                    double vy = mc.player.getVelocity().y;
                    if (vy < 0.1) {
                        mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            0.1,
                            mc.player.getVelocity().z
                        );
                    }
                }
            }
        }
    }
}
