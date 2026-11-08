package com.kelpclient;

import com.kelpclient.module.Cat;
import com.kelpclient.module.HudRenderer;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import com.kelpclient.module.Tracker;
import com.kelpclient.ui.KButton;
import com.kelpclient.ui.KelpMenuScreen;
import com.kelpclient.ui.ModGridScreen;
import com.kelpclient.ui.ScreenshotsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class KelpClient implements ClientModInitializer {
	public static final String ID = "kelpclient";
	private boolean shiftWas;

	@Override
	public void onInitializeClient() {
		Modules.init();
		Config.load();
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		HudRenderCallback.EVENT.register((ctx, tickCounter) -> HudRenderer.render(ctx));
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClient() && entity instanceof LivingEntity) Tracker.onHit();
			return ActionResult.PASS;
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());

		// Screenshot viewer + mod menu buttons on the title screen and the pause menu.
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof TitleScreen || screen instanceof GameMenuScreen) {
				Screens.getButtons(screen).add(new KButton(KButton.Mode.PLAIN, 6, 6, 96, 20, "Screenshots", "camera",
						b -> client.setScreen(new ScreenshotsScreen(screen))));
				Screens.getButtons(screen).add(new KButton(KButton.Mode.PLAIN, 6, 30, 96, 20, "Kelp Mods", "kelp",
						b -> client.setScreen(new ModGridScreen(screen))));
			}
		});
	}

	private void tick(MinecraftClient mc) {
		if (mc.getWindow() == null) return;
		long win = mc.getWindow().getHandle();
		boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		if (shift && !shiftWas && mc.currentScreen == null && mc.player != null) mc.setScreen(new KelpMenuScreen());
		shiftWas = shift;

		if (mc.player == null || mc.world == null) return;
		Tracker.tick(mc);
		for (Module m : Modules.ALL) {
			if (!m.enabled) continue;
			try { m.onTick(mc); } catch (Throwable ignored) { }
		}
	}
}
