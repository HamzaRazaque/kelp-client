package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Module;
import com.kelpclient.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Per-module options: enable, keybind (none by default), HUD size, each setting, reset position. */
public class ModOptionsScreen extends KScreen {
	private static final int[] SIZES = {50, 75, 100, 125, 150, 175, 200};
	private final Module m;
	private boolean listening;
	private final boolean[] held = new boolean[GLFW.GLFW_KEY_LAST + 1];
	private KButton keyBtn;

	public ModOptionsScreen(Screen parent, Module m) { super(m.name.toUpperCase(), parent); this.m = m; }

	private int rowCount() { return 2 + m.settings.size() + (m.kind == Module.Kind.HUD ? 2 : 0); }

	@Override
	protected void layout() {
		pw = Math.min(width - 16, 340);
		ph = Math.min(height - 16, 60 + rowCount() * 24 + 30);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
	}

	private String keyLabel() { return listening ? "Press a key..." : KeyNames.of(m.key); }

	@Override
	protected void build() {
		int y = py + 30;
		addDrawableChild(new KButton(KButton.Mode.SWITCH, px + pw - 48, y, 38, 18, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); }).on(() -> m.enabled));
		y += 24;
		keyBtn = new KButton(KButton.Mode.PLAIN, px + pw - 110, y, 100, 18, keyLabel(), null, b -> {
			listening = !listening;
			if (listening) for (int k = 0; k < held.length; k++) held[k] = GLFW.glfwGetKey(client.getWindow().getHandle(), k) == GLFW.GLFW_PRESS;
			b.setMessage(Text.literal(keyLabel()));
		});
		addDrawableChild(keyBtn);
		y += 24;
		if (m.kind == Module.Kind.HUD) {
			addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 110, y, 100, 18, m.scale + "%", null, b -> {
				int next = SIZES[0];
				for (int s : SIZES) if (s > m.scale) { next = s; break; }
				m.scale = next;
				b.setMessage(Text.literal(m.scale + "%"));
				Config.save();
			}));
			y += 24;
		}
		for (Setting s : m.settings) {
			final Setting st = s;
			addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 110, y, 100, 18, st.display(), null, b -> { st.next(); b.setMessage(Text.literal(st.display())); Config.save(); }));
			y += 24;
		}
		if (m.kind == Module.Kind.HUD) {
			addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, 120, 18, "Reset position", null, b -> { m.resetPos(); Config.save(); }));
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		Theme.text(c, Theme.fit(m.desc, pw - 20), px + 10, py + 20, Theme.DIM);
		int y = py + 30;
		Theme.text(c, "Enabled", px + 10, y + 5, Theme.TEXT);
		y += 24;
		Theme.text(c, "Keybind (toggle)", px + 10, y + 5, Theme.TEXT);
		y += 24;
		if (m.kind == Module.Kind.HUD) { Theme.text(c, "Size", px + 10, y + 5, Theme.TEXT); y += 24; }
		for (Setting s : m.settings) { Theme.text(c, s.label, px + 10, y + 5, Theme.TEXT); y += 24; }
		if (listening) Theme.text(c, "Backspace clears the key, Esc cancels", px + 10, py + ph - 20, Theme.DIM);
	}

	@Override
	public void render(DrawContext c, int mx, int my, float delta) {
		super.render(c, mx, my, delta);
		if (!listening) return;
		long win = client.getWindow().getHandle();
		for (int k = GLFW.GLFW_KEY_SPACE; k <= GLFW.GLFW_KEY_LAST; k++) {
			boolean down = GLFW.glfwGetKey(win, k) == GLFW.GLFW_PRESS;
			if (down && !held[k]) {
				if (k == GLFW.GLFW_KEY_ESCAPE) { listening = false; }
				else {
					m.key = (k == GLFW.GLFW_KEY_BACKSPACE || k == GLFW.GLFW_KEY_DELETE) ? -1 : k;
					listening = false;
					Config.save();
				}
				keyBtn.setMessage(Text.literal(keyLabel()));
			}
			held[k] = down;
		}
	}
}
