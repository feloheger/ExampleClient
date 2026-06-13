package com.example.client.mixin;

import com.example.client.ExampleClientMod;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin für GameRenderer.
 *
 * Freecam: Verhindert das automatische Zurücksetzen der Kamera-Position
 * durch den Vanilla-Renderer wenn Freecam aktiv ist.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Vor dem Rendern des Frames: Wenn Freecam aktiv ist, soll die
     * Kamera nicht an die Spieler-Position gebunden werden.
     *
     * Die eigentliche Freecam-Logik läuft über ClientTickEvents in
     * Freecam.java. Dieser Mixin verhindert störende Vanilla-Overrides.
     */
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorld(CallbackInfo ci) {
        if (ExampleClientMod.freecam.isEnabled()) {
            // Kein Reset nötig – Freecam.java steuert die Position über
            // mc.player.setPosition() direkt im Tick-Callback.
            // Dieser Hook ist der Einstiegspunkt für spätere Erweiterungen
            // (z.B. separate Camera-Entity oder View-Matrix-Override).
        }
    }
}
