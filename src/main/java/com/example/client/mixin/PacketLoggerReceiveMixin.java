package com.example.client.mixin;

import com.example.client.module.modules.misc.PacketLogger;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Eigenständiges Mixin für PacketLogger (S2C / eingehend).
 * Komplett unabhängig von Freecam.
 *
 * Hookt in ClientConnection#handlePacket, das jedes eingehende Paket
 * vor der Verarbeitung durch den jeweiligen Listener durchläuft.
 */
@Mixin(ClientConnection.class)
public class PacketLoggerReceiveMixin {

    @Inject(
            method = "handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onHandlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        PacketLogger logger = PacketLogger.getInstance();
        if (logger == null) return;

        logger.onReceivePacket(packet);
        if (logger.shouldBlockReceive(packet)) {
            ci.cancel();
        }
    }
}