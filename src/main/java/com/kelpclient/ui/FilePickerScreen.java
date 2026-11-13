package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A small file browser that lives inside the game (no native dialogs, so it works everywhere):
 * shortcuts on top, folders and PNG files in the middle, a picture preview and "Use this image" on the right.
 */
public class FilePickerScreen extends KScreen {
	private static final int ROW_H = 16, PREVIEW = 140;
	private static Path lastDir;

	private final Consumer<Path> onPick;
	private Path dir;
	private final List<Path> entries = new ArrayList<>();
	private final List<ClickableWidget> rowW = new ArrayList<>();
	private int off, rows, lx, ly, lw;
	private Path selected;
	private String problem = "";
	private KButton useBtn;

	public FilePickerScreen(Screen parent, String title, Consumer<Path> onPick) {
		super(title, parent);
		this.onPick = onPick;
		Path start = lastDir;
		if (start == null || !Files.isDirectory(start)) start = guess("Downloads");
		this.dir = start;
	}

	private static Path guess(String name) {
		Path home = new File(System.getProperty("user.home", ".")).toPath();
		Path p = home.resolve(name);
		return Files.isDirectory(p) ? p : home;
	}

	@Override protected boolean hasDone() { return false; }

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 620);
		ph = Math.min(height - 12, 360);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
	}

	@Override
	protected void build() {
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 26, py + 4, 18, 18, "x", null, b -> close()));
		int bx = px + 10, by = py + 28;
		String[][] shortcuts = {{"Downloads", "Downloads"}, {"Desktop", "Desktop"}, {"Pictures", "Pictures"}, {"Documents", "Documents"}, {"Home", ""}};
		for (String[] s : shortcuts) {
			int w = Theme.tw(s[0]) + 14;
			final String sub = s[1];
			addDrawableChild(new KButton(KButton.Mode.PLAIN, bx, by, w, 16, s[0], null, b -> go(sub.isEmpty() ? new File(System.getProperty("user.home", ".")).toPath() : guess(sub))));
			bx += w + 4;
		}
		lx = px + 10;
		ly = py + 74;
		lw = pw - 20 - PREVIEW - 10;
		rows = Math.max(3, (ph - 74 - 12) / ROW_H);
		useBtn = new KButton(KButton.Mode.PLAIN, px + pw - 10 - PREVIEW, py + ph - 28, PREVIEW, 18, "Use this image", "upload", b -> {
			if (selected != null) { lastDir = dir; client.setScreen(parent); onPick.accept(selected); }
		});
		addDrawableChild(useBtn);
		useBtn.active = selected != null;
		load();
	}

	private void go(Path p) {
		if (p == null) return;
		dir = p; off = 0; selected = null;
		if (useBtn != null) useBtn.active = false;
		load();
	}

	private void load() {
		entries.clear();
		problem = "";
		try (Stream<Path> s = Files.list(dir)) {
			List<Path> all = new ArrayList<>();
			s.forEach(all::add);
			all.sort(Comparator.comparing((Path p) -> !Files.isDirectory(p)).thenComparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)));
			for (Path p : all) {
				String n = p.getFileName().toString();
				if (n.startsWith(".")) continue;
				if (Files.isDirectory(p) || n.toLowerCase(Locale.ROOT).endsWith(".png")) entries.add(p);
			}
		} catch (Throwable t) {
			problem = "Can't open this folder.";
		}
		rebuildRows();
	}

	private int total() { return entries.size() + (dir.getParent() != null ? 1 : 0); }

	private Path entryAt(int i) {
		if (dir.getParent() != null) { if (i == 0) return null; i--; }
		return entries.get(i);
	}

	private void rebuildRows() {
		for (ClickableWidget w : rowW) remove(w);
		rowW.clear();
		off = Math.max(0, Math.min(off, Math.max(0, total() - rows)));
		for (int r = 0; r < rows; r++) {
			int i = off + r;
			if (i >= total()) break;
			final int idx = i;
			KButton b = new KButton(KButton.Mode.HIT, lx, ly + r * ROW_H, lw, ROW_H - 1, "", null, x -> clicked(idx));
			rowW.add(b); addDrawableChild(b);
		}
	}

	private void clicked(int i) {
		Path p = entryAt(i);
		if (p == null) { go(dir.getParent()); return; }
		if (Files.isDirectory(p)) { go(p); return; }
		selected = p;
		useBtn.active = true;
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		ShotCache.beginFrame();
		Theme.text(c, Theme.fit(dir.toString(), pw - 70), px + 10, py + 52, Theme.DIM);
		c.fill(lx, ly - 2, lx + lw, ly + rows * ROW_H, 0xFF050B09);
		Theme.outline(c, lx - 1, ly - 3, lw + 2, rows * ROW_H + 3, Theme.BORDER);
		for (int r = 0; r < rows; r++) {
			int i = off + r;
			if (i >= total()) break;
			Path p = entryAt(i);
			int y = ly + r * ROW_H;
			boolean over = mx >= lx && mx < lx + lw && my >= y && my < y + ROW_H - 1;
			boolean sel = p != null && p.equals(selected);
			if (sel) c.fill(lx, y, lx + lw, y + ROW_H - 1, 0xFF14382F);
			else if (over) c.fill(lx, y, lx + lw, y + ROW_H - 1, 0xFF0D211C);
			String name = p == null ? ".." : p.getFileName().toString();
			boolean isDir = p == null || Files.isDirectory(p);
			Icons.draw(c, isDir ? "folder" : "gallery", lx + 3, y - 1, 1);
			Theme.text(c, Theme.fit(name, lw - 30), lx + 24, y + 4, isDir ? Theme.ACCENT2 : Theme.TEXT);
		}
		if (!problem.isEmpty()) Theme.text(c, problem, lx + 6, ly + 6, Theme.RED);
		else if (entries.isEmpty()) Theme.text(c, "No PNG files or folders here.", lx + 24, ly + 22, Theme.DIM);

		int pxx = px + pw - 10 - PREVIEW, pyy = ly - 2;
		Theme.box(c, pxx, pyy, PREVIEW, PREVIEW, 0xFF050B09, Theme.BORDER);
		if (selected != null) {
			ShotCache.Tex t = ShotCache.get(selected.toFile(), true);
			if (t != null && !t.failed) ShotCache.draw(c, t, pxx + 2, pyy + 2, PREVIEW - 4, PREVIEW - 4);
			else Theme.centered(c, t == null ? "Loading..." : "Can't read this", pxx + PREVIEW / 2, pyy + PREVIEW / 2 - 4, Theme.DIM);
			Theme.text(c, Theme.fit(selected.getFileName().toString(), PREVIEW), pxx, pyy + PREVIEW + 6, Theme.TEXT);
		} else {
			Theme.centered(c, "Pick a PNG", pxx + PREVIEW / 2, pyy + PREVIEW / 2 - 4, Theme.DIM);
		}
	}

	@Override protected boolean onScroll(int dir2) { off += dir2; rebuildRows(); return true; }
}
