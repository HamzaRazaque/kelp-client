package com.kelpclient.ui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** The Kelp main-menu background: the underwater kelp forest, slowly drifting and zooming, with rising bubbles. */
public final class MenuBackground {
	private static final Identifier TEX = Identifier.of("kelpclient", "textures/gui/menu_bg.png");
	private static final int TW = 1295, TH = 814;
	private static final long START = System.currentTimeMillis();

	public static void draw(DrawContext c, int w, int h) {
		double t = (System.currentTimeMillis() - START) / 1000.0;
		double aspect = w / (double) Math.max(1, h);
		double zoom = 1.22 + 0.06 * Math.sin(t * 0.13);
		double rh = TH / zoom, rw = rh * aspect;
		if (rw > TW / 1.0) { rw = TW / 1.08; rh = rw / aspect; }
		int regW = (int) rw, regH = (int) rh;
		double maxU = Math.max(0, TW - regW), maxV = Math.max(0, TH - regH);
		float u = (float) (maxU * (0.5 + 0.5 * Math.sin(t * 0.09)));
		float v = (float) (maxV * (0.5 + 0.5 * Math.sin(t * 0.063 + 1.3)));
		c.drawTexture(RenderPipelines.GUI_TEXTURED, TEX, 0, 0, u, v, w, h, regW, regH, TW, TH);
		c.fill(0, 0, w, h, 0x40071411);
		c.fillGradient(0, h / 2, w, h, 0x00071411, 0x99071411);

		// bubbles drifting up
		for (int i = 0; i < 46; i++) {
			double seed = i * 97.13;
			double speed = 14 + (i % 7) * 5;
			double y = h + 10 - ((t * speed + seed * 13) % (h + 20));
			double x = ((seed * 31) % w) + Math.sin(t * 0.8 + i) * 6;
			int s = 1 + (i % 3 == 0 ? 1 : 0);
			int a = 70 + (i % 5) * 20;
			c.fill((int) x, (int) y, (int) x + s, (int) y + s, (a << 24) | 0xA6E8F2);
		}
	}

	private MenuBackground() { }
}
