package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.HudRenderer;
import com.kelpclient.module.Module;
import com.kelpclient.module.Cat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The Right Shift menu: Kelp icon bar on top, draggable HUD editor underneath. */
public class KelpMenuScreen extends Screen {
	private boolean shiftWas = true, leftWas, rightWas;
	private Module drag;
	private int offX, offY, barX, barY, barW, barH;
	private final List<KButton> tiles = new ArrayList<>();

	public KelpMenuScreen() { super(Text.literal("Kelp Client")); }

	@Override
	protected void init() {
		tiles.clear();
		int n = 6, t = Math.max(26, Math.min(40, width / 14)), gap = 4, pad = 6;
		barW = n * t + (n - 1) * gap + pad * 2;
		barH = 16 + pad + t + pad;
		barX = (width - barW) / 2;
		barY = 6;
		String[] icons = {"kelp", "shirt", "pin", "person", "arrows", "gear"};
		String[] tips = {"Mod Menu", "Kelp Mods (HUD)", "Waypoints", "Social / Friends", "Import / Export", "Client Settings"};
		for (int i = 0; i < n; i++) {
			final int idx = i;
			KButton b = new KButton(KButton.Mode.ICON, barX + pad + i * (t + gap), barY + 16 + pad / 2, t, t, "", icons[i], x -> open(idx)).tip(tips[i]);
			tiles.add(b);
			addDrawableChild(b);
		}
	}

	private void open(int i) {
		Screen next;
		switch (i) {
			case 0: next = new ModGridScreen(this); break;
			case 1: next = new ModSettingsScreen(this, Cat.HUD); break;
			case 2: next = new WaypointsScreen(this); break;
			case 3: next = new SocialScreen(this); break;
			case 4: next = new TransferScreen(this); break;
			default: next = new ClientSettingsScreen(this);
		}
		client.setScreen(next);
	}

	@Override
	public void renderBackground(DrawContext c, int mx, int my, float delta) {
		c.fill(0, 0, width, height, 0x66000000);
		HudRenderer.layout(client, width, height);
		for (Map.Entry<Module, int[]> e : HudRenderer.POS.entrySet()) {
			int[] p = e.getValue();
			e.getKey().draw(c, client, p[0], p[1], true);
			boolean hov = e.getKey() == drag || (drag == null && inside(mx, my, p) && !inBar(mx, my));
			Theme.outline(c, p[0] - 1, p[1] - 1, p[2] + 2, p[3] + 2, hov ? Theme.ACCENT : 0x663CE07A);
		}
		Theme.box(c, barX, barY, barW, barH, Theme.BG, Theme.BORDER);
		Icons.draw(c, "kelp", barX + 6, barY + 3, 1);
		Theme.text(c, "KELP CLIENT", barX + 20, barY + 4, Theme.TEXT);
	}

	@Override
	public void render(DrawContext c, int mx, int my, float delta) {
		super.render(c, mx, my, delta);
		for (KButton b : tiles) if (b.isHovered() && b.tip != null) {
			Theme.centered(c, b.tip, width / 2, barY + barH + 4, Theme.ACCENT);
		}
		Theme.centered(c, "Drag HUD modules  -  Right-click one to switch it off  -  Right Shift to close", width / 2, height - 12, Theme.DIM);
		poll(mx, my);
	}

	private boolean inside(int mx, int my, int[] p) { return mx >= p[0] && mx < p[0] + p[2] && my >= p[1] && my < p[1] + p[3]; }
	private boolean inBar(int mx, int my) { return mx >= barX && mx < barX + barW && my >= barY && my < barY + barH + 14; }

	/** Mouse and key state is polled here so we don't depend on the input-event method signatures. */
	private void poll(int mx, int my) {
		long win = client.getWindow().getHandle();
		boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		if (shift && !shiftWas) { close(); return; }
		shiftWas = shift;

		boolean left = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean right = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

		if (left && !leftWas && !inBar(mx, my)) {
			List<Map.Entry<Module, int[]>> es = new ArrayList<>(HudRenderer.POS.entrySet());
			for (int i = es.size() - 1; i >= 0; i--) {
				int[] p = es.get(i).getValue();
				if (inside(mx, my, p)) { drag = es.get(i).getKey(); offX = mx - p[0]; offY = my - p[1]; break; }
			}
		}
		if (left && drag != null) { drag.x = mx - offX; drag.y = my - offY; }
		if (!left && drag != null) { drag = null; Config.save(); }

		if (right && !rightWas && !inBar(mx, my)) {
			List<Map.Entry<Module, int[]>> es = new ArrayList<>(HudRenderer.POS.entrySet());
			for (int i = es.size() - 1; i >= 0; i--) {
				if (inside(mx, my, es.get(i).getValue())) { es.get(i).getKey().setEnabled(false); Config.save(); break; }
			}
		}
		leftWas = left; rightWas = right;
	}

	@Override public void removed() { Config.save(); }
	@Override public boolean shouldPause() { return false; }
}
