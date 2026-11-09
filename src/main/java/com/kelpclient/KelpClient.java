package com.kelpclient;

import com.kelpclient.cosmetic.CosmeticLayer;
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
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class KelpClient implements ClientModInitializer {
	public static final String ID = "kelpclient";
	private boolean shiftWas;
	private long lastShiftMs;
	/** Two Right Shift taps within this many milliseconds open the menu. */
	private static final long DOUBLE_TAP_MS = 380;

	@Override
	public void onInitializeClient() {
		Modules.init();
		Config.load();
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
			HudRenderer.render(ctx);
			pollMenuKey(MinecraftClient.getInstance()); // per frame, so even very quick double taps are caught
		});
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (world.isClient() && entity instanceof LivingEntity) Tracker.onHit();
			return ActionResult.PASS;
		});
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
			if (renderer instanceof PlayerEntityRenderer r) helper.register(new CosmeticLayer(r));
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());

		// Screenshot viewer + mod menu buttons on the title screen and the pause menu.
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof TitleScreen || screen instanceof GameMenuScreen) {
				KButton shots = new KButton(KButton.Mode.ICON, 6, 6, 24, 24, "", "camera", b -> client.setScreen(new ScreenshotsScreen(screen)));
				shots.setTooltip(Tooltip.of(Text.literal("Screenshots")));
				KButton mods = new KButton(KButton.Mode.ICON, 6, 34, 24, 24, "", "kelp", b -> client.setScreen(new ModGridScreen(screen)));
				mods.setTooltip(Tooltip.of(Text.literal("Kelp Mods")));
				Screens.getButtons(screen).add(shots);
				Screens.getButtons(screen).add(mods);
			}
		});
	}

	/** Double-tap Right Shift (in a world, with no screen open) to open the Kelp menu. */
	private void pollMenuKey(MinecraftClient mc) {
		if (mc == null || mc.getWindow() == null) return;
		boolean shift = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		if (shift && !shiftWas && mc.currentScreen == null && mc.player != null) {
			long now = System.currentTimeMillis();
			if (now - lastShiftMs <= DOUBLE_TAP_MS) {
				lastShiftMs = 0;
				mc.execute(() -> { if (mc.currentScreen == null) mc.setScreen(new KelpMenuScreen()); });
			} else {
				lastShiftMs = now;
			}
		}
		shiftWas = shift;
	}

	private void tick(MinecraftClient mc) {
		pollMenuKey(mc); // fallback for when the HUD is hidden (F1)
		if (mc.player == null || mc.world == null) return;
		Tracker.tick(mc);
		for (Module m : Modules.ALL) {
			if (!m.enabled) continue;
			try { m.onTick(mc); } catch (Throwable ignored) { }
		}
	}
}
