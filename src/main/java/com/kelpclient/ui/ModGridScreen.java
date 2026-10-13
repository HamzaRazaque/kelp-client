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
import java.util.List;
import java.util.Locale;

/** Mod Menu grid: tiles with icon, gear and Enabled/Disabled, like the FastClient layout in Kelp colours. */
public class ModGridScreen extends KScreen {
	private Cat filter;
	private String query = "";
	private int rowOff, cols, rows, gx, gy, gw, gh, tileW;
	private static final int TILE_H = 62, GAP = 6;
	private int preset = -1;
	private final List<ClickableWidget> gridW = new ArrayList<>();
	private final List<Object[]> cells = new ArrayList<>(); // {Module, x, y}
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
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 22, py + 6, 14, 14, "x", null, b -> close()));

		int tx = px + 10, ty = py + 28;
		List<Object> tabs = new ArrayList<>();
		tabs.add(null);
		for (Cat c : Cat.values()) tabs.add(c);
		for (Object o : tabs) {
			final Cat cat = (Cat) o;
			String label = cat == null ? "All" : cat.label;
			int w = Theme.tw(label) + 14;
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
		cols = Math.max(2, Math.min(4, (gw + GAP) / (110 + GAP)));
		tileW = (gw - (cols - 1) * GAP) / cols;
		rows = Math.max(1, (gh + GAP) / (TILE_H + GAP));
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
				int x = gx + c * (tileW + GAP), y = gy + r * (TILE_H + GAP);
				cells.add(new Object[]{m, x, y});
				if (!m.settings.isEmpty() || m.kind == Module.Kind.HUD) {
					KButton gear = new KButton(KButton.Mode.GHOST, x + 4, y + TILE_H - 20, 16, 16, "", "gear", b -> client.setScreen(new ModOptionsScreen(this, m)));
					gridW.add(gear); addDrawableChild(gear);
				}
				KButton st = new KButton(KButton.Mode.STATE, x + 22, y + TILE_H - 19, tileW - 26, 14, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); }).on(() -> m.enabled);
				gridW.add(st); addDrawableChild(st);
				KButton hit = new KButton(KButton.Mode.HIT, x, y, tileW, TILE_H - 22, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); });
				gridW.add(hit); addDrawableChild(hit);
			}
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		Theme.text(c, "Scroll for more  -  click a tile to toggle  -  gear for options", px + 70, py + 10, Theme.DIM);
		for (Object[] cell : cells) {
			Module m = (Module) cell[0];
			int x = (Integer) cell[1], y = (Integer) cell[2];
			Theme.box(c, x, y, tileW, TILE_H, Theme.PANEL, Theme.BORDER);
			if (m.enabled) c.fill(x + 1, y + 1, x + tileW - 1, y + 2, Theme.ACCENT);
			Theme.text(c, Theme.fit(m.name, tileW - 10), x + 5, y + 5, Theme.TEXT);
			Icons.drawCentered(c, m.icon, x + tileW / 2, y + 28, 3);
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
