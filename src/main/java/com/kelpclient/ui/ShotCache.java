package com.kelpclient.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Screenshots as real GPU textures (no more blocky pixel approximations).
 * Decoding and down-scaling (area average, so it stays sharp) happens on worker threads;
 * the texture upload happens on the render thread, a couple per frame so scrolling never stutters.
 */
public final class ShotCache {
	public static final int THUMB = 256, FULL_W = 1280, FULL_H = 720;

	public static final class Tex {
		public final Identifier id;
		public final int w, h, srcW, srcH;
		public final boolean failed;
		Tex(Identifier id, int w, int h, int srcW, int srcH, boolean failed) { this.id = id; this.w = w; this.h = h; this.srcW = srcW; this.srcH = srcH; this.failed = failed; }
	}

	private static final class Decoded {
		int w, h, srcW, srcH;
		int[] argb;
		boolean failed;
	}

	private static final Map<String, Tex> THUMBS = new LinkedHashMap<>(64, 0.75f, true);
	private static final Map<String, Tex> FULLS = new LinkedHashMap<>(8, 0.75f, true);
	private static final Map<String, Decoded> READY = new ConcurrentHashMap<>();
	private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
	private static final ExecutorService EX = Executors.newFixedThreadPool(3, r -> {
		Thread t = new Thread(r, "kelp-screenshots");
		t.setDaemon(true);
		return t;
	});
	private static int budget = 2, counter;

	/** Call once per rendered frame by the screens that show screenshots. */
	public static void beginFrame() { budget = 2; }

	/** Every PNG in the game's screenshots folder, newest first. */
	public static List<File> list(MinecraftClient mc) {
		File dir = new File(mc.runDirectory, "screenshots");
		File[] fs = dir.listFiles((d, n) -> n.toLowerCase(Locale.ROOT).endsWith(".png"));
		List<File> out = new ArrayList<>();
		if (fs != null) out.addAll(Arrays.asList(fs));
		out.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
		return out;
	}

	/** Returns the texture if it is ready, otherwise starts loading it and returns null. */
	public static Tex get(File f, boolean thumb) {
		String key = f.getAbsolutePath() + "|" + (thumb ? "t" : "f") + "|" + f.lastModified();
		Map<String, Tex> map = thumb ? THUMBS : FULLS;
		Tex t = map.get(key);
		if (t != null) return t;
		Decoded d = READY.get(key);
		if (d != null) {
			if (budget <= 0) return null;
			budget--;
			READY.remove(key);
			t = upload(d);
			map.put(key, t);
			evict(map, thumb ? 160 : 3);
			return t;
		}
		if (PENDING.add(key)) {
			EX.submit(() -> {
				Decoded out;
				try { out = decode(f, thumb); }
				catch (Throwable e) { out = new Decoded(); out.failed = true; }
				READY.put(key, out);
				PENDING.remove(key);
			});
		}
		return null;
	}

	private static void evict(Map<String, Tex> map, int max) {
		while (map.size() > max) {
			Iterator<Map.Entry<String, Tex>> it = map.entrySet().iterator();
			Map.Entry<String, Tex> e = it.next();
			it.remove();
			if (e.getValue().id != null) MinecraftClient.getInstance().getTextureManager().destroyTexture(e.getValue().id);
		}
	}

	private static Tex upload(Decoded d) {
		if (d.failed || d.argb == null) return new Tex(null, 0, 0, 0, 0, true);
		NativeImage img = new NativeImage(NativeImage.Format.RGBA, d.w, d.h, false);
		for (int y = 0; y < d.h; y++) {
			for (int x = 0; x < d.w; x++) img.setColorArgb(x, y, d.argb[y * d.w + x]);
		}
		Identifier id = Identifier.of("kelpclient", "shots/s" + (counter++));
		NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "kelp_screenshot", img);
		tex.upload();
		MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
		return new Tex(id, d.w, d.h, d.srcW, d.srcH, false);
	}

	private static Decoded decode(File f, boolean thumb) throws IOException {
		BufferedImage src = ImageIO.read(f);
		if (src == null) throw new IOException("unreadable image");
		int sw = src.getWidth(), sh = src.getHeight();
		int[] px = src.getRGB(0, 0, sw, sh, null, 0, sw);
		Decoded d = new Decoded();
		d.srcW = sw; d.srcH = sh;
		if (thumb) {
			// centre-crop to a square, then area-average down to THUMB x THUMB
			int side = Math.min(sw, sh), cx = (sw - side) / 2, cy = (sh - side) / 2;
			int out = Math.min(THUMB, side);
			d.w = out; d.h = out;
			d.argb = scale(px, sw, cx, cy, side, side, out, out);
		} else {
			double s = Math.min(1.0, Math.min(FULL_W / (double) sw, FULL_H / (double) sh));
			int tw = Math.max(1, (int) Math.round(sw * s)), th = Math.max(1, (int) Math.round(sh * s));
			d.w = tw; d.h = th;
			d.argb = scale(px, sw, 0, 0, sw, sh, tw, th);
		}
		return d;
	}

	/** Box-filter resize of a region of an ARGB image. Averaging whole source blocks keeps detail instead of skipping pixels. */
	private static int[] scale(int[] src, int stride, int rx, int ry, int rw, int rh, int tw, int th) {
		int[] out = new int[tw * th];
		if (tw == rw && th == rh) {
			for (int y = 0; y < th; y++) for (int x = 0; x < tw; x++) out[y * tw + x] = 0xFF000000 | src[(ry + y) * stride + rx + x];
			return out;
		}
		for (int y = 0; y < th; y++) {
			int y0 = ry + (int) ((long) y * rh / th), y1 = Math.max(y0 + 1, ry + (int) ((long) (y + 1) * rh / th));
			for (int x = 0; x < tw; x++) {
				int x0 = rx + (int) ((long) x * rw / tw), x1 = Math.max(x0 + 1, rx + (int) ((long) (x + 1) * rw / tw));
				long r = 0, g = 0, b = 0;
				int n = 0;
				for (int yy = y0; yy < y1; yy++) {
					int row = yy * stride;
					for (int xx = x0; xx < x1; xx++) {
						int p = src[row + xx];
						r += (p >> 16) & 255; g += (p >> 8) & 255; b += p & 255; n++;
					}
				}
				n = Math.max(1, n);
				out[y * tw + x] = 0xFF000000 | ((int) (r / n) << 16) | ((int) (g / n) << 8) | (int) (b / n);
			}
		}
		return out;
	}

	/** Draws the whole texture stretched into the given box. */
	public static void draw(DrawContext c, Tex t, int x, int y, int w, int h) {
		if (t == null || t.id == null) return;
		c.drawTexture(RenderPipelines.GUI_TEXTURED, t.id, x, y, 0f, 0f, w, h, t.w, t.h, t.w, t.h);
	}
}
