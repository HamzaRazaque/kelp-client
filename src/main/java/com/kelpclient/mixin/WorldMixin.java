package com.kelpclient.mixin;

import com.kelpclient.module.Extras;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
	/** Custom Time: only the client's world reports the overridden time of day. */
	@Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true, require = 0)
	private void kelp$time(CallbackInfoReturnable<Long> cir) {
		long t = Extras.timeOverride();
		if (t >= 0 && (Object) this instanceof ClientWorld) cir.setReturnValue(t);
	}
}
