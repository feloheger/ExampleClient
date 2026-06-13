package com.example.client.module.modules.movement;

import com.example.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * NoFall: Verhindert Fallschaden.
 *
 * Strategie: Sendet jeden Tick ein PlayerMove-Paket mit onGround=true.
 * Der Server denkt so, der Spieler landet sicher.
 */
public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Verhindert Fallschaden", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Nur senden wenn der Spieler fällt (Velocity nach unten)
        if (mc.player.fallDistance > 2.0f && mc.player.getVelocity().y < 0) {
            mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision)
            );
        }
    }
}
