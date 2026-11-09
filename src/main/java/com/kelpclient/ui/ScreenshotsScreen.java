package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** In-game screenshot gallery: square, sharp thumbnails of every screenshot you have taken. */
public class ScreenshotsScreen extends KScreen {
	private static final int GAP = 6, TARGET = 84;
	private List<File> files = new ArrayList<>();
	private int cols, rows, rowOff, gx, gy, gh, tile;
	private final List<ClickableWidget> tileW = new ArrayList<>();

	public ScreenshotsScreen(Screen parent) { super("SCREENSHOTS", parent); }

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 640);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		files = ShotCache.list(client);
		int gw = pw - 20 - 8;
		cols = Math.max(2, (gw + GAP) / (TARGET + GAP));
		tile = Math.min(120, (gw - (cols - 1) * GAP) / cols);
		gh = ph - 28 - 34;
		rows = Math.max(1, (gh + GAP) / (tile + GAP));
		gx = px + 10 + (gw - (cols * tile + (cols - 1) * GAP)) / 2;
		gy = py + 28;
		rebuild();
	}

	private void rebuild() {
		for (ClickableWidget w : tileW) remove(w);
		tileW.clear();
		int total = (files.size() + cols - 1) / cols;
		rowOff = Math.max(0, Math.min(rowOff, Math.max(0, total - rows)));
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int idx = (rowOff + r) * cols + c;
				if (idx >= files.size()) break;
				final int index = idx;
				KButton hit = new KButton(KButton.Mode.HIT, gx + c * (tile + GAP), gy + r * (tile + GAP), tile, tile, "", null,
						b -> client.setScreen(new ScreenshotViewScreen(this, files, index)));
				tileW.add(hit); addDrawableChild(hit);
			}
		}
	}

	/** "2026-11-08_19.07.45.png" becomes "11-08 19:07". */
	static String label(String file) {
		String n = file.replace(".png", "");
		if (n.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}\\.\\d{2}\\.\\d{2}.*")) return n.substring(5, 10) + " " + n.substring(11, 13) + ":" + n.substring(14, 16);
		return n;
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		ShotCache.beginFrame();
		String count = files.size() + (files.size() == 1 ? " screenshot" : " screenshots");
		Theme.text(c, count, px + pw - 10 - Theme.tw(count), py + 10, Theme.DIM);
		if (files.isEmpty()) {
			Theme.centered(c, "No screenshots yet.", px + pw / 2, py + 60, Theme.TEXT);
			Theme.centered(c, "Press F2 in a world to take one - it will show up here.", px + pw / 2, py + 74, Theme.DIM);
			return;
		}
		for (int r = 0; r < rows; r++) {
			for (int col = 0; col < cols; col++) {
				int idx = (rowOff + r) * cols + col;
				if (idx >= files.size()) break;
				File f = files.get(idx);
				int x = gx + col * (tile + GAP), y = gy + r * (tile + GAP);
				boolean over = mx >= x && mx < x + tile && my >= y && my < y + tile;
				Theme.box(c, x, y, tile, tile, 0xFF050B09, over ? Theme.ACCENT : Theme.BORDER);
				ShotCache.Tex t = ShotCache.get(f, true);
				if (t == null) {
					Theme.centered(c, "...", x + tile / 2, y + tile / 2 - 4, Theme.DIM);
				} else if (t.failed) {
					Theme.centered(c, "Unreadable", x + tile / 2, y + tile / 2 - 4, Theme.RED);
				} else {
					ShotCache.draw(c, t, x + 1, y + 1, tile - 2, tile - 2);
				}
				c.fill(x + 1, y + tile - 13, x + tile - 1, y + tile - 1, 0xB0050D0B);
				Theme.text(c, Theme.fit(label(f.getName()), tile - 8), x + 4, y + tile - 11, over ? Theme.TEXT : Theme.DIM);
			}
		}
		int total = (files.size() + cols - 1) / cols;
		if (total > rows) {
			int th = Math.max(16, gh * rows / total);
			int ty = gy + (gh - th) * rowOff / Math.max(1, total - rows);
			c.fill(px + pw - 12, gy, px + pw - 9, gy + gh, 0xFF10201C);
			c.fill(px + pw - 12, ty, px + pw - 9, ty + th, Theme.ACCENT);
		}
	}

	@Override protected boolean onScroll(int dir) { rowOff += dir; rebuild(); return true; }
}
