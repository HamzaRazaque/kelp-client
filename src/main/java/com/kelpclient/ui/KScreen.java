package com.kelpclient.ui;

import com.kelpclient.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Base for Kelp panels: dim backdrop, bordered panel, title, Done button, scroll hook. */
public abstract class KScreen extends Screen {
	protected final Screen parent;
	protected final String titleText;
	protected int px, py, pw, ph;

	protected KScreen(String title, Screen parent) {
		super(Text.literal(title));
		this.titleText = title;
		this.parent = parent;
	}

	protected void layout() {
		pw = Math.min(width - 16, 420);
		ph = Math.min(height - 16, 250);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
	}

	protected boolean hasDone() { return true; }
	protected abstract void build();
	protected void drawContent(DrawContext c, int mx, int my) { }
	protected boolean onScroll(int dir) { return false; }

	@Override
	protected void init() {
		layout();
		build();
		if (hasDone()) addDrawableChild(new KButton(KButton.Mode.PRIMARY, px + pw - 70, py + ph - 26, 62, 18, "Done", null, b -> close()));
	}

	@Override
	public void renderBackground(DrawContext c, int mx, int my, float delta) {
		c.fill(0, 0, width, height, Theme.DIM_BG);
		Theme.box(c, px, py, pw, ph, Theme.BG, Theme.BORDER);
		Theme.text(c, titleText, px + 10, py + 10, Theme.ACCENT);
		drawContent(c, mx, my);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double h, double v) {
		if (v != 0 && onScroll(v > 0 ? -1 : 1)) return true;
		return super.mouseScrolled(mx, my, h, v);
	}

	@Override public void close() { if (client != null) client.setScreen(parent); }
	@Override public void removed() { Config.save(); }
	@Override public boolean shouldPause() { return false; }
}
