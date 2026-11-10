package com.kelpclient.cosmetic;

import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/** Validates and installs a dropped PNG as the player's skin or cape (config/kelpclient/skin.png, cape.png). */
public final class SkinImport {
	/** Returns null on success, otherwise a short error message to show the player. */
	public static String importSkin(Path src) {
		try {
			BufferedImage in = ImageIO.read(src.toFile());
			if (in == null) return "That file is not a readable PNG.";
			int w = in.getWidth(), h = in.getHeight();
			BufferedImage out;
			if (w == 64 && h == 64) out = argb(in, 64, 64);
			else if (w == 64 && h == 32) out = legacy(in);
			else return "Skins must be 64x64 (or old 64x32). This one is " + w + "x" + h + ".";
			return write(out, "skin.png", "customskin");
		} catch (Throwable t) {
			return "Could not read that file.";
		}
	}

	public static String importCape(Path src) {
		try {
			BufferedImage in = ImageIO.read(src.toFile());
			if (in == null) return "That file is not a readable PNG.";
			if (in.getWidth() < 8 || in.getHeight() < 8) return "That picture is too small for a cape.";
			if (in.getWidth() > 4096 || in.getHeight() > 4096) return "That picture is too large (max 4096 px).";
			return write(argb(in, in.getWidth(), in.getHeight()), "cape.png", "customcape");
		} catch (Throwable t) {
			return "Could not read that file.";
		}
	}

	public static void remove(String kind) {
		File f = new File(CustomAssets.dir(), kind.equals("skin") ? "skin.png" : "cape.png");
		if (f.exists()) f.delete();
		Module m = Modules.BY_ID.get(kind.equals("skin") ? "customskin" : "customcape");
		if (m != null) m.setEnabled(false);
	}

	public static boolean has(String kind) { return new File(CustomAssets.dir(), kind.equals("skin") ? "skin.png" : "cape.png").exists(); }

	private static String write(BufferedImage img, String name, String moduleId) throws Exception {
		File f = new File(CustomAssets.dir(), name);
		if (f.exists()) f.delete(); // a fresh file gets a fresh timestamp, so the game reloads it
		if (!ImageIO.write(img, "png", f)) return "Could not save the image.";
		Module m = Modules.BY_ID.get(moduleId);
		if (m != null) m.setEnabled(true);
		return null;
	}

	private static BufferedImage argb(BufferedImage in, int w, int h) {
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		out.getGraphics().drawImage(in, 0, 0, null);
		return out;
	}

	/** Old 64x32 skins get their left arm and leg filled in from the right ones, like the game does. */
	private static BufferedImage legacy(BufferedImage in) {
		BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		out.getGraphics().drawImage(in, 0, 0, null);
		int[][] r = {{4, 16, 16, 32, 4, 4}, {8, 16, 16, 32, 4, 4}, {0, 20, 24, 32, 4, 12}, {4, 20, 16, 32, 4, 12}, {8, 20, 8, 32, 4, 12}, {12, 20, 16, 32, 4, 12},
				{44, 16, -8, 32, 4, 4}, {48, 16, -8, 32, 4, 4}, {40, 20, 0, 32, 4, 12}, {44, 20, -8, 32, 4, 12}, {48, 20, -16, 32, 4, 12}, {52, 20, -8, 32, 4, 12}};
		for (int[] q : r) {
			for (int i = 0; i < q[5]; i++) for (int j = 0; j < q[4]; j++) {
				int dx = q[0] + q[2] + j, dy = q[1] + q[3] + i;
				if (dx >= 0 && dx < 64 && dy >= 0 && dy < 64) out.setRGB(dx, dy, in.getRGB(q[0] + (q[4] - 1 - j), q[1] + i));
			}
		}
		return out;
	}

	private SkinImport() { }
}
