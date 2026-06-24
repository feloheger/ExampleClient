package com.example.client.mixin;

import com.example.client.module.modules.misc.PacketLogger;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Eigenständiges Mixin für PacketLogger (C2S / ausgehend).
 * Komplett unabhängig von Freecam – nutzt nur den gleichen Hook-Punkt.
 */
@Mixin(ClientCommonNetworkHandler.class)
public class PacketLoggerSendMixin {

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketLogger logger = PacketLogger.getInstance();
        if (logger == null) return;

        logger.onSendPacket(packet);
        if (logger.shouldBlockSend(packet)) {
            ci.cancel();
        }
    }
}