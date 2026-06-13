package com.example.client.mixin;

import com.example.client.ExampleClientMod;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin für ClientPlayerEntity.
 *
 * Aktuell: Verhindert das Zurücksetzen von NoGravity durch
 * Vanilla-Logik, solange Flight oder Freecam aktiv ist.
 *
 * Erweiterbar für weitere Movement-Hooks.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    /**
     * Wird aufgerufen bevor der Spieler seinen Tick verarbeitet.
     * Hier können Movement-Module wie Flight ihre Velocity-Änderungen
     * anwenden, bevor Vanilla sie überschreibt.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;

        // Flight/Freecam: NoGravity-Flag absichern
        if (ExampleClientMod.flight.isEnabled() || ExampleClientMod.freecam.isEnabled()) {
            self.setNoGravity(true);
        }
    }

    /**
     * Wird aufgerufen wenn der Spieler Schaden durch Fallen nehmen würde.
     * Wenn NoFall aktiv → Schaden unterdrücken.
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;

        if (ExampleClientMod.noFall.isEnabled()) {
            self.fallDistance = 0f;
        }
    }
}
