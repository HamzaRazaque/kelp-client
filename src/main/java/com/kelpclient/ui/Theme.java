package com.kelpclient.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Theme {
	public static final int BG = 0xFF071411, PANEL = 0xFF0D211C, PANEL2 = 0xFF122B24, BORDER = 0xFF1D5545,
			ACCENT = 0xFF39E6A2, ACCENT2 = 0xFF42C7D5, SEL = 0xFF1D5545, TEXT = 0xFFE9FFF4, DIM = 0xFF86AA9C, HOVER = 0xFF18392F,
			OFF = 0xFF2B3A36, DIM_BG = 0xC0050D0B, RED = 0xFFE5484D;

	private static TextRenderer tr() { return MinecraftClient.getInstance().textRenderer; }

	public static int lerp(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int r = 0;
		for (int sh = 24; sh >= 0; sh -= 8) {
			int ca = (a >> sh) & 0xFF, cb = (b >> sh) & 0xFF;
			r |= ((int) (ca + (cb - ca) * t) & 0xFF) << sh;
		}
		return r;
	}

	public static int withAlpha(int rgb, int a) { return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF); }

	/** Slow breathing accent colour for "active" things. */
	public static int pulse() { return withAlpha(ACCENT, 170 + (int) (85 * Math.sin(System.currentTimeMillis() / 320.0))); }

	public static void box(DrawContext c, int x, int y, int w, int h, int fill, int border) {
		// chamfered corners give the soft "rounded card" look of the Kelp theme
		c.fill(x + 1, y, x + w - 1, y + h, border);
		c.fill(x, y + 1, x + w, y + h - 1, border);
		c.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
	}

	public static void outline(DrawContext c, int x, int y, int w, int h, int col) {
		c.fill(x, y, x + w, y + 1, col);
		c.fill(x, y + h - 1, x + w, y + h, col);
		c.fill(x, y, x + 1, y + h, col);
		c.fill(x + w - 1, y, x + w, y + h, col);
	}

	public static void hudBox(DrawContext c, int x, int y, int w, int h) {
		c.fill(x, y, x + w, y + h, 0x99000000);
		c.fill(x, y, x + 1, y + h, ACCENT);
	}

	public static void text(DrawContext c, String s, int x, int y, int col) { c.drawText(tr(), s, x, y, col, false); }
	public static void shadow(DrawContext c, String s, int x, int y, int col) { c.drawText(tr(), s, x, y, col, true); }
	public static void centered(DrawContext c, String s, int cx, int y, int col) { c.drawText(tr(), s, cx - tr().getWidth(s) / 2, y, col, false); }
	public static int tw(String s) { return tr().getWidth(s); }

	public static String fit(String s, int maxW) {
		if (tw(s) <= maxW) return s;
		while (s.length() > 1 && tw(s + "..") > maxW) s = s.substring(0, s.length() - 1);
		return s + "..";
	}
}
