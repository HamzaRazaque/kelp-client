package com.kelpclient.mixin;

import com.kelpclient.module.Modules;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	/** Hide the vanilla crosshair while the Kelp custom crosshair is on. */
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
	private void kelp$hideCrosshair(CallbackInfo ci) {
		if (Modules.CROSSHAIR != null && Modules.CROSSHAIR.enabled) ci.cancel();
	}
}
