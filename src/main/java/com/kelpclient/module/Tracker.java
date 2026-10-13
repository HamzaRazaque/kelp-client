package com.kelpclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

/** Shared per-tick state: CPS, combo, speed. */
public final class Tracker {
	private static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();
	private static boolean lw, rw, hurtPrev;
	private static double lx, lz;
	private static long lastHit;
	public static int combo;
	public static double speed;

	public static int leftCps() { prune(); return LEFT.size(); }
	public static int rightCps() { prune(); return RIGHT.size(); }

	private static void prune() {
		long now = System.currentTimeMillis();
		while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
		while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
	}

	public static void onHit() { combo++; lastHit = System.currentTimeMillis(); }

	public static void tick(MinecraftClient mc) {
		long now = System.currentTimeMillis();
		long win = mc.getWindow().getHandle();
		boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		if (mc.currentScreen == null) {
			if (l && !lw) LEFT.addLast(now);
			if (r && !rw) RIGHT.addLast(now);
		}
		lw = l; rw = r;
		prune();

		ClientPlayerEntity p = mc.player;
		if (p == null) return;
		double dx = p.getX() - lx, dz = p.getZ() - lz;
		speed = speed * 0.6 + Math.sqrt(dx * dx + dz * dz) * 20.0 * 0.4;
		lx = p.getX(); lz = p.getZ();

		boolean hurt = p.hurtTime > 0;
		if (hurt && !hurtPrev) combo = 0;
		hurtPrev = hurt;
		if (combo > 0 && now - lastHit > 3000) combo = 0;
	}
}
