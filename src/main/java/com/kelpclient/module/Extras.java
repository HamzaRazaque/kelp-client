package com.kelpclient.module;

import com.kelpclient.module.Module.Anchor;
import com.kelpclient.module.Module.Kind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

/** Session timer, the visual-customization modules and the particle cosmetics. */
public final class Extras {
	private static final long START = System.currentTimeMillis();
	private static Module TIME;
	private static final long[] TIMES = {23000, 1000, 6000, 12500, 14500, 18000};

	/** Read by WorldMixin; -1 means "don't override". */
	public static long timeOverride() {
		return TIME != null && TIME.enabled ? TIMES[Math.max(0, Math.min(TIMES.length - 1, TIME.get("time")))] : -1L;
	}

	static void register() {
		Modules.add(new Modules.TextMod("session", "Session Timer", "Time since you launched the game", "clock", Cat.HUD, Anchor.STACK_LEFT,
				(m, mc) -> {
					long s = (System.currentTimeMillis() - START) / 1000;
					return Modules.L(String.format("Session: %d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60));
				}));

		// ---------- Visual ----------
		TIME = Modules.add(new Modules.Func("time", "Custom Time", "Fix the time of day you see (client only)", "clock", Cat.VISUAL, null, null, null))
				.set(Setting.cycle("time", "Time", 2, "Sunrise", "Day", "Noon", "Sunset", "Night", "Midnight"));

		Modules.add(new Modules.Func("weather", "Custom Weather", "Force clear, rain or thunder on your screen", "moon", Cat.VISUAL,
				mc -> {
					int w = Modules.BY_ID.get("weather").get("weather");
					mc.world.setRainGradient(w == 0 ? 0f : 1f);
					mc.world.setThunderGradient(w == 2 ? 1f : 0f);
				}, null, null)).set(Setting.cycle("weather", "Weather", 0, "Clear", "Rain", "Thunder"));

		Modules.add(new FovMod());
		Modules.add(new ParticlesMod());
		Modules.add(new Modules.Func("hurtcam", "Custom Hurt Camera", "How strongly the camera tilts when hurt", "heart", Cat.VISUAL,
				mc -> {
					Module m = Modules.BY_ID.get("hurtcam");
					if (mc.player.hurtTime > 0) mc.player.hurtTime = (int) (mc.player.hurtTime * m.get("strength") / 100f);
				}, null, null)).set(Setting.range("strength", "Tilt strength %", 50, 0, 100, 10));
		Modules.add(new BobMod());
		Modules.add(new SkyMod());
		Modules.add(new FogMod());
		Modules.add(new BlurMod());

		// ---------- Cosmetics ----------
		Modules.add(new Cosmetic("trail", "Particle Trail", "Leaves particles behind you as you move", "bolt", 0, 3, true));
		Modules.add(new Cosmetic("aura", "Aura", "A ring of particles circling your body", "star", 1, 3, false));
		Modules.add(new Cosmetic("halo", "Halo", "A glowing ring above your head", "sun", 3, 2, false));
		Modules.add(new Cosmetic("wings", "Wings", "Flapping particle wings on your back", "eye", 3, 2, false));
	}

	private static int argb(int alpha, int rgb) { return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF); }

	static class FovMod extends Module {
		private Integer saved;
		FovMod() {
			super("fov", "FOV Changer", "Set any field of view from 30 to 110", "eye", Cat.VISUAL, Kind.LOGIC, Anchor.STACK_LEFT);
			set(Setting.range("fov", "FOV", 90, 30, 110, 5));
		}
		@Override public void onEnable(MinecraftClient mc) { saved = mc.options.getFov().getValue(); }
		@Override public void onTick(MinecraftClient mc) {
			Module zoom = Modules.BY_ID.get("zoom");
			boolean zooming = zoom != null && zoom.enabled && mc.currentScreen == null
					&& GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
			if (zooming) return;
			if (saved == null) saved = mc.options.getFov().getValue();
			if (mc.options.getFov().getValue() != get("fov")) mc.options.getFov().setValue(get("fov"));
		}
		@Override public void onDisable(MinecraftClient mc) { if (saved != null) { mc.options.getFov().setValue(saved); saved = null; } }
	}

	static class ParticlesMod extends Module {
		private Integer saved;
		ParticlesMod() {
			super("particles", "Custom Particles", "Choose how many vanilla particles you see", "star", Cat.VISUAL, Kind.LOGIC, Anchor.STACK_LEFT);
			set(Setting.cycle("mode", "Particles", 1, "All", "Decreased", "Minimal"));
		}

		// The particle-level enum moved packages between versions, so it is handled by ordinal (ALL, DECREASED, MINIMAL).
		private static int current(MinecraftClient mc) { return ((Enum<?>) mc.options.getParticles().getValue()).ordinal(); }

		@SuppressWarnings({"rawtypes", "unchecked"})
		private static void put(MinecraftClient mc, int ord) {
			SimpleOption o = mc.options.getParticles();
			Enum cur = (Enum) o.getValue();
			Object[] all = cur.getDeclaringClass().getEnumConstants();
			o.setValue(all[Math.max(0, Math.min(ord, all.length - 1))]);
		}

		@Override public void onEnable(MinecraftClient mc) { saved = current(mc); }
		@Override public void onTick(MinecraftClient mc) {
			if (saved == null) saved = current(mc);
			int want = Math.max(0, Math.min(2, get("mode")));
			if (current(mc) != want) put(mc, want);
		}
		@Override public void onDisable(MinecraftClient mc) { if (saved != null) { put(mc, saved); saved = null; } }
	}

	static class BobMod extends Module {
		private Boolean saved;
		BobMod() {
			super("bobbing", "View Bobbing Toggle", "Turn the walking camera bob on or off", "boot", Cat.VISUAL, Kind.LOGIC, Anchor.STACK_LEFT);
			set(Setting.cycle("bob", "View bobbing", 0, "Off", "On"));
		}
		@Override public void onEnable(MinecraftClient mc) { saved = mc.options.getBobView().getValue(); }
		@Override public void onTick(MinecraftClient mc) {
			if (saved == null) saved = mc.options.getBobView().getValue();
			boolean want = get("bob") == 1;
			if (mc.options.getBobView().getValue() != want) mc.options.getBobView().setValue(want);
		}
		@Override public void onDisable(MinecraftClient mc) { if (saved != null) { mc.options.getBobView().setValue(saved); saved = null; } }
	}

	private static final int[] TINTS = {0x3B82F6, 0xA855F7, 0xEC4899, 0xF97316, 0x22C55E, 0xEF4444, 0xFFFFFF, 0x9CA3AF};
	private static final String[] TINT_NAMES = {"Blue", "Purple", "Pink", "Orange", "Green", "Red", "White", "Gray"};

	private static int horizonY(MinecraftClient mc, int sh) {
		double fov = Math.toRadians(Math.max(30, mc.options.getFov().getValue()));
		double focal = (sh / 2.0) / Math.tan(fov / 2.0);
		double rel = -mc.player.getPitch();
		return MathHelper.clamp(sh / 2 + (int) (focal * Math.tan(Math.toRadians(MathHelper.clamp(rel, -80, 80)))), 0, sh);
	}

	/** Custom Sky: a coloured tint over the part of the screen above the horizon. */
	static class SkyMod extends Module {
		SkyMod() {
			super("sky", "Custom Sky", "Tints the sky above the horizon", "sun", Cat.VISUAL, Kind.OVERLAY, Anchor.STACK_LEFT);
			set(Setting.cycle("color", "Colour", 1, TINT_NAMES), Setting.range("amount", "Strength %", 35, 5, 80, 5));
		}
		@Override public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) {
			if (mc.currentScreen != null) return;
			int hy = horizonY(mc, sh);
			if (hy <= 0) return;
			int rgb = TINTS[get("color")], a = get("amount") * 255 / 100;
			c.fillGradient(0, 0, sw, hy, argb(a, rgb), argb(a / 6, rgb));
		}
	}

	/** Custom Fog: haze over the whole view, thickest at the horizon. */
	static class FogMod extends Module {
		FogMod() {
			super("fog", "Custom Fog", "Adds a coloured haze to your view", "cube", Cat.VISUAL, Kind.OVERLAY, Anchor.STACK_LEFT);
			set(Setting.cycle("color", "Colour", 7, TINT_NAMES), Setting.range("density", "Density %", 30, 5, 80, 5));
		}
		@Override public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) {
			if (mc.currentScreen != null) return;
			int rgb = TINTS[get("color")], a = get("density") * 255 / 100;
			c.fill(0, 0, sw, sh, argb(a / 3, rgb));
			int hy = horizonY(mc, sh), band = sh / 4;
			c.fillGradient(0, Math.max(0, hy - band), sw, hy, argb(0, rgb), argb(a / 2, rgb));
			c.fillGradient(0, hy, sw, Math.min(sh, hy + band), argb(a / 2, rgb), argb(0, rgb));
		}
	}

	/** Motion Blur: speed streaks along the screen edges while you turn fast (a light stand-in for a real blur shader). */
	static class BlurMod extends Module {
		private float lastYaw, lastPitch, energy;
		BlurMod() {
			super("motionblur", "Motion Blur", "Edge streaks when you turn or move fast", "bolt", Cat.VISUAL, Kind.OVERLAY, Anchor.STACK_LEFT);
			set(Setting.range("amount", "Strength", 5, 1, 10, 1));
		}
		@Override public void overlay(DrawContext c, MinecraftClient mc, int sw, int sh) {
			ClientPlayerEntity p = mc.player;
			float dy = Math.abs(MathHelper.wrapDegrees(p.getYaw() - lastYaw)), dp = Math.abs(p.getPitch() - lastPitch);
			lastYaw = p.getYaw(); lastPitch = p.getPitch();
			energy = energy * 0.85f + (dy + dp) * 0.15f;
			if (mc.currentScreen != null) return;
			float k = Math.min(1f, energy * get("amount") / 40f);
			if (k < 0.02f) return;
			int w = sw / 5, steps = 20;
			for (int i = 0; i < steps; i++) {
				int a = (int) (k * 150 * (1f - i / (float) steps));
				int x0 = i * w / steps, x1 = (i + 1) * w / steps + 1;
				c.fill(x0, 0, x1, sh, argb(a, 0xDDEEFF));
				c.fill(sw - x1, 0, sw - x0, sh, argb(a, 0xDDEEFF));
			}
		}
	}

	// ---------- Cosmetics ----------
	private static final String[] FX_NAMES = {"Hearts", "Flames", "Soul Fire", "End Rod", "Enchant", "Notes", "Sparkles", "Crits", "Clouds", "Snow", "Witch", "Electric"};
	private static ParticleEffect[] fx;
	private static ParticleEffect fx(int i) {
		if (fx == null) fx = new ParticleEffect[]{ParticleTypes.HEART, ParticleTypes.FLAME, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.END_ROD,
				ParticleTypes.ENCHANT, ParticleTypes.NOTE, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.CRIT, ParticleTypes.CLOUD,
				ParticleTypes.SNOWFLAKE, ParticleTypes.WITCH, ParticleTypes.ELECTRIC_SPARK};
		return fx[Math.max(0, Math.min(fx.length - 1, i))];
	}

	static class Cosmetic extends Module {
		private int ticks;
		Cosmetic(String id, String name, String desc, String icon, int defStyle, int defDensity, boolean showFirstPerson) {
			super(id, name, desc, icon, Cat.COSMETICS, Kind.LOGIC, Anchor.STACK_LEFT);
			set(Setting.cycle("style", "Particle", defStyle, FX_NAMES),
					Setting.range("density", "Density", defDensity, 1, 6, 1),
					Setting.cycle("fp", "First person", showFirstPerson ? 1 : 0, "Hidden", "Shown"));
		}

		private void spawn(MinecraftClient mc, double x, double y, double z, double vx, double vy, double vz) {
			mc.particleManager.addParticle(fx(get("style")), x, y, z, vx, vy, vz);
		}

		@Override public void onTick(MinecraftClient mc) {
			ClientPlayerEntity p = mc.player;
			if (p == null || mc.world == null || p.isSpectator()) return;
			if (get("fp") == 0 && mc.options.getPerspective().isFirstPerson()) return;
			ticks++;
			ThreadLocalRandom r = ThreadLocalRandom.current();
			int n = get("density");
			double x = p.getX(), y = p.getY(), z = p.getZ();
			switch (id) {
				case "trail":
					if (Tracker.speed < 0.6) return;
					for (int i = 0; i < n; i++) spawn(mc, x + r.nextDouble(-0.25, 0.25), y + 0.1 + r.nextDouble(0, 0.3), z + r.nextDouble(-0.25, 0.25), 0, 0.02, 0);
					break;
				case "aura": {
					for (int i = 0; i < n * 2; i++) {
						double a = ticks * 0.25 + i * (Math.PI * 2 / (n * 2));
						spawn(mc, x + Math.cos(a) * 0.75, y + 0.2 + ((ticks + i * 7) % 18) / 10.0, z + Math.sin(a) * 0.75, 0, 0, 0);
					}
					break;
				}
				case "halo": {
					double hy = y + p.getHeight() + 0.25;
					int pts = 4 + n * 2;
					for (int i = 0; i < pts; i++) {
						double a = i * (Math.PI * 2 / pts) + ticks * 0.08;
						spawn(mc, x + Math.cos(a) * 0.36, hy, z + Math.sin(a) * 0.36, 0, 0, 0);
					}
					break;
				}
				default: { // wings
					if (ticks % 2 != 0) return;
					double yaw = Math.toRadians(p.getYaw());
					double bx = Math.sin(yaw), bz = -Math.cos(yaw); // direction behind the player
					double rx = Math.cos(yaw), rz = Math.sin(yaw);   // sideways
					double flap = Math.sin(ticks * 0.18) * 0.35;
					int feathers = 3 + n * 2;
					for (int side = -1; side <= 1; side += 2) {
						for (int i = 1; i <= feathers; i++) {
							double out = i * 0.2;
							double up = 1.45 + i * 0.07 - flap * out * 1.4 + (i % 2 == 0 ? 0.12 : -0.05);
							double back = 0.28 + out * 0.25;
							spawn(mc, x + bx * back + rx * side * out, y + up, z + bz * back + rz * side * out, 0, 0, 0);
						}
					}
				}
			}
		}
	}
}
