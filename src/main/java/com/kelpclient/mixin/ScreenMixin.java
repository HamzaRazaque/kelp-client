package com.kelpclient.mixin;

import com.kelpclient.ui.MenuBackground;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow public int width;
	@Shadow public int height;

	/** Replace the vanilla panorama (menus outside a world) with the Kelp background. */
	@Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true, require = 0)
	private void kelp$background(DrawContext context, float delta, CallbackInfo ci) {
		MenuBackground.draw(context, width, height);
		ci.cancel();
	}
}
