package com.kelpclient.mixin;

import com.kelpclient.module.Modules;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	private static final DateTimeFormatter F24 = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter F12 = DateTimeFormatter.ofPattern("hh:mm a");

	@ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
			at = @At("HEAD"), argsOnly = true, require = 0)
	private Text kelp$timestamp(Text original) {
		if (Modules.CHAT_TIMESTAMPS == null || !Modules.CHAT_TIMESTAMPS.enabled) return original;
		String t = LocalTime.now().format(Modules.CHAT_TIMESTAMPS.get("fmt") == 0 ? F24 : F12);
		return Text.literal("[" + t + "] ").formatted(net.minecraft.util.Formatting.GRAY).append(original);
	}
}
