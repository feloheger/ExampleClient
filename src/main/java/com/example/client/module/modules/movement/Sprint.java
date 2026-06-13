package com.example.client.module.modules.movement;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * Sprint: Hält den Spieler dauerhaft am Sprinten.
 * Nutzt ClientTickEvents – kein Mixin nötig.
 */
public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Dauerhaftes Sprinten ohne W-Doppeltippen", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        // Nur sprinten wenn der Spieler sich nach vorne bewegt
        // und nicht hungrig ist (Vanilla-Bedingung)
        if (mc.options.forwardKey.isPressed() && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
