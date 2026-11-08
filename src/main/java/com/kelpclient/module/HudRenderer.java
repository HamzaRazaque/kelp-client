package com.kelpclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HudRenderer {
	/** module -> {x, y, w, h} in scaled GUI pixels, rebuilt each frame. */
	public static final Map<Module, int[]> POS = new LinkedHashMap<>();

	public static void layout(MinecraftClient mc, int sw, int sh) {
		POS.clear();
		int leftY = 4, rightY = 40;
		for (Module m : Modules.ALL) {
			if (!m.enabled || m.kind != Module.Kind.HUD) continue;
			int w = m.w(mc), h = m.h(mc), x, y;
			if (m.placed()) {
				x = Math.max(0, Math.min(m.x, sw - w));
				y = Math.max(0, Math.min(m.y, sh - h));
			} else {
				switch (m.anchor) {
					case STACK_RIGHT: x = sw - w - 4; y = rightY; rightY += h + 3; break;
					case ABOVE_HOTBAR: x = (sw - w) / 2; y = sh - 72 - h; break;
					case TOP_CENTER: x = (sw - w) / 2; y = 4; break;
					case BELOW_CROSSHAIR: x = (sw - w) / 2; y = sh / 2 + 16; break;
					case BOTTOM_LEFT: x = 4; y = sh - h - 4; break;
					default: x = 4; y = leftY; leftY += h + 3;
				}
			}
			POS.put(m, new int[]{x, y, w, h});
		}
	}

	public static void render(DrawContext c) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
		int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
		boolean editor = mc.currentScreen instanceof com.kelpclient.ui.KelpMenuScreen;
		try {
			if (!editor) {
				layout(mc, sw, sh);
				for (Map.Entry<Module, int[]> e : POS.entrySet()) {
					int[] p = e.getValue();
					e.getKey().draw(c, mc, p[0], p[1], false);
				}
			}
			for (Module m : Modules.ALL) if (m.enabled && m.kind == Module.Kind.OVERLAY) m.overlay(c, mc, sw, sh);
		} catch (Throwable t) {
			// never let a HUD bug crash the game
		}
	}
}
