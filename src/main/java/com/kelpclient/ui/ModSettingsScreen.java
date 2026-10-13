package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Cat;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.List;

/** The "KELP MODS" screen: category sidebar, module rows with switches. */
public class ModSettingsScreen extends KScreen {
	private Cat cat;
	private int rowOff, rows, rx, ry, rw;
	private static final int ROW_H = 28, GAP = 3;
	private final List<ClickableWidget> rowW = new ArrayList<>();
	private final List<Module> mods = new ArrayList<>();

	public ModSettingsScreen(Screen parent, Cat cat) { super("KELP MODS", parent); this.cat = cat; }

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 560);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		int sbW = pw < 400 ? 84 : 110;
		int y = py + 30;
		for (Cat c : Cat.values()) {
			final Cat cc = c;
			addDrawableChild(new KButton(KButton.Mode.TAB, px + 8, y, sbW, 24, c.label, c.icon, b -> { cat = cc; rowOff = 0; rebuildRows(); }).on(() -> cat == cc));
			y += 28;
		}
		rx = px + sbW + 20;
		ry = py + 30;
		rw = pw - sbW - 20 - 8;
		rows = Math.max(1, (ph - 30 - 34) / (ROW_H + GAP));
		rebuildRows();
	}

	private void rebuildRows() {
		for (ClickableWidget w : rowW) remove(w);
		rowW.clear();
		mods.clear();
		for (Module m : Modules.ALL) if (m.cat == cat) mods.add(m);
		rowOff = Math.max(0, Math.min(rowOff, Math.max(0, mods.size() - rows)));
		for (int i = 0; i < rows; i++) {
			int idx = rowOff + i;
			if (idx >= mods.size()) break;
			final Module m = mods.get(idx);
			int y = ry + i * (ROW_H + GAP);
			if (!m.settings.isEmpty() || m.kind == Module.Kind.HUD) {
				KButton gear = new KButton(KButton.Mode.GHOST, rx + rw - 62, y + 6, 16, 16, "", "gear", b -> client.setScreen(new ModOptionsScreen(this, m)));
				rowW.add(gear); addDrawableChild(gear);
			}
			KButton sw = new KButton(KButton.Mode.SWITCH, rx + rw - 40, y, 32, ROW_H, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); }).on(() -> m.enabled);
			rowW.add(sw); addDrawableChild(sw);
			KButton hit = new KButton(KButton.Mode.HIT, rx, y, rw, ROW_H, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); });
			rowW.add(hit); addDrawableChild(hit);
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		c.fill(px + 8 + (pw < 400 ? 84 : 110) + 6, py + 28, px + 8 + (pw < 400 ? 84 : 110) + 7, py + ph - 10, Theme.BORDER);
		Theme.text(c, cat.label, rx, py + 10, Theme.DIM);
		for (int i = 0; i < rows; i++) {
			int idx = rowOff + i;
			if (idx >= mods.size()) break;
			Module m = mods.get(idx);
			int y = ry + i * (ROW_H + GAP);
			Theme.box(c, rx, y, rw, ROW_H, Theme.PANEL, Theme.BORDER);
			Theme.text(c, Theme.fit(m.name, rw - 76), rx + 7, y + 5, Theme.TEXT);
			Theme.text(c, Theme.fit(m.desc, rw - 76), rx + 7, y + 16, Theme.DIM);
		}
		int after = ry + Math.min(rows, mods.size() - rowOff) * (ROW_H + GAP);
		if (after + 44 < py + ph - 6) {
			Theme.box(c, rx, after, rw, 40, Theme.PANEL, Theme.BORDER);
			String[] lines = cat.hint.split("\n");
			for (int i = 0; i < lines.length; i++) Theme.text(c, Theme.fit(lines[i], rw - 14), rx + 7, after + 8 + i * 12, Theme.DIM);
		}
		if (mods.size() > rows) Theme.text(c, "Scroll for more", rx, py + ph - 20, Theme.DIM);
	}

	@Override protected boolean onScroll(int dir) { rowOff += dir; rebuildRows(); return true; }
}
