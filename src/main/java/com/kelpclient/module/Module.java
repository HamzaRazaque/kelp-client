package com.kelpclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Module {
	public enum Kind { HUD, OVERLAY, LOGIC }
	public enum Anchor { STACK_LEFT, STACK_RIGHT, ABOVE_HOTBAR, TOP_CENTER, BELOW_CROSSHAIR, BOTTOM_LEFT }

	public static final int UNSET = Integer.MIN_VALUE;

	public final String id, name, desc, icon;
	public final Cat cat;
	public final Kind kind;
	public final Anchor anchor;
	public boolean enabled, defEnabled;
	public int x = UNSET, y = UNSET;
	/** GLFW key that toggles this module, -1 = none (default). */
	public int key = -1;
	/** HUD size in percent (50..200). */
	public int scale = 100;
	public final List<Setting> settings = new ArrayList<>();

	protected Module(String id, String name, String desc, String icon, Cat cat, Kind kind, Anchor anchor) {
		this.id = id; this.name = name; this.desc = desc; this.icon = icon;
		this.cat = cat; this.kind = kind; this.anchor = anchor;
	}

	public Module on(boolean def) { defEnabled = def; enabled = def; return this; }
	public Module set(Setting... s) { settings.addAll(Arrays.asList(s)); return this; }
	public boolean placed() { return x != UNSET && y != UNSET; }
	public void resetPos() { x = UNSET; y = UNSET; }

	/** Size on screen after the user's HUD scale. */
	public final int sw(MinecraftClient mc) { return Math.max(1, w(mc) * scale / 100); }
	public final int sh(MinecraftClient mc) { return Math.max(1, h(mc) * scale / 100); }

	/** Draws the HUD box at (x, y) using the user's scale. */
	public final void drawScaled(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
		if (scale == 100) { draw(c, mc, x, y, preview); return; }
		float s = scale / 100f;
		c.getMatrices().pushMatrix();
		c.getMatrices().translate(x, y);
		c.getMatrices().scale(s, s);
		try { draw(c, mc, 0, 0, preview); } finally { c.getMatrices().popMatrix(); }
	}

	public int get(String key) {
		for (Setting s : settings) if (s.key.equals(key)) return s.v;
		return 0;
	}

	public void setEnabled(boolean b) {
		if (b == enabled) return;
		enabled = b;
		MinecraftClient mc = MinecraftClient.getInstance();
		try { if (b) onEnable(mc); else onDisable(mc); } catch (Throwable ignored) { }
	}

	public void onTick(MinecraftClient mc) { }
	public void onEnable(MinecraftClient mc) { }
	public void onDisable(MinecraftClient mc) { }

	/** HUD boxes: size and drawing. */
	public int w(MinecraftClient mc) { return 0; }
	public int h(MinecraftClient mc) { return 0; }
	public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) { }

	/** Full-screen overlays (crosshair, waypoints, sky, fog, blur). */
	public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) { }
}
