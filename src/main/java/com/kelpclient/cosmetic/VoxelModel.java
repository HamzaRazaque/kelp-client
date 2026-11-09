package com.kelpclient.cosmetic;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Arrays;
import java.util.Map;

/** A pre-baked set of coloured quads, in model pixels (1/16 block, y down, back of the player is +z). */
final class VoxelModel {
	private float[] pos = new float[256];
	private int[] col = new int[64];
	private float[] nrm = new float[64];
	private int quads;

	private void quad(float[] p, int color, float nx, float ny, float nz) {
		if ((quads + 1) * 12 > pos.length) pos = Arrays.copyOf(pos, pos.length * 2);
		if (quads + 1 > col.length) col = Arrays.copyOf(col, col.length * 2);
		if ((quads + 1) * 3 > nrm.length) nrm = Arrays.copyOf(nrm, nrm.length * 2);
		System.arraycopy(p, 0, pos, quads * 12, 12);
		col[quads] = color;
		nrm[quads * 3] = nx; nrm[quads * 3 + 1] = ny; nrm[quads * 3 + 2] = nz;
		quads++;
	}

	/** Axis-aligned box; mask bits: 1=-x 2=+x 4=-y 8=+y 16=-z 32=+z are the faces that get drawn. */
	VoxelModel box(float x0, float y0, float z0, float x1, float y1, float z1, int argb, int mask) {
		if ((mask & 1) != 0) quad(new float[]{x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1}, argb, -1, 0, 0);
		if ((mask & 2) != 0) quad(new float[]{x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0}, argb, 1, 0, 0);
		if ((mask & 4) != 0) quad(new float[]{x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0}, argb, 0, -1, 0);
		if ((mask & 8) != 0) quad(new float[]{x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1}, argb, 0, 1, 0);
		if ((mask & 16) != 0) quad(new float[]{x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z0}, argb, 0, 0, -1);
		if ((mask & 32) != 0) quad(new float[]{x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1}, argb, 0, 0, 1);
		return this;
	}

	VoxelModel box(float x0, float y0, float z0, float x1, float y1, float z1, int argb) { return box(x0, y0, z0, x1, y1, z1, argb, 63); }

	/** Box given by its centre-bottom and size, handy for animals. Up is -y. */
	VoxelModel cube(float cx, float bottom, float cz, float w, float h, float d, int argb) {
		return box(cx - w / 2, bottom - h, cz - d / 2, cx + w / 2, bottom, cz + d / 2, argb);
	}

	/**
	 * Extrudes a pixel-art grid into voxels. Column 0 is at x = ox and columns grow by {@code dirX} (+1 or -1),
	 * rows grow downwards from oy. Hidden faces between neighbouring pixels are skipped.
	 */
	VoxelModel grid(String[] rows, Map<Character, Integer> pal, float ox, float oy, float z0, float px, float depth, int dirX, int hue) {
		int h = rows.length;
		for (int r = 0; r < h; r++) {
			for (int c = 0; c < rows[r].length(); c++) {
				char ch = rows[r].charAt(c);
				if (ch == '.' || !pal.containsKey(ch)) continue;
				int color = Colors.shift(0xFF000000 | pal.get(ch), hue);
				float a = ox + dirX * c * px, b = ox + dirX * (c + 1) * px;
				float xa = Math.min(a, b), xb = Math.max(a, b);
				float ya = oy + r * px, yb = ya + px;
				int mask = 16 | 32;
				boolean left = filled(rows, pal, r, c - 1), right = filled(rows, pal, r, c + 1);
				boolean lo = dirX > 0 ? left : right, hi = dirX > 0 ? right : left;
				if (!lo) mask |= 1;
				if (!hi) mask |= 2;
				if (!filled(rows, pal, r - 1, c)) mask |= 4;
				if (!filled(rows, pal, r + 1, c)) mask |= 8;
				box(xa, ya, z0, xb, yb, z0 + depth, color, mask);
			}
		}
		return this;
	}

	private static boolean filled(String[] rows, Map<Character, Integer> pal, int r, int c) {
		if (r < 0 || r >= rows.length || c < 0 || c >= rows[r].length()) return false;
		char ch = rows[r].charAt(c);
		return ch != '.' && pal.containsKey(ch);
	}

	void emit(MatrixStack.Entry e, VertexConsumer vc, int light) {
		final float k = 1f / 16f;
		for (int q = 0; q < quads; q++) {
			int color = col[q];
			for (int v = 0; v < 4; v++) {
				int i = q * 12 + v * 3;
				vc.vertex(e, pos[i] * k, pos[i + 1] * k, pos[i + 2] * k)
						.color(color)
						.texture(0.5f, 0.5f)
						.overlay(OverlayTexture.DEFAULT_UV)
						.light(light)
						.normal(e, nrm[q * 3], nrm[q * 3 + 1], nrm[q * 3 + 2]);
			}
		}
	}
}
