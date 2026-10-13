package com.kelpclient.module;

import com.kelpclient.Config;
import com.kelpclient.module.Module.Anchor;
import com.kelpclient.module.Module.Kind;
import com.kelpclient.ui.Icons;
import com.kelpclient.ui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class Modules {
	public static final List<Module> ALL = new ArrayList<>();
	public static final Map<String, Module> BY_ID = new HashMap<>();

	public static Module CROSSHAIR, CHAT_TIMESTAMPS;
	private static boolean inited;

	private static Module add(Module m) { ALL.add(m); BY_ID.put(m.id, m); return m; }
	private static List<String> L(String... s) { return Arrays.asList(s); }

	public static final String[] PRESET_NAMES = {"Minimal", "PvP", "Survival", "Everything"};

	public static void applyPreset(int i) {
		for (Module m : ALL) m.setEnabled(false);
		String[] ids;
		switch (i) {
			case 0: ids = new String[]{"fps", "coords", "combo"}; break;
			case 1: ids = new String[]{"fps", "ping", "cps", "keystrokes", "armor", "potions", "combo", "health", "fullbright", "nohurt", "sprint", "crosshair"}; break;
			case 2: ids = new String[]{"fps", "coords", "direction", "biome", "clock", "day", "armor", "saturation", "waypoints", "fullbright"}; break;
			default: { List<String> all = new ArrayList<>(); for (Module m : ALL) all.add(m.id); ids = all.toArray(new String[0]); }
		}
		for (String id : ids) { Module m = BY_ID.get(id); if (m != null) m.setEnabled(true); }
		Config.save();
	}

	public static void resetAll() {
		for (Module m : ALL) {
			m.setEnabled(m.defEnabled);
			m.resetPos();
			for (Setting s : m.settings) s.v = s.def;
		}
		Config.save();
	}

	public static void init() {
		if (inited) return;
		inited = true;

		// ---------------- HUD ----------------
		add(new TextMod("fps", "FPS", "Frames per second", "monitor", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> L("FPS: " + mc.getCurrentFps()))).on(true);

		add(new TextMod("coords", "Coordinates", "Your X / Y / Z position", "cross", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					String f = "%." + m.get("dec") + "f";
					ClientPlayerEntity p = mc.player;
					return L("XYZ: " + String.format(f, p.getX()) + " / " + String.format(f, p.getY()) + " / " + String.format(f, p.getZ()));
				})).set(Setting.cycle("dec", "Decimals", 0, "0", "1", "2"));

		add(new TextMod("direction", "Direction", "Compass direction you face", "compass", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					String[] d = {"South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East"};
					float yaw = MathHelper.wrapDegrees(mc.player.getYaw());
					int i = Math.floorMod((int) Math.floor((yaw + 22.5) / 45.0), 8);
					return L("Facing: " + d[i] + " (" + (int) yaw + ")");
				}));

		add(new TextMod("clock", "Clock", "Real time of day", "clock", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> L(LocalTime.now().format(DateTimeFormatter.ofPattern(m.get("fmt") == 0 ? "HH:mm" : "hh:mm a")))))
				.set(Setting.cycle("fmt", "Format", 0, "24 hour", "12 hour"));

		add(new TextMod("memory", "Memory Usage", "Java memory in use", "chip", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					Runtime r = Runtime.getRuntime();
					long used = (r.totalMemory() - r.freeMemory()) / 1048576L, max = r.maxMemory() / 1048576L;
					return L("Mem: " + used + " / " + max + " MB");
				}));

		add(new TextMod("biome", "Biome", "Biome you are standing in", "tree", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					String b = mc.world.getBiome(mc.player.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("unknown");
					return L("Biome: " + b.replace('_', ' '));
				}));

		add(new TextMod("server", "Server Info", "Address of the server you are on", "monitor", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					net.minecraft.client.network.ServerInfo si = mc.getCurrentServerEntry();
					return L(si == null ? "Singleplayer" : "Server: " + si.address);
				}));

		add(new TextMod("ping", "Ping", "Your latency to the server", "bars", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					PlayerListEntry e = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
					return L("Ping: " + (e == null ? "-" : e.getLatency() + " ms"));
				}));

		add(new TextMod("day", "Day Counter", "Days since the world began", "sun", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> L("Day " + (mc.world.getTime() / 24000L + 1))));

		add(new TextMod("speed", "Speed", "Blocks per second", "bolt", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> L("Speed: " + String.format("%.1f", Tracker.speed) + " b/s")));

		add(new TextMod("saturation", "Saturation", "Food and saturation levels", "food", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> L("Food: " + mc.player.getHungerManager().getFoodLevel() + "  Sat: " + String.format("%.1f", mc.player.getHungerManager().getSaturationLevel()))));

		add(new ArmorMod());
		add(new PotionMod());
		add(new KeysMod());

		add(new TextMod("cps", "CPS Counter", "Clicks per second", "mouse", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> L("CPS: " + Tracker.leftCps() + " | " + Tracker.rightCps())));

		add(new TextMod("active", "Active Modules", "List of modules that are on", "list", Cat.HUD, Anchor.STACK_RIGHT,
				(m, mc) -> {
					List<String> out = new ArrayList<>();
					for (Module o : ALL) if (o.enabled && o != m) out.add(o.name);
					out.sort((a, b) -> Integer.compare(Theme.tw(b), Theme.tw(a)));
					if (out.size() > 14) out = new ArrayList<>(out.subList(0, 14));
					if (out.isEmpty()) out.add("(none)");
					return out;
				}));

		// ---------------- Gameplay ----------------
		add(new ComboMod()).on(true);
		add(new HealthMod());

		add(new Func("fullbright", "Full Bright", "See clearly in the dark", "sun", Cat.GAMEPLAY,
				mc -> {
					if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
						mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0, true, false, false));
				},
				null,
				mc -> { if (mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION); }));

		add(new Func("nohurt", "Remove Hurt Shake", "No camera shake when you take damage", "heart", Cat.GAMEPLAY,
				mc -> { mc.player.hurtTime = 0; }, null, null));

		add(new Func("nobossbar", "Hide Boss Bar", "Hides boss bars at the top", "list", Cat.GAMEPLAY,
				mc -> { mc.inGameHud.getBossBarHud().clear(); }, null, null));

		add(new Func("sprint", "Auto Sprint", "Sprint whenever you walk forward", "bolt", Cat.GAMEPLAY,
				mc -> {
					ClientPlayerEntity p = mc.player;
					if (mc.options.forwardKey.isPressed() && !p.isSneaking() && !p.horizontalCollision && p.getHungerManager().getFoodLevel() > 6)
						p.setSprinting(true);
				}, null, null));

		add(new ZoomMod());

		// ---------------- Crosshair ----------------
		CROSSHAIR = add(new CrosshairMod());

		// ---------------- Waypoints ----------------
		add(new WaypointsMod());

		// ---------------- Social ----------------
		add(new TextMod("friends", "Friends Online", "Shows which of your friends are online", "person", Cat.SOCIAL, Anchor.STACK_RIGHT,
				(m, mc) -> {
					List<String> out = new ArrayList<>();
					out.add("Friends online:");
					if (mc.getNetworkHandler() != null) {
						for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
							String n = nameOf(e);
							if (n == null) continue;
							for (String f : Config.friends) if (f.equalsIgnoreCase(n)) { out.add(" - " + n); break; }
						}
					}
					if (out.size() == 1) out.add(" nobody yet");
					return out;
				}));

		CHAT_TIMESTAMPS = add(new Func("chattime", "Chat Timestamps", "Adds the time in front of chat messages", "chat", Cat.SOCIAL, null, null, null))
				.set(Setting.cycle("fmt", "Format", 0, "24 hour", "12 hour"));
	}

	/** Player name from a tab-list entry; GameProfile accessors changed between versions, so ask by reflection. */
	static String nameOf(PlayerListEntry e) {
		try {
			Object prof = e.getProfile();
			for (String n : new String[]{"name", "getName"}) {
				try { return (String) prof.getClass().getMethod(n).invoke(prof); } catch (NoSuchMethodException ignored) { }
			}
		} catch (Throwable ignored) { }
		return null;
	}

	// =============================================================
	// Module kinds
	// =============================================================

	static class TextMod extends Module {
		final BiFunction<Module, MinecraftClient, List<String>> f;

		TextMod(String id, String name, String desc, String icon, Cat cat, Anchor a, BiFunction<Module, MinecraftClient, List<String>> f) {
			super(id, name, desc, icon, cat, Kind.HUD, a);
			this.f = f;
		}

		List<String> lines(MinecraftClient mc) {
			try { return f.apply(this, mc); } catch (Throwable t) { return L(name + ": n/a"); }
		}

		@Override public int w(MinecraftClient mc) {
			int m = 30;
			for (String s : lines(mc)) m = Math.max(m, Theme.tw(s));
			return m + 12;
		}
		@Override public int h(MinecraftClient mc) { return lines(mc).size() * 10 + 5; }
		@Override public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
			List<String> l = lines(mc);
			Theme.hudBox(c, x, y, w(mc), h(mc));
			for (int i = 0; i < l.size(); i++) Theme.shadow(c, l.get(i), x + 6, y + 3 + i * 10, 0xFFFFFFFF);
		}
	}

	static class Func extends Module {
		final Consumer<MinecraftClient> tick, en, dis;

		Func(String id, String name, String desc, String icon, Cat cat, Consumer<MinecraftClient> tick, Consumer<MinecraftClient> en, Consumer<MinecraftClient> dis) {
			super(id, name, desc, icon, cat, Kind.LOGIC, Anchor.STACK_LEFT);
			this.tick = tick; this.en = en; this.dis = dis;
		}
		@Override public void onTick(MinecraftClient mc) { if (tick != null) tick.accept(mc); }
		@Override public void onEnable(MinecraftClient mc) { if (en != null) en.accept(mc); }
		@Override public void onDisable(MinecraftClient mc) { if (dis != null) dis.accept(mc); }
	}

	static class ArmorMod extends Module {
		static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

		ArmorMod() { super("armor", "Armor Status", "Armor pieces and durability", "armor", Cat.HUD, Kind.HUD, Anchor.STACK_RIGHT); }
		@Override public int w(MinecraftClient mc) { return 50; }
		@Override public int h(MinecraftClient mc) { return 4 * 17 + 3; }
		@Override public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
			Theme.hudBox(c, x, y, w(mc), h(mc));
			for (int i = 0; i < 4; i++) {
				ItemStack s = mc.player.getEquippedStack(SLOTS[i]);
				int yy = y + 2 + i * 17;
				if (s.isEmpty()) { Theme.text(c, "-", x + 8, yy + 4, Theme.DIM); continue; }
				c.drawItem(s, x + 3, yy);
				if (s.isDamageable()) {
					int pct = 100 * (s.getMaxDamage() - s.getDamage()) / Math.max(1, s.getMaxDamage());
					int col = pct > 50 ? 0xFF7CFF9B : pct > 20 ? 0xFFFFD166 : 0xFFFF6B6B;
					Theme.shadow(c, pct + "%", x + 22, yy + 4, col);
				}
			}
		}
	}

	static class PotionMod extends Module {
		PotionMod() { super("potions", "Potion Effects", "Active effects and time left", "potion", Cat.HUD, Kind.HUD, Anchor.STACK_RIGHT); }

		List<String> lines(MinecraftClient mc, boolean preview) {
			List<String> out = new ArrayList<>();
			for (StatusEffectInstance e : mc.player.getStatusEffects()) {
				String n = e.getEffectType().value().getName().getString();
				int d = e.getDuration();
				String t = d < 0 ? "**:**" : String.format("%d:%02d", d / 1200, (d / 20) % 60);
				out.add(n + " " + (e.getAmplifier() + 1) + "  " + t);
			}
			if (out.isEmpty()) out.add(preview ? "Speed 2  1:30" : "No effects");
			return out;
		}
		@Override public int w(MinecraftClient mc) { int m = 40; for (String s : lines(mc, true)) m = Math.max(m, Theme.tw(s)); return m + 12; }
		@Override public int h(MinecraftClient mc) { return lines(mc, true).size() * 10 + 5; }
		@Override public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
			List<String> l = lines(mc, preview);
			Theme.hudBox(c, x, y, w(mc), h(mc));
			for (int i = 0; i < l.size(); i++) Theme.shadow(c, l.get(i), x + 6, y + 3 + i * 10, 0xFFFFFFFF);
		}
	}

	static class KeysMod extends Module {
		KeysMod() { super("keystrokes", "Keystrokes", "WASD and mouse buttons", "keys", Cat.HUD, Kind.HUD, Anchor.BOTTOM_LEFT); }
		@Override public int w(MinecraftClient mc) { return 53; }
		@Override public int h(MinecraftClient mc) { return 52; }

		private void key(DrawContext c, int x, int y, int w, int h, String label, boolean down) {
			c.fill(x, y, x + w, y + h, down ? 0xCC3CE07A : 0x99000000);
			Theme.text(c, label, x + (w - Theme.tw(label)) / 2, y + (h - 8) / 2, down ? 0xFF06130E : 0xFFFFFFFF);
		}

		@Override public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
			net.minecraft.client.option.GameOptions o = mc.options;
			key(c, x + 18, y, 17, 17, "W", o.forwardKey.isPressed());
			key(c, x, y + 18, 17, 17, "A", o.leftKey.isPressed());
			key(c, x + 18, y + 18, 17, 17, "S", o.backKey.isPressed());
			key(c, x + 36, y + 18, 17, 17, "D", o.rightKey.isPressed());
			key(c, x, y + 37, 26, 15, "L " + Tracker.leftCps(), o.attackKey.isPressed());
			key(c, x + 27, y + 37, 26, 15, "R " + Tracker.rightCps(), o.useKey.isPressed());
		}
	}

	static class ComboMod extends Module {
		ComboMod() { super("combo", "Combo Counter", "Hits in a row, shown above the hotbar", "star", Cat.GAMEPLAY, Kind.HUD, Anchor.ABOVE_HOTBAR); }
		@Override public int w(MinecraftClient mc) { return 76; }
		@Override public int h(MinecraftClient mc) { return 16; }
		@Override public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
			int n = preview ? Math.max(5, Tracker.combo) : Tracker.combo;
			if (n <= 0) return;
			Theme.hudBox(c, x, y, w(mc), h(mc));
			String s = n + (n == 1 ? " hit" : " hit combo");
			Theme.shadow(c, s, x + (w(mc) - Theme.tw(s)) / 2 + 1, y + 4, Theme.ACCENT);
		}
	}

	static class HealthMod extends Module {
		HealthMod() { super("health", "Health Indicator", "Hearts of the mob or player you target", "heart", Cat.GAMEPLAY, Kind.HUD, Anchor.BELOW_CROSSHAIR); }
		@Override public int w(MinecraftClient mc) { return 100; }
		@Override public int h(MinecraftClient mc) { return 26; }
		@Override public void draw(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
			String name; float hp, max;
			if (mc.targetedEntity instanceof LivingEntity le) {
				name = le.getName().getString(); hp = le.getHealth(); max = le.getMaxHealth();
			} else if (preview) {
				name = "Target"; hp = 14; max = 20;
			} else return;
			int w = w(mc);
			Theme.hudBox(c, x, y, w, h(mc));
			Theme.shadow(c, Theme.fit(name, w - 10), x + 5, y + 3, 0xFFFFFFFF);
			int hearts = Math.min(10, (int) Math.ceil(max / 2f));
			int full = (int) (hp / 2f);
			boolean half = hp - full * 2 >= 1f;
			int hx = x + 5;
			for (int i = 0; i < hearts; i++) {
				int px = hx + i * 9;
				Icons.draw(c, "heart", px, y + 14, 1, 0xFF3A2A2A, 99);
				if (i < full) Icons.draw(c, "heart", px, y + 14, 1, 0xFFE5484D, 99);
				else if (i == full && half) Icons.draw(c, "heart", px, y + 14, 1, 0xFFE5484D, 5);
			}
		}
	}

	static class ZoomMod extends Module {
		private Integer saved;
		ZoomMod() {
			super("zoom", "Zoom", "Hold C to zoom in", "eye", Cat.GAMEPLAY, Kind.LOGIC, Anchor.STACK_LEFT);
			set(Setting.range("fov", "Zoom FOV", 30, 10, 60, 10));
		}
		@Override public void onTick(MinecraftClient mc) {
			boolean held = mc.currentScreen == null && GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
			if (held) {
				if (saved == null) saved = mc.options.getFov().getValue();
				mc.options.getFov().setValue(Math.max(30, get("fov")));
			} else restore(mc);
		}
		@Override public void onDisable(MinecraftClient mc) { restore(mc); }
		private void restore(MinecraftClient mc) { if (saved != null) { mc.options.getFov().setValue(saved); saved = null; } }
	}

	static class CrosshairMod extends Module {
		static final int[] COLORS = {0xFF3CE07A, 0xFFFFFFFF, 0xFFE5484D, 0xFF4AD6F0, 0xFFF2C230, 0xFFFF7AD9};

		CrosshairMod() {
			super("crosshair", "Custom Crosshair", "Replaces the vanilla crosshair", "cross", Cat.CROSSHAIR, Kind.OVERLAY, Anchor.STACK_LEFT);
			set(Setting.cycle("style", "Style", 0, "Cross", "Dot", "Circle"),
					Setting.range("size", "Size", 5, 1, 12, 1),
					Setting.range("gap", "Gap", 2, 0, 8, 1),
					Setting.range("thick", "Thickness", 1, 1, 3, 1),
					Setting.cycle("color", "Colour", 0, "Green", "White", "Red", "Cyan", "Yellow", "Pink"),
					Setting.cycle("target", "Red on target", 0, "On", "Off"));
		}
		@Override public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) {
			if (mc.currentScreen != null || !mc.options.getPerspective().isFirstPerson()) return;
			int cx = sw / 2, cy = sh / 2;
			int col = COLORS[Math.max(0, Math.min(COLORS.length - 1, get("color")))];
			if (get("target") == 0 && mc.targetedEntity != null) col = 0xFFE5484D;
			int size = get("size"), gap = get("gap"), t = get("thick"), lo = t / 2, hi = t - lo;
			switch (get("style")) {
				case 1:
					c.fill(cx - lo - 1, cy - lo - 1, cx + hi + 1, cy + hi + 1, col);
					break;
				case 2: {
					int r = size + gap + 2;
					for (int a = 0; a < 48; a++) {
						double ang = a * Math.PI * 2 / 48;
						int px = cx + (int) Math.round(Math.cos(ang) * r), py = cy + (int) Math.round(Math.sin(ang) * r);
						c.fill(px, py, px + t, py + t, col);
					}
					break;
				}
				default:
					c.fill(cx - lo, cy - gap - size, cx + hi, cy - gap, col);
					c.fill(cx - lo, cy + gap, cx + hi, cy + gap + size, col);
					c.fill(cx - gap - size, cy - lo, cx - gap, cy + hi, col);
					c.fill(cx + gap, cy - lo, cx + gap + size, cy + hi, col);
			}
		}
	}

	static class WaypointsMod extends Module {
		WaypointsMod() {
			super("waypoints", "Waypoints", "Shows your waypoints on screen with distance", "pin", Cat.WAYPOINTS, Kind.OVERLAY, Anchor.STACK_LEFT);
			set(Setting.cycle("dist", "Show distance", 0, "On", "Off"),
					Setting.range("max", "Max distance (x100)", 0, 0, 50, 5));
			enabled = defEnabled = true;
		}
		@Override public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) {
			if (mc.currentScreen != null || Config.waypoints.isEmpty()) return;
			ClientPlayerEntity p = mc.player;
			String dim = mc.world.getRegistryKey().getValue().toString();
			double fov = Math.toRadians(mc.options.getFov().getValue());
			double focal = (sh / 2.0) / Math.tan(fov / 2.0);
			int maxD = get("max") * 100;
			for (Config.Waypoint w : Config.waypoints) {
				if (!w.dim.equals(dim)) continue;
				double dx = w.x + 0.5 - p.getX(), dy = w.y + 0.5 - (p.getY() + p.getStandingEyeHeight()), dz = w.z + 0.5 - p.getZ();
				double horiz = Math.sqrt(dx * dx + dz * dz), dist = Math.sqrt(horiz * horiz + dy * dy);
				if (maxD > 0 && dist > maxD) continue;
				double yawTo = Math.toDegrees(Math.atan2(-dx, dz));
				double rel = MathHelper.wrapDegrees((float) (yawTo - p.getYaw()));
				double pitchTo = -Math.toDegrees(Math.atan2(dy, Math.max(0.001, horiz)));
				double relP = pitchTo - p.getPitch();
				int sx, sy;
				if (Math.abs(rel) < 85 && Math.abs(relP) < 85) {
					sx = sw / 2 + (int) (focal * Math.tan(Math.toRadians(rel)));
					sy = sh / 2 + (int) (focal * Math.tan(Math.toRadians(relP)));
				} else {
					sx = rel > 0 ? sw - 14 : 14;
					sy = sh / 2;
				}
				sx = MathHelper.clamp(sx, 14, sw - 14);
				sy = MathHelper.clamp(sy, 22, sh - 30);
				Icons.drawCentered(c, "pin", sx, sy, 1);
				String label = w.name + (get("dist") == 0 ? " " + (int) dist + "m" : "");
				Theme.shadow(c, label, sx - Theme.tw(label) / 2, sy + 7, 0xFFFFFFFF);
			}
		}
	}
}
