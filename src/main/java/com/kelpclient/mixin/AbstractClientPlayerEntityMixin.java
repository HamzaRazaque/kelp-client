package com.kelpclient.mixin;

import com.kelpclient.Cosmetics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true, require = 0)
    private void kelp$skin(CallbackInfoReturnable<SkinTextures> cir) {
        if (MinecraftClient.getInstance().player == (Object) this) cir.setReturnValue(Cosmetics.apply(cir.getReturnValue()));
    }
}
