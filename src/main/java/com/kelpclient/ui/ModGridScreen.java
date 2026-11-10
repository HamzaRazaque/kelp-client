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

/** Mod Menu: sections (one per category) of wide rounded cards, each with a mint line icon and a label. */
public class ModGridScreen extends KScreen {
	private static final int GAP = 6, CARD_H = 46, HEADER_H = 24, DIVIDER_H = 14, TARGET_W = 108;

	private String query = "";
	private int lineOff, cols, cardW, gx, gy, gw, gh, preset = -1;
	private final List<Object> lines = new ArrayList<>(); // String = section header, List<Module> = one row of cards
	private final List<ClickableWidget> gridW = new ArrayList<>();
	private final List<Object[]> cells = new ArrayList<>(); // {Module, x, y}
	private final List<Object[]> heads = new ArrayList<>(); // {String, y, boolean divider}
	private final Map<Module, Float> glow = new HashMap<>();
	private int shownCount;

	public ModGridScreen(Screen parent) { super("KELP", parent); }

	@Override protected boolean hasDone() { return false; }

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 660);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 26, py + 4, 18, 18, "x", null, b -> close()));
		int ry = py + 26;
		int sw = Math.min(150, pw - 20 - 270);
		TextFieldWidget search = new TextFieldWidget(textRenderer, px + 10, ry, sw, 18, Text.literal("Search"));
		search.setPlaceholder(Text.literal("Search"));
		search.setText(query);
		search.setChangedListener(s -> { query = s; lineOff = 0; rebuildGrid(); });
		addDrawableChild(search);
		int bx = px + pw - 10;
		bx -= 52; addDrawableChild(new KButton(KButton.Mode.PLAIN, bx, ry, 52, 18, "Reset", null, b -> { Modules.resetAll(); rebuildGrid(); }));
		bx -= 4 + 62; addDrawableChild(new KButton(KButton.Mode.PLAIN, bx, ry, 62, 18, "Presets", null, b -> {
			preset = (preset + 1) % Modules.PRESET_NAMES.length;
			Modules.applyPreset(preset);
			b.setMessage(Text.literal(Modules.PRESET_NAMES[preset]));
			rebuildGrid();
		}));
		bx -= 4 + 74; addDrawableChild(new KButton(KButton.Mode.PLAIN, bx, ry, 74, 18, "Wardrobe", "sparkle", b -> client.setScreen(new WardrobeScreen(this))));
		bx -= 4 + 62; addDrawableChild(new KButton(KButton.Mode.PLAIN, bx, ry, 62, 18, "Shots", "camera", b -> client.setScreen(new ScreenshotsScreen(this))));

		gx = px + 10;
		gy = ry + 26;
		gw = pw - 20 - 6;
		gh = py + ph - 8 - gy;
		cols = Math.max(2, (gw + GAP) / (TARGET_W + GAP));
		cardW = (gw - (cols - 1) * GAP) / cols;
		gx = px + 10 + (gw - (cols * cardW + (cols - 1) * GAP)) / 2;
		buildLines();
		rebuildGrid();
	}

	@SuppressWarnings("unchecked")
	private void buildLines() {
		lines.clear();
		String q = query.toLowerCase(Locale.ROOT).trim();
		shownCount = 0;
		for (Cat cat : Cat.values()) {
			List<Module> mods = new ArrayList<>();
			for (Module m : Modules.ALL) {
				if (m.cat != cat) continue;
				if (!q.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(q) && !m.desc.toLowerCase(Locale.ROOT).contains(q)) continue;
				mods.add(m);
			}
			if (mods.isEmpty()) continue;
			shownCount += mods.size();
			lines.add(cat.label);
			for (int i = 0; i < mods.size(); i += cols) lines.add(new ArrayList<>(mods.subList(i, Math.min(mods.size(), i + cols))));
		}
	}

	@SuppressWarnings("unchecked")
	private void rebuildGrid() {
		for (ClickableWidget w : gridW) remove(w);
		gridW.clear();
		cells.clear();
		heads.clear();
		buildLines();
		lineOff = Math.max(0, Math.min(lineOff, Math.max(0, lines.size() - 1)));
		int y = gy;
		boolean first = true;
		for (int i = lineOff; i < lines.size(); i++) {
			Object ln = lines.get(i);
			if (ln instanceof String) {
				boolean div = !first && i > lineOff;
				int h = HEADER_H + (div ? DIVIDER_H : 0);
				if (y + h > gy + gh) break;
				heads.add(new Object[]{ln, y + (div ? DIVIDER_H : 0), div, y});
				y += h;
			} else {
				if (y + CARD_H > gy + gh) break;
				List<Module> row = (List<Module>) ln;
				for (int c = 0; c < row.size(); c++) {
					final Module m = row.get(c);
					int x = gx + c * (cardW + GAP);
					cells.add(new Object[]{m, x, y});
					KButton hit = new KButton(KButton.Mode.HIT, x, y, cardW, CARD_H, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); });
					gridW.add(hit); addDrawableChild(hit);
					KButton gear = new KButton(KButton.Mode.GHOST, x + cardW - 17, y + 1, 16, 16, "", "gear", b -> client.setScreen(new ModOptionsScreen(this, m)));
					gridW.add(gear); addDrawableChild(gear);
				}
				y += CARD_H + GAP;
			}
			first = false;
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		// header pill, like the theme card
		String pill = "Abyssal Mint";
		int pw2 = Theme.tw(pill) + 14, pxx = px + pw - 30 - pw2;
		Theme.box(c, pxx, py + 5, pw2, 14, 0xFF0F3A2B, 0xFF17603F);
		Theme.text(c, pill, pxx + 7, py + 9, Theme.ACCENT);

		for (Object[] h : heads) {
			int y = (Integer) h[1];
			if ((Boolean) h[2]) c.fill(gx, y - 8, gx + gw, y - 7, Theme.BORDER);
			Theme.text(c, (String) h[0], gx + 2, y + 6, Theme.TEXT);
		}
		long now = System.currentTimeMillis();
		for (Object[] cell : cells) {
			Module m = (Module) cell[0];
			int x = (Integer) cell[1], y = (Integer) cell[2];
			boolean over = mx >= x && mx < x + cardW && my >= y && my < y + CARD_H;
			float g = glow.getOrDefault(m, 0f);
			g += ((over ? 1f : 0f) - g) * 0.25f;
			glow.put(m, g);
			int border = m.enabled ? Theme.lerp(0xFF2B9B73, Theme.ACCENT, g) : Theme.lerp(0xFF14382F, Theme.BORDER, g);
			Theme.box(c, x, y, cardW, CARD_H, Theme.lerp(Theme.PANEL2, Theme.HOVER, g), border);
			int bob = Math.round(g * 2f + (float) Math.sin(now / 260.0 + x) * g * 0.6f);
			Icons.drawCentered(c, m.icon, x + cardW / 2, y + 17 - bob, 1);
			Theme.centered(c, Theme.fit(m.name, cardW - 8), x + cardW / 2, y + CARD_H - 13, m.enabled ? Theme.TEXT : Theme.DIM);
			if (m.enabled) c.fill(x + 5, y + 5, x + 8, y + 8, Theme.ACCENT);
		}
		if (shownCount == 0) Theme.centered(c, "No mods match", px + pw / 2, gy + 30, Theme.DIM);
		if (lines.size() > 1 && (lineOff > 0 || cells.size() < shownCount)) {
			int bx = px + pw - 9, th = Math.max(16, gh * Math.max(1, cells.size() + heads.size()) / Math.max(1, shownCount + heads.size()));
			th = Math.min(th, gh);
			int ty = gy + (gh - th) * lineOff / Math.max(1, lines.size() - 1);
			c.fill(bx, gy, bx + 3, gy + gh, 0xFF10201C);
			c.fill(bx, ty, bx + 3, ty + th, Theme.ACCENT);
		}
	}

	@Override
	protected boolean onScroll(int dir) { lineOff += dir; rebuildGrid(); return true; }
}
