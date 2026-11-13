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


	/** Copies another model into this one, rotated (z roll first, then y yaw, in radians) and moved. Used for previews. */
	VoxelModel merge(VoxelModel o, float rotY, float rotZ, float tx, float ty, float tz) {
		float cy = (float) Math.cos(rotY), sy = (float) Math.sin(rotY), cz = (float) Math.cos(rotZ), sz = (float) Math.sin(rotZ);
		for (int q = 0; q < o.quads; q++) {
			float[] p = new float[12];
			for (int v = 0; v < 4; v++) {
				float[] r = rot(o.pos[q * 12 + v * 3], o.pos[q * 12 + v * 3 + 1], o.pos[q * 12 + v * 3 + 2], cy, sy, cz, sz);
				p[v * 3] = r[0] + tx; p[v * 3 + 1] = r[1] + ty; p[v * 3 + 2] = r[2] + tz;
			}
			float[] n = rot(o.nrm[q * 3], o.nrm[q * 3 + 1], o.nrm[q * 3 + 2], cy, sy, cz, sz);
			quad(p, o.col[q], n[0], n[1], n[2]);
		}
		return this;
	}

	private static float[] rot(float x, float y, float z, float cy, float sy, float cz, float sz) {
		float x1 = x * cz - y * sz, y1 = x * sz + y * cz;
		return new float[]{x1 * cy + z * sy, y1, -x1 * sy + z * cy};
	}

	/**
	 * Software-renders the model (front = viewer at -z) into an ARGB image of size x size, with a z-buffer and simple
	 * lighting. yaw turns the model around its vertical axis, pitch looks down on it. Transparent where empty.
	 */
	int[] raster(int size, float yaw, float pitch) {
		final int ss = 2, S = size * ss;
		float cy = (float) Math.cos(yaw), sy = (float) Math.sin(yaw), cp = (float) Math.cos(pitch), sp = (float) Math.sin(pitch);
		float[] vx = new float[quads * 4], vy = new float[quads * 4], vz = new float[quads * 4];
		float minX = 1e9f, minY = 1e9f, maxX = -1e9f, maxY = -1e9f;
		for (int q = 0; q < quads; q++) for (int v = 0; v < 4; v++) {
			float x = pos[q * 12 + v * 3], y = pos[q * 12 + v * 3 + 1], z = pos[q * 12 + v * 3 + 2];
			float x1 = x * cy + z * sy, z1 = -x * sy + z * cy;
			float y2 = y * cp - z1 * sp, z2 = y * sp + z1 * cp;
			int i = q * 4 + v;
			vx[i] = x1; vy[i] = y2; vz[i] = z2;
			minX = Math.min(minX, x1); maxX = Math.max(maxX, x1); minY = Math.min(minY, y2); maxY = Math.max(maxY, y2);
		}
		float w = Math.max(0.01f, maxX - minX), h = Math.max(0.01f, maxY - minY);
		float sc = S * 0.9f / Math.max(w, h), ox = S / 2f - (minX + maxX) / 2f * sc, oy = S / 2f - (minY + maxY) / 2f * sc;
		float[] zb = new float[S * S];
		int[] px = new int[S * S];
		Arrays.fill(zb, Float.MAX_VALUE);
		float lx = -0.35f, ly = -0.6f, lz = -0.72f, ll = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
		lx /= ll; ly /= ll; lz /= ll;
		for (int q = 0; q < quads; q++) {
			float nx = nrm[q * 3], ny = nrm[q * 3 + 1], nz = nrm[q * 3 + 2];
			float n1x = nx * cy + nz * sy, n1z = -nx * sy + nz * cy;
			float n2y = ny * cp - n1z * sp, n2z = ny * sp + n1z * cp;
			float shade = 0.5f + 0.5f * Math.max(0f, n1x * lx + n2y * ly + n2z * lz);
			int c = col[q];
			int r = Math.min(255, (int) (((c >> 16) & 255) * shade)), g = Math.min(255, (int) (((c >> 8) & 255) * shade)), b = Math.min(255, (int) ((c & 255) * shade));
			int out = 0xFF000000 | (r << 16) | (g << 8) | b;
			int b0 = q * 4;
			tri(px, zb, S, vx, vy, vz, b0, b0 + 1, b0 + 2, sc, ox, oy, out);
			tri(px, zb, S, vx, vy, vz, b0, b0 + 2, b0 + 3, sc, ox, oy, out);
		}
		int[] res = new int[size * size];
		for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
			int a = 0, r = 0, g = 0, b = 0;
			for (int dy = 0; dy < ss; dy++) for (int dx = 0; dx < ss; dx++) {
				int p = px[(y * ss + dy) * S + x * ss + dx];
				if ((p >>> 24) != 0) { a++; r += (p >> 16) & 255; g += (p >> 8) & 255; b += p & 255; }
			}
			if (a == 0) continue;
			res[y * size + x] = ((a * 255 / (ss * ss)) << 24) | ((r / a) << 16) | ((g / a) << 8) | (b / a);
		}
		return res;
	}

	private static void tri(int[] px, float[] zb, int S, float[] vx, float[] vy, float[] vz, int a, int b, int c, float sc, float ox, float oy, int color) {
		float ax = vx[a] * sc + ox, ay = vy[a] * sc + oy, bx = vx[b] * sc + ox, by = vy[b] * sc + oy, cx = vx[c] * sc + ox, cy = vy[c] * sc + oy;
		float den = (by - cy) * (ax - cx) + (cx - bx) * (ay - cy);
		if (Math.abs(den) < 1e-6f) return;
		int x0 = Math.max(0, (int) Math.floor(Math.min(ax, Math.min(bx, cx)))), x1 = Math.min(S - 1, (int) Math.ceil(Math.max(ax, Math.max(bx, cx))));
		int y0 = Math.max(0, (int) Math.floor(Math.min(ay, Math.min(by, cy)))), y1 = Math.min(S - 1, (int) Math.ceil(Math.max(ay, Math.max(by, cy))));
		for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) {
			float fx = x + 0.5f, fy = y + 0.5f;
			float l1 = ((by - cy) * (fx - cx) + (cx - bx) * (fy - cy)) / den;
			float l2 = ((cy - ay) * (fx - cx) + (ax - cx) * (fy - cy)) / den;
			float l3 = 1 - l1 - l2;
			if (l1 < -0.001f || l2 < -0.001f || l3 < -0.001f) continue;
			float z = l1 * vz[a] + l2 * vz[b] + l3 * vz[c];
			int i = y * S + x;
			if (z < zb[i]) { zb[i] = z; px[i] = color; }
		}
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
