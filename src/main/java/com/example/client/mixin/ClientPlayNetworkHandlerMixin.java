package com.example.client.mixin;

import com.example.client.module.modules.render.Freecam;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)  // ← Elternklasse, nicht ClientPlayNetworkHandler
public class ClientPlayNetworkHandlerMixin {

    @Inject(
            method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        Freecam freecam = Freecam.getInstance();
        if (freecam != null && freecam.shouldBlockPacket(packet)) {
            ci.cancel();
        }
    }
}