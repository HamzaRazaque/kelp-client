package com.kelpclient.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads screenshots off-thread and turns them into small run-length "pixel" images that are drawn with plain fills.
 * This avoids the texture-upload APIs, which change a lot between Minecraft versions.
 */
public final class ShotCache {
	public static final class Img {
		public final int w, h, srcW, srcH;
		public final int[] runs; // x, y, length, rgb  (4 ints per run)
		Img(int w, int h, int srcW, int srcH, int[] runs) { this.w = w; this.h = h; this.srcW = srcW; this.srcH = srcH; this.runs = runs; }
	}

	private static final Map<String, Img> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, Img>(16, 0.75f, true) {
		@Override protected boolean removeEldestEntry(Map.Entry<String, Img> e) { return size() > 90; }
	});
	private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
	private static final ExecutorService EX = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "kelp-screenshots");
		t.setDaemon(true);
		return t;
	});

	/** Every PNG in the game's screenshots folder, newest first. */
	public static List<File> list(MinecraftClient mc) {
		File dir = new File(mc.runDirectory, "screenshots");
		File[] fs = dir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".png"));
		List<File> out = new ArrayList<>();
		if (fs != null) out.addAll(Arrays.asList(fs));
		out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
		return out;
	}

	/** Returns the image if ready, otherwise starts loading it and returns null. */
	public static Img get(File f, int cellsW, int cellsH) {
		String key = f.getAbsolutePath() + "@" + cellsW + "x" + cellsH + "@" + f.lastModified();
		Img i = CACHE.get(key);
		if (i == null && PENDING.add(key)) {
			EX.submit(() -> {
				try { CACHE.put(key, build(f, cellsW, cellsH)); }
				catch (Throwable t) { CACHE.put(key, new Img(0, 0, 0, 0, new int[0])); }
				finally { PENDING.remove(key); }
			});
		}
		return i;
	}

	private static Img build(File f, int cw, int ch) throws IOException {
		BufferedImage src = ImageIO.read(f);
		if (src == null) throw new IOException("unreadable image");
		int sw = src.getWidth(), sh = src.getHeight();
		double sc = Math.min(cw / (double) sw, ch / (double) sh);
		int tw = Math.max(1, (int) Math.round(sw * sc)), th = Math.max(1, (int) Math.round(sh * sc));
		int[] q = new int[tw * th];
		for (int y = 0; y < th; y++) {
			int y0 = y * sh / th, y1 = Math.max(y0 + 1, (y + 1) * sh / th);
			int sy = Math.max(1, (y1 - y0) / 3);
			for (int x = 0; x < tw; x++) {
				int x0 = x * sw / tw, x1 = Math.max(x0 + 1, (x + 1) * sw / tw);
				int sx = Math.max(1, (x1 - x0) / 3);
				long r = 0, g = 0, b = 0;
				int n = 0;
				for (int yy = y0; yy < y1 && yy < sh; yy += sy) {
					for (int xx = x0; xx < x1 && xx < sw; xx += sx) {
						int p = src.getRGB(xx, yy);
						r += (p >> 16) & 255; g += (p >> 8) & 255; b += p & 255; n++;
					}
				}
				n = Math.max(1, n);
				int rr = (int) (r / n) & 0xF0, gg = (int) (g / n) & 0xF0, bb = (int) (b / n) & 0xF0;
				rr |= rr >> 4; gg |= gg >> 4; bb |= bb >> 4;
				q[y * tw + x] = (rr << 16) | (gg << 8) | bb;
			}
		}
		int[] buf = new int[4096];
		int len = 0;
		for (int y = 0; y < th; y++) {
			int x = 0;
			while (x < tw) {
				int col = q[y * tw + x], x2 = x + 1;
				while (x2 < tw && q[y * tw + x2] == col) x2++;
				if (len + 4 > buf.length) buf = Arrays.copyOf(buf, buf.length * 2);
				buf[len++] = x; buf[len++] = y; buf[len++] = x2 - x; buf[len++] = col;
				x = x2;
			}
		}
		return new Img(tw, th, sw, sh, Arrays.copyOf(buf, len));
	}

	public static void draw(DrawContext c, Img i, int ox, int oy, int s) {
		int[] r = i.runs;
		for (int k = 0; k < r.length; k += 4) {
			c.fill(ox + r[k] * s, oy + r[k + 1] * s, ox + (r[k] + r[k + 2]) * s, oy + (r[k + 1] + 1) * s, 0xFF000000 | r[k + 3]);
		}
	}
}
