package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;

/** Tiny pixel-art icons drawn with fills, so the mod needs no texture files. */
public final class Icons {
	private static final Map<String, String[]> M = new HashMap<>();
	private static final Map<Character, Integer> P = new HashMap<>();

	private static void def(String k, String... rows) { M.put(k, rows); }

	static {
		P.put('g', 0xFF3CD16B); P.put('G', 0xFF1E8A45); P.put('l', 0xFFB6F5C8);
		P.put('b', 0xFF4AA3F0); P.put('B', 0xFF2B6CB0);
		P.put('r', 0xFFE5484D); P.put('R', 0xFF9B2226);
		P.put('y', 0xFFF2C230); P.put('Y', 0xFFB8860B);
		P.put('w', 0xFFEFF5F2); P.put('a', 0xFF9AA5A1); P.put('A', 0xFF5C6663);
		P.put('k', 0xFF101A18); P.put('o', 0xFFE9822B); P.put('p', 0xFF8B5A2B); P.put('v', 0xFFA855F7);

		def("kelp", ".....gg..", "....gg...", "...gg....", "..gg.....", "...ggg...", ".....gg..", "....gg...", "...gg....", "..gg.....");
		def("shirt", ".bb...bb.", "bbbb.bbbb", "bbbbbbbbb", "bbbbbbbbb", ".bbbbbbb.", "..bbbbb..", "..bbbbb..", "..bbbbb..", "..bbbbb..");
		def("pin", "...rrr...", "..rrrrr..", ".rrrwrrr.", ".rrwwwrr.", ".rrrwrrr.", "..rrrrr..", "...rrr...", "....r....", "....r....");
		def("person", "...yyy...", "..yyyyy..", "..yyyyy..", "...yyy...", ".yyyyyyy.", "yyyyyyyyy", "yyyyyyyyy", "yyyyyyyyy", "yyyyyyyyy");
		def("arrows", ".....g...", ".....gg..", "ggggggggg", ".....gg..", ".....g...", "...w.....", "..ww.....", "wwwwwwwww", "..ww.....", "...w.....");
		def("gear", "...a.a...", ".a.aaa.a.", "..aaaaa..", ".aaa.aaa.", "aaaa.aaaa", ".aaa.aaa.", "..aaaaa..", ".a.aaa.a.", "...a.a...");
		def("monitor", "aaaaaaaaa", "akkkkkkka", "akgggkkka", "akkkkkkka", "akgggggka", "akkkkkkka", "aaaaaaaaa", "...aaa...", "..aaaaa..");
		def("sword", "........w", ".......ww", "......ww.", ".....ww..", "....ww...", ".y.ww....", "..yw.....", ".pyy.....", "pp.......");
		def("cross", "....g....", "....g....", ".........", "gg..r..gg", ".........", "....g....", "....g....");
		def("heart", ".rr...rr.", "rrrr.rrrr", "rrrrrrrrr", "rrrrrrrrr", ".rrrrrrr.", "..rrrrr..", "...rrr...", "....r....");
		def("clock", "..aaaaa..", ".awwwwwa.", "awwwkwwwa", "awwwkwwwa", "awwwkkkwa", "awwwwwwwa", ".awwwwwa.", "..aaaaa..");
		def("compass", "..aaaaa..", ".a..r..a.", "a..rrr..a", "a..rrr..a", "a..www..a", "a..www..a", ".a..w..a.", "..aaaaa..");
		def("chip", ".a.a.a.a.", "aaaaaaaaa", ".akkkkka.", "aakgggkaa", ".akgggka.", "aakgggkaa", ".akkkkka.", "aaaaaaaaa", ".a.a.a.a.");
		def("mouse", "..aaaaa..", ".aaawaaa.", ".aaawaaa.", ".aaaaaaa.", ".aaaaaaa.", ".aaaaaaa.", ".aaaaaaa.", "..aaaaa..");
		def("list", "gg.wwwwww", ".........", "gg.wwwwww", ".........", "gg.wwwwww", ".........", "gg.wwwwww");
		def("keys", "....aaa....", "....a.a....", "....aaa....", "aaa.aaa.aaa", "a.a.a.a.a.a", "aaa.aaa.aaa");
		def("bolt", "....yyy..", "...yyy...", "..yyy....", ".yyyyyyy.", "....yyy..", "...yyy...", "..yy.....", ".y.......");
		def("sun", "....y....", ".y..y..y.", "..yyyyy..", "yyyyyyyyy", "..yyyyy..", ".y..y..y.", "....y....");
		def("moon", "..bbbb...", ".bbb.....", "bbb......", "bbb......", "bbb......", ".bbb.....", "..bbbb...");
		def("eye", "...aaa...", ".aawwwaa.", "aawwkwwaa", ".aawwwaa.", "...aaa...");
		def("cube", ".aaaaaaa.", ".a.....a.", ".a.....a.", ".a.....a.", ".aaaaaaa.");
		def("food", "..ooo....", ".ooooo...", ".ooooo...", "..ooo....", "...ow....", "....w....", "...www...");
		def("star", "....y....", "....y....", "yyyyyyyyy", ".yyyyyyy.", "..yyyyy..", ".yyy.yyy.", ".yy...yy.");
		def("bars", ".......g.", ".......g.", "....g..g.", "....g..g.", ".g..g..g.", ".g..g..g.");
		def("chat", "wwwwwwwww", "w.......w", "w.......w", "wwwwwwwww", "..ww.....", "...w.....");
		def("armor", "aa.....aa", "aaa...aaa", "aaaaaaaaa", ".aaaaaaa.", ".aaaaaaa.", ".aaaaaaa.", "..aaaaa..");
		def("potion", "...www...", "....a....", "...aaa...", "..avvva..", ".avvvvva.", ".avvvvva.", "..aaaaa..");
		def("tree", "..ggg....", ".ggggg...", "ggggggg..", ".ggggg...", "...p.....", "...p.....");
		def("x", "a.....a", ".a...a.", "..a.a..", "...a...", "..a.a..", ".a...a.", "a.....a");
	}

	public static boolean has(String k) { return M.containsKey(k); }
	public static int w(String k) { String[] r = M.get(k); if (r == null) return 0; int m = 0; for (String s : r) m = Math.max(m, s.length()); return m; }
	public static int h(String k) { String[] r = M.get(k); return r == null ? 0 : r.length; }

	public static void draw(DrawContext c, String k, int x, int y, int s) { draw(c, k, x, y, s, 0, 99); }

	/** tint != 0 paints every pixel in that colour; maxCols limits columns (used for half hearts). */
	public static void draw(DrawContext c, String k, int x, int y, int s, int tint, int maxCols) {
		String[] r = M.get(k);
		if (r == null) return;
		for (int row = 0; row < r.length; row++) {
			String line = r[row];
			int col = 0;
			while (col < line.length() && col < maxCols) {
				char ch = line.charAt(col);
				if (ch == '.') { col++; continue; }
				int start = col;
				while (col < line.length() && col < maxCols && line.charAt(col) == ch) col++;
				Integer color = tint != 0 ? Integer.valueOf(tint) : P.get(ch);
				if (color == null) continue;
				c.fill(x + start * s, y + row * s, x + col * s, y + (row + 1) * s, color);
			}
		}
	}

	public static void drawCentered(DrawContext c, String k, int cx, int cy, int s) { drawCentered(c, k, cx, cy, s, 0); }

	public static void drawCentered(DrawContext c, String k, int cx, int cy, int s, int tint) {
		draw(c, k, cx - w(k) * s / 2, cy - h(k) * s / 2, s, tint, 99);
	}
}
