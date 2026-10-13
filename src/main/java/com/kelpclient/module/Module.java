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
	public final List<Setting> settings = new ArrayList<>();

	protected Module(String id, String name, String desc, String icon, Cat cat, Kind kind, Anchor anchor) {
		this.id = id; this.name = name; this.desc = desc; this.icon = icon;
		this.cat = cat; this.kind = kind; this.anchor = anchor;
	}

	public Module on(boolean def) { defEnabled = def; enabled = def; return this; }
	public Module set(Setting... s) { settings.addAll(Arrays.asList(s)); return this; }
	public boolean placed() { return x != UNSET && y != UNSET; }
	public void resetPos() { x = UNSET; y = UNSET; }

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

	/** Full-screen overlays (crosshair, waypoints). */
	public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) { }
}
