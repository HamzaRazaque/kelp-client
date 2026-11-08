package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** In-game screenshot gallery: every screenshot you have taken, shown inside Minecraft. */
public class ScreenshotsScreen extends KScreen {
	private static final int CELLS_W = 48, CELLS_H = 27, S = 2, TILE_W = CELLS_W * S + 8, TILE_H = CELLS_H * S + 26, GAP = 6;
	private List<File> files = new ArrayList<>();
	private int cols, rows, rowOff, gx, gy;
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
		cols = Math.max(1, (pw - 20 + GAP) / (TILE_W + GAP));
		rows = Math.max(1, (ph - 30 - 34 + GAP) / (TILE_H + GAP));
		gx = px + (pw - (cols * TILE_W + (cols - 1) * GAP)) / 2;
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
				KButton hit = new KButton(KButton.Mode.HIT, gx + c * (TILE_W + GAP), gy + r * (TILE_H + GAP), TILE_W, TILE_H, "", null,
						b -> client.setScreen(new ScreenshotViewScreen(this, files, index)));
				tileW.add(hit); addDrawableChild(hit);
			}
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
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
				if (idx >= files.size()) return;
				File f = files.get(idx);
				int x = gx + col * (TILE_W + GAP), y = gy + r * (TILE_H + GAP);
				Theme.box(c, x, y, TILE_W, TILE_H, Theme.PANEL, Theme.BORDER);
				ShotCache.Img img = ShotCache.get(f, CELLS_W, CELLS_H);
				if (img == null) {
					Theme.centered(c, "Loading...", x + TILE_W / 2, y + 4 + CELLS_H * S / 2 - 4, Theme.DIM);
				} else if (img.w == 0) {
					Theme.centered(c, "Unreadable", x + TILE_W / 2, y + 4 + CELLS_H * S / 2 - 4, Theme.RED);
				} else {
					ShotCache.draw(c, img, x + (TILE_W - img.w * S) / 2, y + 4 + (CELLS_H - img.h) * S / 2, S);
				}
				String name = f.getName().replace(".png", "");
				Theme.text(c, Theme.fit(name, TILE_W - 8), x + 4, y + TILE_H - 14, Theme.DIM);
			}
		}
		int total = (files.size() + cols - 1) / cols;
		if (total > rows) {
			int bh = ph - 30 - 34, th = Math.max(16, bh * rows / total);
			int ty = gy + (bh - th) * rowOff / Math.max(1, total - rows);
			c.fill(px + pw - 7, gy, px + pw - 4, gy + bh, 0xFF10201C);
			c.fill(px + pw - 7, ty, px + pw - 4, ty + th, Theme.ACCENT);
		}
	}

	@Override protected boolean onScroll(int dir) { rowOff += dir; rebuild(); return true; }
}
