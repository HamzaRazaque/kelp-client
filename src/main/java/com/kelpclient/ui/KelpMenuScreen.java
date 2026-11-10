package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Cat;
import com.kelpclient.module.HudRenderer;
import com.kelpclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The menu you get by double-tapping Right Shift: a square grid of Kelp tiles in the middle of the screen,
 * floating over the draggable HUD editor. It pops in (and out) with an animation.
 */
public class KelpMenuScreen extends Screen {
	private static final int COLS = 3, ROWS = 2, GAP = 6, PAD = 10, HEAD = 22;
	private static final long OPEN_MS = 340, CLOSE_MS = 190, STAGGER_MS = 45;

	private final long openMs = System.currentTimeMillis();
	private long closeMs = -1;
	private boolean shiftWas = true, leftWas, rightWas;
	private Module drag;
	private int offX, offY, panelX, panelY, panelW, panelH, tile;
	private final List<KButton> tiles = new ArrayList<>();

	public KelpMenuScreen() { super(Text.literal("Kelp Client")); }

	@Override
	protected void init() {
		tiles.clear();
		tile = Math.max(40, Math.min(56, Math.min(width / 6, height / 5)));
		panelW = COLS * tile + (COLS - 1) * GAP + PAD * 2;
		panelH = HEAD + ROWS * tile + (ROWS - 1) * GAP + PAD + 2;
		panelX = (width - panelW) / 2;
		panelY = (height - panelH) / 2; // dead centre
		String[] icons = {"grid", "sparkle", "pin", "users", "arrows", "gear"};
		String[] names = {"Mods", "Wardrobe", "Waypoints", "Social", "Transfer", "Settings"};
		for (int i = 0; i < COLS * ROWS; i++) {
			final int idx = i;
			int cx = panelX + PAD + (i % COLS) * (tile + GAP), cy = panelY + HEAD + (i / COLS) * (tile + GAP);
			KButton b = new KButton(KButton.Mode.ICON, cx, cy, tile, tile, names[i], icons[i], x -> open(idx));
			tiles.add(b);
			addDrawableChild(b);
		}
	}

	private void open(int i) {
		if (closeMs >= 0) return;
		Screen next;
		switch (i) {
			case 0: next = new ModGridScreen(this); break;
			case 1: next = new WardrobeScreen(this); break;
			case 2: next = new WaypointsScreen(this); break;
			case 3: next = new SocialScreen(this); break;
			case 4: next = new TransferScreen(this); break;
			default: next = new ClientSettingsScreen(this);
		}
		client.setScreen(next);
	}

	private static float easeOut(float t) { t = Math.max(0f, Math.min(1f, t)); return 1f - (1f - t) * (1f - t) * (1f - t); }

	private static float easeOutBack(float t) {
		t = Math.max(0f, Math.min(1f, t));
		float c1 = 1.70158f, c3 = c1 + 1f, u = t - 1f;
		return 1f + c3 * u * u * u + c1 * u * u;
	}

	/** 0 = fully hidden, 1 = fully shown (while opening and closing). */
	private float show() {
		long now = System.currentTimeMillis();
		if (closeMs >= 0) return 1f - easeOut((now - closeMs) / (float) CLOSE_MS);
		return easeOut((now - openMs) / (float) OPEN_MS);
	}

	private void beginClose() { if (closeMs < 0) closeMs = System.currentTimeMillis(); }

	/** The panel itself (drawn inside the animated transform by Screen.render, just before the tile widgets). */
	@Override
	public void renderBackground(DrawContext c, int mx, int my, float delta) {
		Theme.box(c, panelX, panelY, panelW, panelH, Theme.BG, Theme.BORDER);
		c.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 2, Theme.withAlpha(Theme.ACCENT, 90));
		Icons.draw(c, "kelp", panelX + PAD, panelY + 3, 1);
		Theme.text(c, "KELP CLIENT", panelX + PAD + 20, panelY + 8, Theme.TEXT);
	}

	@Override
	public void render(DrawContext c, int mx, int my, float delta) {
		float sh = show();
		long now = System.currentTimeMillis();

		// dim + the draggable HUD editor sit behind the panel and are not scaled
		c.fill(0, 0, width, height, Theme.withAlpha(0x000000, (int) (0x66 * sh)));
		HudRenderer.layout(client, width, height);
		for (Map.Entry<Module, int[]> e : HudRenderer.POS.entrySet()) {
			int[] p = e.getValue();
			e.getKey().drawScaled(c, client, p[0], p[1], true);
			boolean hov = e.getKey() == drag || (drag == null && inside(mx, my, p) && !inPanel(mx, my));
			Theme.outline(c, p[0] - 1, p[1] - 1, p[2] + 2, p[3] + 2, hov ? Theme.pulse() : 0x6639E6A2);
		}

		// tiles pop in one after another, and shrink away together on close
		long since = now - openMs;
		for (int i = 0; i < tiles.size(); i++) {
			float pop = closeMs >= 0 ? sh : easeOutBack((since - 70 - i * STAGGER_MS) / 260f);
			tiles.get(i).pop = pop;
		}

		// panel: scale + fade
		float sc = 0.86f + 0.14f * sh;
		float cx = panelX + panelW / 2f, cy = panelY + panelH / 2f;
		c.getMatrices().pushMatrix();
		c.getMatrices().translate(cx, cy);
		c.getMatrices().scale(sc, sc);
		c.getMatrices().translate(-cx, -cy);
		try {
			super.render(c, mx, my, delta);
			if (sh < 1f) c.fill(panelX, panelY, panelX + panelW, panelY + panelH, Theme.withAlpha(0x081411, (int) ((1f - sh) * 235)));
		} finally {
			c.getMatrices().popMatrix();
		}

		if (sh > 0.95f) {
			Theme.centered(c, "Drag HUD modules  -  Right-click one to switch it off  -  Right Shift to close", width / 2, panelY + panelH + 10, Theme.DIM);
		}
		poll(mx, my);
		if (closeMs >= 0 && now - closeMs >= CLOSE_MS) client.setScreen(null);
	}

	private boolean inside(int mx, int my, int[] p) { return mx >= p[0] && mx < p[0] + p[2] && my >= p[1] && my < p[1] + p[3]; }
	private boolean inPanel(int mx, int my) { return mx >= panelX && mx < panelX + panelW && my >= panelY && my < panelY + panelH; }

	/** Mouse and key state is polled here so we don't depend on the input-event method signatures. */
	private void poll(int mx, int my) {
		long win = client.getWindow().getHandle();
		boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		if (shift && !shiftWas) { beginClose(); }
		shiftWas = shift;
		if (closeMs >= 0) return;

		boolean left = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean right = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

		if (left && !leftWas && !inPanel(mx, my)) {
			List<Map.Entry<Module, int[]>> es = new ArrayList<>(HudRenderer.POS.entrySet());
			for (int i = es.size() - 1; i >= 0; i--) {
				int[] p = es.get(i).getValue();
				if (inside(mx, my, p)) { drag = es.get(i).getKey(); offX = mx - p[0]; offY = my - p[1]; break; }
			}
		}
		if (left && drag != null) { drag.x = mx - offX; drag.y = my - offY; }
		if (!left && drag != null) { drag = null; Config.save(); }

		if (right && !rightWas && !inPanel(mx, my)) {
			List<Map.Entry<Module, int[]>> es = new ArrayList<>(HudRenderer.POS.entrySet());
			for (int i = es.size() - 1; i >= 0; i--) {
				if (inside(mx, my, es.get(i).getValue())) { es.get(i).getKey().setEnabled(false); Config.save(); break; }
			}
		}
		leftWas = left; rightWas = right;
	}

	@Override public void close() { beginClose(); }
	@Override public void removed() { Config.save(); }
	@Override public boolean shouldPause() { return false; }
}
