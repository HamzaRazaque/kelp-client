package com.kelpclient.cosmetic;

/** Hue shifting so one piece of art can come in many colours. */
public final class Colors {
	static final float[] HUES = {0f, 215f, 120f, 280f, 330f, 45f, 175f};
	public static final String[] NAMES = {"Default", "Blue", "Green", "Purple", "Pink", "Gold", "Cyan"};

	/** Rotates the hue of an ARGB colour by the given variant's offset (variant 0 returns it untouched). */
	static int shift(int argb, int variant) {
		if (variant <= 0 || variant >= HUES.length) return argb;
		float r = ((argb >> 16) & 255) / 255f, g = ((argb >> 8) & 255) / 255f, b = (argb & 255) / 255f;
		float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), d = max - min;
		if (d < 0.02f) return argb;
		float h;
		if (max == r) h = ((g - b) / d) % 6f; else if (max == g) h = (b - r) / d + 2f; else h = (r - g) / d + 4f;
		h = (h * 60f + HUES[variant] + 360f) % 360f;
		float s = max == 0 ? 0 : d / max, v = max;
		float c = v * s, x = c * (1 - Math.abs((h / 60f) % 2 - 1)), m = v - c;
		float rr, gg, bb;
		if (h < 60) { rr = c; gg = x; bb = 0; } else if (h < 120) { rr = x; gg = c; bb = 0; } else if (h < 180) { rr = 0; gg = c; bb = x; }
		else if (h < 240) { rr = 0; gg = x; bb = c; } else if (h < 300) { rr = x; gg = 0; bb = c; } else { rr = c; gg = 0; bb = x; }
		return (argb & 0xFF000000) | (Math.round((rr + m) * 255) << 16) | (Math.round((gg + m) * 255) << 8) | Math.round((bb + m) * 255);
	}
	private Colors() { }
}
