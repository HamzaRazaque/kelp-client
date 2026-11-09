package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.List;

/** Full view of one screenshot (sharp, real texture) with previous / next (buttons, arrow keys, scroll wheel) and delete. */
public class ScreenshotViewScreen extends KScreen {
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
		pw = Math.min(width - 12, 760);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		int y = py + ph - 26;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, 52, 18, "< Prev", null, b -> step(-1)));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 66, y, 52, 18, "Next >", null, b -> step(1)));
		deleteBtn = new KButton(KButton.Mode.PLAIN, px + 122, y, 70, 18, "Delete", "x", b -> {
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
		ShotCache.beginFrame();
		File f = files.get(index);
		int ax = px + 10, ay = py + 28, areaW = pw - 20, areaH = ph - 28 - 56;
		Theme.box(c, ax, ay, areaW, areaH, 0xFF050B09, Theme.BORDER);

		ShotCache.Tex t = ShotCache.get(f, false);
		if (t == null) {
			Theme.centered(c, "Loading...", ax + areaW / 2, ay + areaH / 2 - 4, Theme.DIM);
		} else if (t.failed) {
			Theme.centered(c, "This image could not be read.", ax + areaW / 2, ay + areaH / 2 - 4, Theme.RED);
		} else {
			double s = Math.min((areaW - 4) / (double) t.w, (areaH - 4) / (double) t.h);
			int dw = Math.max(1, (int) Math.round(t.w * s)), dh = Math.max(1, (int) Math.round(t.h * s));
			ShotCache.draw(c, t, ax + (areaW - dw) / 2, ay + (areaH - dh) / 2, dw, dh);
		}
		// warm the neighbours so browsing feels instant
		if (files.size() > 1) {
			ShotCache.get(files.get(Math.floorMod(index + 1, files.size())), false);
			ShotCache.get(files.get(Math.floorMod(index - 1, files.size())), false);
		}

		String info = f.getName() + "   " + (index + 1) + " / " + files.size();
		if (t != null && !t.failed) info += "   " + t.srcW + "x" + t.srcH;
		info += "   " + (f.length() / 1024) + " KB";
		Theme.text(c, Theme.fit(info, pw - 20), px + 10, ay + areaH + 8, Theme.DIM);
		String hint = "Arrow keys or scroll to browse";
		Theme.text(c, hint, px + pw - 10 - Theme.tw(hint), py + 10, Theme.DIM);
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
