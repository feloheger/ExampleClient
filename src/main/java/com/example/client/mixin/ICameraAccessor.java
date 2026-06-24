package com.example.client.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface ICameraAccessor {

    @Invoker("setPos")
    void invokeSetPos(double x, double y, double z);

    // setRotation raus – nicht nötig!
}