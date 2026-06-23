package com.example.client.mixin;

import com.example.client.module.modules.render.Freecam;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.html.parser.Entity;

@Mixin(Camera.class)
public class CameraSubmersionMixin {

    @Inject(
            method = "getSubmersionType",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
        Freecam freecam = Freecam.getInstance();
        if (freecam != null && freecam.isEnabled()) {
            cir.setReturnValue(CameraSubmersionType.NONE);
        }
    }
}