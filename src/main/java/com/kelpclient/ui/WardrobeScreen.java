package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.cosmetic.CosmeticPreview;
import com.kelpclient.cosmetic.CustomAssets;
import com.kelpclient.cosmetic.SkinImport;
import com.kelpclient.module.Cat;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import com.kelpclient.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelp Wardrobe: your cosmetics (with previews) on the left, your player on the right.
 * Drag the player to look from every angle, scroll over it to zoom. Drop a PNG on the window to set your skin or cape.
 */
public class WardrobeScreen extends KScreen {
	private static final int GAP = 6, CARD_H = 76;

	private final List<Module> mods = new ArrayList<>();
	private final List<ClickableWidget> cardW = new ArrayList<>();
	private final List<Object[]> cards = new ArrayList<>(); // {Module, x, y}
	private int listX, listY, listW, listH, cols, cw, rows, rowOff;
	private int vx, vy, vw, vh;
	private float yaw, tilt = 0.06f, zoom = 1f;
	private boolean spin = true, dragging, leftWas;
	private int lastMx, lastMy;
	private long lastFrame = System.currentTimeMillis();
	private String armed; // "skin" or "cape": what the next dropped file is for (null = guess from its size)
	private String status = "";
	private int statusColor = Theme.DIM;
	private long statusMs;
	private KButton spinBtn;

	public WardrobeScreen(Screen parent) { super("KELP WARDROBE", parent); }

	@Override protected boolean hasDone() { return false; }

	@Override
	protected void layout() {
		pw = Math.min(width - 12, 740);
		ph = height - 12;
		px = (width - pw) / 2;
		py = 6;
	}

	@Override
	protected void build() {
		mods.clear();
		for (Module m : Modules.ALL) if (m.cat == Cat.COSMETICS) mods.add(m);

		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 26, py + 4, 18, 18, "x", null, b -> close()));

		listX = px + 10;
		listY = py + 34;
		listW = (int) (pw * 0.58f) - 10;
		listH = ph - 34 - 10;
		cols = listW >= 330 ? 2 : 1;
		cw = (listW - 8 - (cols - 1) * GAP) / cols;
		rows = Math.max(1, (listH + GAP) / (CARD_H + GAP));

		vx = px + listW + 22;
		vw = px + pw - 10 - vx;
		vy = py + 34;
		vh = ph - 34 - 96;

		int by = vy + vh + 8, half = (vw - 4) / 2;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, vx, by, half, 18, "Set Skin", "person", b -> arm("skin")));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, vx + half + 4, by, half, 18, "Set Cape", "upload", b -> arm("cape")));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, vx, by + 22, half, 18, "Remove Skin", null, b -> {
			SkinImport.remove("skin"); say("Skin removed - you have your normal skin again.", Theme.ACCENT);
		}));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, vx + half + 4, by + 22, half, 18, "Remove Cape", null, b -> {
			SkinImport.remove("cape"); say("Cape removed.", Theme.ACCENT);
		}));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, vx, by + 44, half, 18, "Open Folder", "folder", b -> {
			try { Util.getOperatingSystem().open(CustomAssets.dir()); } catch (Throwable t) { say("Folder: " + CustomAssets.dir().getPath(), Theme.DIM); }
		}));
		spinBtn = new KButton(KButton.Mode.PLAIN, vx + half + 4, by + 44, half, 18, spin ? "Spin: On" : "Spin: Off", null, b -> {
			spin = !spin;
			b.setMessage(Text.literal(spin ? "Spin: On" : "Spin: Off"));
		});
		addDrawableChild(spinBtn);
		rebuildCards();
	}

	private void arm(String kind) {
		armed = kind;
		say("Drag a " + (kind.equals("skin") ? "skin (64x64 PNG)" : "cape picture (PNG)") + " from your computer onto this window.", Theme.ACCENT2);
	}

	private void say(String s, int color) { status = s; statusColor = color; statusMs = System.currentTimeMillis(); }

	@Override
	public void onFilesDropped(List<Path> paths) {
		if (paths == null || paths.isEmpty()) return;
		Path p = paths.get(0);
		String name = p.getFileName() == null ? "" : p.getFileName().toString().toLowerCase();
		if (!name.endsWith(".png")) { say("Please drop a .png file.", Theme.RED); return; }
		String kind = armed;
		if (kind == null) {
			try {
				java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(p.toFile());
				kind = (bi != null && bi.getWidth() == 64 && (bi.getHeight() == 64 || bi.getHeight() == 32)) ? "skin" : "cape";
			} catch (Throwable t) { kind = "cape"; }
		}
		String err = kind.equals("skin") ? SkinImport.importSkin(p) : SkinImport.importCape(p);
		armed = null;
		if (err != null) say(err, Theme.RED);
		else say((kind.equals("skin") ? "Skin" : "Cape") + " applied. Turn it off anytime in the list.", Theme.ACCENT);
		rebuildCards();
	}

	private Setting colorOf(Module m) {
		for (Setting s : m.settings) if (s.key.equals("color")) return s;
		return null;
	}

	private void rebuildCards() {
		for (ClickableWidget w : cardW) remove(w);
		cardW.clear();
		cards.clear();
		int total = (mods.size() + cols - 1) / cols;
		rowOff = Math.max(0, Math.min(rowOff, Math.max(0, total - rows)));
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int idx = (rowOff + r) * cols + c;
				if (idx >= mods.size()) break;
				final Module m = mods.get(idx);
				int x = listX + c * (cw + GAP), y = listY + r * (CARD_H + GAP);
				cards.add(new Object[]{m, x, y});
				int cx = x + 8 + 62 + 8, cwid = x + cw - 8 - cx;
				final Setting col = colorOf(m);
				int bw = col != null ? cwid / 2 - 2 : cwid;
				KButton eq = new KButton(KButton.Mode.PLAIN, cx, y + CARD_H - 24, bw, 16, m.enabled ? "Remove" : "Equip", null, b -> {
					m.setEnabled(!m.enabled);
					Config.save();
					b.setMessage(Text.literal(m.enabled ? "Remove" : "Equip"));
				});
				cardW.add(eq); addDrawableChild(eq);
				if (col != null) {
					KButton cb = new KButton(KButton.Mode.PLAIN, cx + bw + 4, y + CARD_H - 24, cwid - bw - 4, 16, col.display(), null, b -> {
						col.next(); Config.save(); b.setMessage(Text.literal(col.display()));
					});
					cardW.add(cb); addDrawableChild(cb);
				}
			}
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		lastMx = mx; lastMy = my;
		Theme.text(c, "Equip cosmetics and see them on you", px + 10 + Theme.tw(titleText) + 14, py + 10, Theme.DIM);
		c.fill(px + 10, py + 24, px + pw - 10, py + 25, Theme.BORDER);

		for (Object[] card : cards) {
			Module m = (Module) card[0];
			int x = (Integer) card[1], y = (Integer) card[2];
			boolean over = mx >= x && mx < x + cw && my >= y && my < y + CARD_H;
			Theme.box(c, x, y, cw, CARD_H, over ? Theme.HOVER : Theme.PANEL2, m.enabled ? Theme.ACCENT : Theme.BORDER);
			if (m.enabled) c.fillGradient(x + 1, y + 1, x + cw - 1, y + 3, Theme.ACCENT, Theme.ACCENT2);
			// preview well
			c.fillGradient(x + 8, y + 8, x + 70, y + 70, 0xFF0A1F19, 0xFF06100D);
			Theme.outline(c, x + 8, y + 8, 62, 62, m.enabled ? 0xFF2B9B73 : 0xFF14382F);
			CosmeticPreview.draw(c, m, x + 8, y + 8, 62, 62);
			int tx = x + 78, tw = x + cw - 8 - tx;
			Theme.text(c, Theme.fit(m.name, tw - (m.enabled ? 44 : 0)), tx, y + 9, Theme.TEXT);
			if (m.enabled) {
				int bx = x + cw - 8 - 40;
				Theme.box(c, bx, y + 6, 40, 12, 0xFF0F3A2B, 0xFF17603F);
				Theme.centered(c, "WORN", bx + 20, y + 9, Theme.ACCENT);
			}
			Theme.text(c, Theme.fit(m.desc, tw), tx, y + 24, Theme.DIM);
			String more = shortDesc(m);
			if (more != null) Theme.text(c, Theme.fit(more, tw), tx, y + 35, Theme.ACCENT2);
		}
		int total = (mods.size() + cols - 1) / cols;
		if (total > rows) {
			int th = Math.max(16, listH * rows / total), ty = listY + (listH - th) * rowOff / Math.max(1, total - rows);
			c.fill(listX + listW - 3, listY, listX + listW, listY + listH, 0xFF10201C);
			c.fill(listX + listW - 3, ty, listX + listW, ty + th, Theme.ACCENT);
		}

		// viewport
		c.fillGradient(vx, vy, vx + vw, vy + vh, 0xFF0C2C24, 0xFF061210);
		Theme.outline(c, vx, vy, vw, vh, armed != null ? Theme.ACCENT2 : Theme.BORDER);
		int fy = vy + vh - 14;
		for (int i = 0; i < 5; i++) c.fill(vx + vw / 2 - 40 + i * 6, fy + i, vx + vw / 2 + 40 - i * 6, fy + i + 1, Theme.withAlpha(Theme.ACCENT, 60 - i * 10));
		drawPlayer(c);
		Theme.text(c, "Drag to rotate  -  Scroll to zoom", vx + 6, vy + 6, Theme.withAlpha(0xFFFFFF, 90));

		int by = vy + vh + 8 + 66;
		if (!status.isEmpty() && System.currentTimeMillis() - statusMs < 9000) {
			Theme.text(c, Theme.fit(status, vw), vx, by + 2, statusColor);
		} else {
			Theme.text(c, Theme.fit("Tip: drop a PNG onto this window to set your skin or cape.", vw), vx, by + 2, Theme.DIM);
		}
	}

	private String shortDesc(Module m) {
		Setting s = colorOf(m);
		return s == null ? null : "Colour: " + s.display();
	}

	/** Draws the local player into the viewport, turned by the drag angle. */
	private void drawPlayer(DrawContext c) {
		ClientPlayerEntity p = client.player;
		if (p == null) { Theme.centered(c, "Join a world to preview", vx + vw / 2, vy + vh / 2, Theme.DIM); return; }
		float by = p.bodyYaw, lby = p.lastBodyYaw, hy = p.headYaw, lhy = p.lastHeadYaw, yw = p.getYaw(), lyw = p.lastYaw, pt = p.getPitch(), lpt = p.lastPitch;
		try {
			p.bodyYaw = 180f; p.lastBodyYaw = 180f; p.headYaw = 180f; p.lastHeadYaw = 180f;
			p.setYaw(180f); p.lastYaw = 180f; p.setPitch(0f); p.lastPitch = 0f;
			EntityRenderState state = client.getEntityRenderDispatcher().getAndUpdateRenderState(p, 1.0f);
			Quaternionf rot = new Quaternionf().rotateZ((float) Math.PI).rotateX(-tilt).rotateY(yaw);
			Vector3f off = new Vector3f(0f, p.getHeight() / 2f, 0f);
			float size = vh / 2.15f * zoom;
			c.addEntity(state, size, off, rot, null, vx + 1, vy + 1, vx + vw - 1, vy + vh - 1);
		} catch (Throwable t) {
			Theme.centered(c, "Preview unavailable", vx + vw / 2, vy + vh / 2, Theme.DIM);
		} finally {
			p.bodyYaw = by; p.lastBodyYaw = lby; p.headYaw = hy; p.lastHeadYaw = lhy;
			p.setYaw(yw); p.lastYaw = lyw; p.setPitch(pt); p.lastPitch = lpt;
		}
	}

	@Override
	public void render(DrawContext c, int mx, int my, float delta) {
		super.render(c, mx, my, delta);
		long now = System.currentTimeMillis();
		float dt = Math.min(0.1f, (now - lastFrame) / 1000f);
		lastFrame = now;
		boolean left = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean inView = mx >= vx && mx < vx + vw && my >= vy && my < vy + vh;
		if (left && !leftWas && inView) { dragging = true; dragX = mx; dragY = my; }
		if (!left) dragging = false;
		if (dragging) {
			yaw += (mx - dragX) * 0.018f;
			tilt = Math.max(-0.5f, Math.min(0.6f, tilt + (my - dragY) * 0.004f));
			dragX = mx; dragY = my;
		} else if (spin) {
			yaw += dt * 0.6f;
		}
		leftWas = left;
	}

	private int dragX, dragY;

	@Override
	protected boolean onScroll(int dir) {
		if (lastMx >= vx && lastMx < vx + vw && lastMy >= vy && lastMy < vy + vh) {
			zoom = Math.max(0.6f, Math.min(1.8f, zoom - dir * 0.08f));
			return true;
		}
		rowOff += dir;
		rebuildCards();
		return true;
	}
}
