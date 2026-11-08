package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.List;

/** Full view of one screenshot with previous / next (buttons, arrow keys, scroll wheel) and delete. */
public class ScreenshotViewScreen extends KScreen {
	private static final int CELLS_W = 160, CELLS_H = 90;
	private final List<File> files;
	private int index;
	private boolean confirm, leftWas, rightWas;
	private KButton deleteBtn;

	public ScreenshotViewScreen(Screen parent, List<File> files, int index) {
		super("SCREENSHOT", parent);
		this.files = files;
		this.index = index;
	}

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 640);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		int y = py + ph - 26;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, 52, 18, "< Prev", null, b -> step(-1)));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 66, y, 52, 18, "Next >", null, b -> step(1)));
		deleteBtn = new KButton(KButton.Mode.PLAIN, px + 122, y, 62, 18, "Delete", "x", b -> {
			if (!confirm) { confirm = true; b.setMessage(Text.literal("Sure?")); return; }
			delete();
		});
		addDrawableChild(deleteBtn);
	}

	private void step(int d) {
		if (files.isEmpty()) return;
		index = Math.floorMod(index + d, files.size());
		confirm = false;
		if (deleteBtn != null) deleteBtn.setMessage(Text.literal("Delete"));
	}

	private void delete() {
		if (index < 0 || index >= files.size()) return;
		File f = files.get(index);
		try { java.nio.file.Files.deleteIfExists(f.toPath()); } catch (Exception ignored) { }
		files.remove(index);
		confirm = false;
		if (deleteBtn != null) deleteBtn.setMessage(Text.literal("Delete"));
		if (files.isEmpty()) { close(); return; }
		if (index >= files.size()) index = files.size() - 1;
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		if (files.isEmpty()) return;
		File f = files.get(index);
		int s = Math.max(1, Math.min((pw - 20) / CELLS_W, (ph - 30 - 40) / CELLS_H));
		int areaW = pw - 20, areaH = CELLS_H * s;
		int ax = px + 10, ay = py + 28;
		Theme.box(c, ax, ay, areaW, areaH + 4, 0xFF050B09, Theme.BORDER);
		ShotCache.Img img = ShotCache.get(f, CELLS_W, CELLS_H);
		if (img == null) Theme.centered(c, "Loading...", ax + areaW / 2, ay + areaH / 2, Theme.DIM);
		else if (img.w == 0) Theme.centered(c, "This image could not be read.", ax + areaW / 2, ay + areaH / 2, Theme.RED);
		else ShotCache.draw(c, img, ax + (areaW - img.w * s) / 2, ay + 2 + (CELLS_H - img.h) * s / 2, s);

		String info = f.getName() + "   " + (index + 1) + " / " + files.size();
		if (img != null && img.w > 0) info += "   " + img.srcW + "x" + img.srcH;
		info += "   " + (f.length() / 1024) + " KB";
		Theme.text(c, Theme.fit(info, pw - 20), px + 10, ay + areaH + 10, Theme.DIM);
		Theme.text(c, "Arrow keys or scroll to browse", px + 10, py + 10 + 0, Theme.DIM);
	}

	@Override
	public void render(DrawContext c, int mx, int my, float delta) {
		super.render(c, mx, my, delta);
		long win = client.getWindow().getHandle();
		boolean l = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS;
		boolean r = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
		if (l && !leftWas) step(-1);
		if (r && !rightWas) step(1);
		leftWas = l; rightWas = r;
	}

	@Override protected boolean onScroll(int dir) { step(dir); return true; }
}
