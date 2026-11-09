package com.kelpclient.mixin;

import com.kelpclient.cosmetic.CustomAssets;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererMixin {
	/** Custom Skin: your own skin.png replaces your skin (client side only). */
	@Inject(method = "getTexture(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/Identifier;",
			at = @At("RETURN"), cancellable = true, require = 0)
	private void kelp$skin(PlayerEntityRenderState state, CallbackInfoReturnable<Identifier> cir) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || state.id != mc.player.getId()) return;
		Module m = Modules.BY_ID.get("customskin");
		if (m == null || !m.enabled) return;
		Identifier id = CustomAssets.skin();
		if (id != null) cir.setReturnValue(id);
	}
}
