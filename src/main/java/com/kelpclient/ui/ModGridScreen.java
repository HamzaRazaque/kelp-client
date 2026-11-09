package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Cat;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Mod Menu grid: tiles with icon, gear and Enabled/Disabled, in Kelp colours, with hover animations. */
public class ModGridScreen extends KScreen {
	private Cat filter;
	private String query = "";
	private int rowOff, cols, rows, gx, gy, gw, gh, tile;
	private static final int GAP = 6, TARGET = 86;
	private int preset = -1;
	private final List<ClickableWidget> gridW = new ArrayList<>();
	private final List<Object[]> cells = new ArrayList<>(); // {Module, x, y}
	private final Map<Module, Float> glow = new HashMap<>();
	private List<Module> shown = new ArrayList<>();

	public ModGridScreen(Screen parent) { super("MOD MENU", parent); }

	@Override protected boolean hasDone() { return false; }

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 640);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 26, py + 4, 18, 18, "x", null, b -> close()));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 26 - 4 - 62, py + 4, 62, 18, "Shots", "camera", b -> client.setScreen(new ScreenshotsScreen(this))));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 26 - 8 - 62 - 58, py + 4, 58, 18, "Mods", "kelp", b -> client.setScreen(new ModSettingsScreen(this, Cat.HUD))));

		int tx = px + 10, ty = py + 28;
		List<Object> tabs = new ArrayList<>();
		tabs.add(null);
		for (Cat c : Cat.values()) tabs.add(c);
		for (Object o : tabs) {
			final Cat cat = (Cat) o;
			String label = cat == null ? "All" : cat.label;
			int w = Theme.tw(label) + 10;
			if (tx + w > px + pw - 10) { tx = px + 10; ty += 20; }
			addDrawableChild(new KButton(KButton.Mode.TAB, tx, ty, w, 16, label, null, b -> { filter = cat; rowOff = 0; rebuildGrid(); }).on(() -> filter == cat));
			tx += w + 4;
		}

		int ry = ty + 20;
		int sw = Math.min(160, pw - 20 - 110);
		TextFieldWidget search = new TextFieldWidget(textRenderer, px + 10, ry, sw, 16, Text.literal("Search"));
		search.setPlaceholder(Text.literal("Search"));
		search.setText(query);
		search.setChangedListener(s -> { query = s; rowOff = 0; rebuildGrid(); });
		addDrawableChild(search);
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 10 - 100, ry, 44, 16, "Presets", null, b -> {
			preset = (preset + 1) % Modules.PRESET_NAMES.length;
			Modules.applyPreset(preset);
			b.setMessage(Text.literal(Modules.PRESET_NAMES[preset]));
			rebuildGrid();
		}));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 10 - 52, ry, 52, 16, "Reset", null, b -> {
			Modules.resetAll();
			rebuildGrid();
		}));

		gx = px + 10;
		gy = ry + 24;
		gw = pw - 20 - 8;
		gh = py + ph - 10 - gy;
		// square tiles: pick the column count first, then the tile size follows from the width
		cols = Math.max(3, (gw + GAP) / (TARGET + GAP));
		tile = Math.min(108, (gw - (cols - 1) * GAP) / cols);
		gx = px + 10 + (gw - (cols * tile + (cols - 1) * GAP)) / 2;
		rows = Math.max(1, (gh + GAP) / (tile + GAP));
		rebuildGrid();
	}

	private List<Module> filtered() {
		List<Module> out = new ArrayList<>();
		String q = query.toLowerCase(Locale.ROOT).trim();
		for (Module m : Modules.ALL) {
			if (filter != null && m.cat != filter) continue;
			if (!q.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(q) && !m.desc.toLowerCase(Locale.ROOT).contains(q)) continue;
			out.add(m);
		}
		return out;
	}

	private void rebuildGrid() {
		for (ClickableWidget w : gridW) remove(w);
		gridW.clear();
		cells.clear();
		shown = filtered();
		int total = (shown.size() + cols - 1) / cols;
		rowOff = Math.max(0, Math.min(rowOff, Math.max(0, total - rows)));
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int idx = (rowOff + r) * cols + c;
				if (idx >= shown.size()) break;
				final Module m = shown.get(idx);
				int x = gx + c * (tile + GAP), y = gy + r * (tile + GAP);
				cells.add(new Object[]{m, x, y});
				if (!m.settings.isEmpty() || m.kind == Module.Kind.HUD) {
					KButton gear = new KButton(KButton.Mode.GHOST, x + 4, y + tile - 20, 16, 16, "", "gear", b -> client.setScreen(new ModOptionsScreen(this, m)));
					gridW.add(gear); addDrawableChild(gear);
				}
				KButton st = new KButton(KButton.Mode.STATE, x + 22, y + tile - 19, tile - 26, 14, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); }).on(() -> m.enabled);
				gridW.add(st); addDrawableChild(st);
				KButton hit = new KButton(KButton.Mode.HIT, x, y, tile, tile - 22, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); });
				gridW.add(hit); addDrawableChild(hit);
			}
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		long now = System.currentTimeMillis();
		for (Object[] cell : cells) {
			Module m = (Module) cell[0];
			int x = (Integer) cell[1], y = (Integer) cell[2];
			boolean over = mx >= x && mx < x + tile && my >= y && my < y + tile;
			float g = glow.getOrDefault(m, 0f);
			g += ((over ? 1f : 0f) - g) * 0.25f;
			glow.put(m, g);
			Theme.box(c, x, y, tile, tile, Theme.lerp(Theme.PANEL, Theme.PANEL2, g), Theme.lerp(Theme.BORDER, Theme.ACCENT, g * 0.8f));
			c.fill(x + 1, y + 1, x + tile - 1, y + 2, m.enabled ? Theme.pulse() : Theme.withAlpha(0xFFFFFF, 12));
			Theme.text(c, Theme.fit(m.name, tile - 10), x + 5, y + 6, Theme.TEXT);
			int s = Math.min(3, Icons.fit(m.icon, tile - 36));
			int areaTop = y + 16, areaBot = y + tile - 22;
			int bob = Math.round(g * 2f + (float) Math.sin(now / 260.0 + x) * g);
			Icons.drawCentered(c, m.icon, x + tile / 2, (areaTop + areaBot) / 2 - bob, s);
		}
		if (shown.isEmpty()) Theme.centered(c, "No mods match", px + pw / 2, gy + 30, Theme.DIM);
		int total = (shown.size() + cols - 1) / cols;
		if (total > rows) {
			int bx = px + pw - 12, barH = gh, th = Math.max(16, barH * rows / total);
			int ty = gy + (barH - th) * rowOff / Math.max(1, total - rows);
			c.fill(bx, gy, bx + 3, gy + barH, 0xFF10201C);
			c.fill(bx, ty, bx + 3, ty + th, Theme.ACCENT);
		}
	}

	@Override
	protected boolean onScroll(int dir) { rowOff += dir; rebuildGrid(); return true; }
}
