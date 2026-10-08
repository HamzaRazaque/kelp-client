package com.kelpclient.mixin;

import com.kelpclient.KelpTag;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void kelp$tag(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (KelpTag.isKelp(entry.getProfile().id())) cir.setReturnValue(KelpTag.prefix(cir.getReturnValue()));
    }
}
