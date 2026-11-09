package com.kelpclient.cosmetic;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/** The player's own files: config/kelpclient/skin.png (64x64 skin) and config/kelpclient/cape.png (any picture). */
public final class CustomAssets {
	public static final class CapeArt {
		final String[] rows; final Map<Character, Integer> pal; final long stamp;
		CapeArt(String[] rows, Map<Character, Integer> pal, long stamp) { this.rows = rows; this.pal = pal; this.stamp = stamp; }
	}

	public static File dir() {
		File d = FabricLoader.getInstance().getConfigDir().resolve("kelpclient").toFile();
		d.mkdirs();
		return d;
	}

	private static long skinStamp = -1, capeStamp = -1;
	private static Identifier skinId;
	private static CapeArt capeArt;

	/** Texture id of skin.png, or null when there is no (valid) file. */
	public static Identifier skin() {
		File f = new File(dir(), "skin.png");
		long stamp = f.exists() ? f.lastModified() : 0;
		if (stamp == skinStamp) return skinId;
		skinStamp = stamp;
		skinId = null;
		if (stamp == 0) return null;
		try (InputStream in = Files.newInputStream(f.toPath())) {
			NativeImage img = NativeImage.read(in);
			NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "kelp_custom_skin", img);
			tex.upload();
			Identifier id = Identifier.of("kelpclient", "custom_skin/s" + stamp);
			MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
			skinId = id;
		} catch (Throwable ignored) { }
		return skinId;
	}

	/** cape.png squeezed into a 20x32 pixel grid, or null when there is no (valid) file. */
	public static CapeArt cape() {
		File f = new File(dir(), "cape.png");
		long stamp = f.exists() ? f.lastModified() : 0;
		if (stamp == capeStamp) return capeArt;
		capeStamp = stamp;
		capeArt = null;
		if (stamp == 0) return null;
		try {
			BufferedImage src = ImageIO.read(f);
			if (src == null) return null;
			int W = 20, H = 32, sw = src.getWidth(), sh = src.getHeight();
			Map<Character, Integer> pal = new HashMap<>();
			Map<Integer, Character> seen = new HashMap<>();
			String[] rows = new String[H];
			for (int y = 0; y < H; y++) {
				StringBuilder sb = new StringBuilder();
				int y0 = y * sh / H, y1 = Math.max(y0 + 1, (y + 1) * sh / H);
				for (int x = 0; x < W; x++) {
					int x0 = x * sw / W, x1 = Math.max(x0 + 1, (x + 1) * sw / W);
					long a = 0, r = 0, g = 0, b = 0; int n = 0;
					for (int yy = y0; yy < y1 && yy < sh; yy++) for (int xx = x0; xx < x1 && xx < sw; xx++) {
						int p = src.getRGB(xx, yy);
						a += (p >>> 24); r += (p >> 16) & 255; g += (p >> 8) & 255; b += p & 255; n++;
					}
					n = Math.max(1, n);
					if (a / n < 100) { sb.append('.'); continue; }
					// quantise a little so similar pixels share a palette entry
					int rgb = (((int) (r / n) & 0xF8) << 16) | (((int) (g / n) & 0xF8) << 8) | ((int) (b / n) & 0xF8);
					Character ch = seen.get(rgb);
					if (ch == null) { ch = (char) (0x100 + seen.size()); seen.put(rgb, ch); pal.put(ch, rgb); }
					sb.append(ch.charValue());
				}
				rows[y] = sb.toString();
			}
			capeArt = new CapeArt(rows, pal, stamp);
		} catch (Throwable ignored) { }
		return capeArt;
	}

	private CustomAssets() { }
}
