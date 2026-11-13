package com.kelpclient.cosmetic;

import com.kelpclient.module.Module;
import com.kelpclient.ui.Icons;
import net.minecraft.client.gui.DrawContext;

import java.util.Map;

/** Small 2D previews of cosmetics for the Wardrobe cards (drawn from the same pixel art as the 3D models). */
public final class CosmeticPreview {
	public static void draw(DrawContext c, Module m, int x, int y, int w, int h) {
		if (ModelPreview.draw(c, m, x, y, w, h)) return;
		int hue = 0;
		for (com.kelpclient.module.Setting s : m.settings) if (s.key.equals("color")) hue = s.v;
		switch (m.id) {
			case "sword":
				grid(c, CosmeticArt.SWORD, CosmeticArt.SWORD_PAL, x, y, w, h, false, hue);
				return;
			case "wings": {
				int half = w / 2;
				grid(c, CosmeticArt.WING, CosmeticArt.WING_PAL, x, y, half * 2, h, true, hue, -1);
				return;
			}
			case "customcape": {
				CustomAssets.CapeArt a = CustomAssets.cape();
				if (a != null) { grid(c, a.rows, a.pal, x, y, w, h, false, 0); return; }
				break;
			}
			default:
		}
		int s = Math.max(1, Math.min(2, Icons.fit(m.icon, Math.min(w, h) - 6)));
		Icons.drawCentered(c, m.icon, x + w / 2, y + h / 2, s);
	}

	private static void grid(DrawContext c, String[] rows, Map<Character, Integer> pal, int x, int y, int w, int h, boolean pair, int hue) { grid(c, rows, pal, x, y, w, h, pair, hue, 1); }

	/** pair = draw the art and its mirror image side by side (wings). */
	private static void grid(DrawContext c, String[] rows, Map<Character, Integer> pal, int x, int y, int w, int h, boolean pair, int hue, int unused) {
		int cols = 0;
		for (String r : rows) cols = Math.max(cols, r.length());
		int totalCols = pair ? cols * 2 : cols;
		int s = Math.max(1, Math.min((w - 4) / totalCols, (h - 4) / rows.length));
		int ox = x + (w - totalCols * s) / 2, oy = y + (h - rows.length * s) / 2;
		for (int r = 0; r < rows.length; r++) {
			for (int col = 0; col < rows[r].length(); col++) {
				char ch = rows[r].charAt(col);
				Integer rgb = pal.get(ch);
				if (ch == '.' || rgb == null) continue;
				int argb = Colors.shift(0xFF000000 | rgb, hue);
				if (pair) {
					c.fill(ox + (cols + col) * s, oy + r * s, ox + (cols + col + 1) * s, oy + (r + 1) * s, argb);
					c.fill(ox + (cols - 1 - col) * s, oy + r * s, ox + (cols - col) * s, oy + (r + 1) * s, argb);
				} else {
					c.fill(ox + col * s, oy + r * s, ox + (col + 1) * s, oy + (r + 1) * s, argb);
				}
			}
		}
	}

	private CosmeticPreview() { }
}
