package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.function.BooleanSupplier;

/** One widget class for every Kelp button style, with hover / toggle animations. */
public class KButton extends ButtonWidget {
	public enum Mode { PLAIN, PRIMARY, TAB, ICON, GHOST, SWITCH, STATE, HIT }

	public final Mode mode;
	public String icon, tip;
	public int iconScale = 1;
	public BooleanSupplier on = () -> false;
	private float hov, onA;

	public KButton(Mode mode, int x, int y, int w, int h, String label, String icon, PressAction action) {
		super(x, y, w, h, net.minecraft.text.Text.literal(label), action == null ? b -> { } : action, DEFAULT_NARRATION_SUPPLIER);
		this.mode = mode;
		this.icon = icon;
		this.setAlpha(0f); // hide the vanilla button art; drawIcon below paints everything
	}

	public KButton tip(String t) { this.tip = t; return this; }
	public KButton on(BooleanSupplier s) { this.on = s; onA = s.getAsBoolean() ? 1f : 0f; return this; }
	public KButton scale(int s) { this.iconScale = s; return this; }

	@Override
	public void drawIcon(DrawContext c, int mx, int my, float delta) {
		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		boolean isHov = isHovered(), isOn = on.getAsBoolean();
		hov += ((isHov ? 1f : 0f) - hov) * 0.3f;
		onA += ((isOn ? 1f : 0f) - onA) * 0.3f;
		String label = getMessage().getString();
		switch (mode) {
			case HIT:
				if (hov > 0.03f) Theme.outline(c, x, y, w, h, Theme.withAlpha(Theme.ACCENT, (int) (hov * 255)));
				break;
			case GHOST:
				if (icon != null) Icons.drawCentered(c, icon, x + w / 2, y + h / 2, iconScale, Theme.lerp(0xFF8FB3A4, Theme.ACCENT, hov));
				break;
			case ICON: {
				Theme.box(c, x, y, w, h, Theme.lerp(Theme.PANEL2, Theme.HOVER, hov), Theme.lerp(Theme.BORDER, Theme.ACCENT, Math.max(hov, onA)));
				if (icon != null) {
					int s = Math.max(1, (int) (Math.min(w, h) * 0.62 / Math.max(Icons.w(icon), Icons.h(icon))));
					Icons.drawCentered(c, icon, x + w / 2, y + h / 2 - Math.round(hov * 2), s);
				}
				break;
			}
			case SWITCH: {
				int tw = Math.min(26, w), th = Math.min(12, h), tx = x + w - tw, ty = y + (h - th) / 2;
				c.fill(tx, ty, tx + tw, ty + th, Theme.lerp(0xFF3A4A45, 0xFF2FD36B, onA));
				int kw = th - 4;
				int kx = tx + 2 + Math.round((tw - kw - 4) * onA);
				c.fill(kx, ty + 2, kx + kw, ty + 2 + kw, 0xFFFFFFFF);
				break;
			}
			case STATE: {
				int base = Theme.lerp(0xFF16231F, Theme.HOVER, hov);
				c.fill(x, y, x + w, y + h, Theme.lerp(base, 0xFF1B6B3A, onA));
				Theme.centered(c, isOn ? "Enabled" : "Disabled", x + w / 2, y + (h - 8) / 2, Theme.lerp(Theme.DIM, 0xFFFFFFFF, onA));
				break;
			}
			default: {
				int fill, border;
				if (mode == Mode.PRIMARY) { fill = Theme.lerp(0xFF16724F, 0xFF1F8A5E, hov); border = Theme.BORDER; }
				else if (mode == Mode.TAB) { fill = Theme.lerp(Theme.lerp(Theme.PANEL2, Theme.HOVER, hov), Theme.SEL, onA); border = Theme.lerp(Theme.BORDER, Theme.ACCENT, onA); }
				else { fill = Theme.lerp(Theme.PANEL2, Theme.HOVER, hov); border = Theme.lerp(Theme.BORDER, Theme.ACCENT, hov * 0.6f); }
				Theme.box(c, x, y, w, h, fill, border);
				int tx = x + w / 2;
				if (icon != null) {
					int iw = Icons.w(icon) * iconScale;
					Icons.drawCentered(c, icon, x + 6 + iw / 2, y + h / 2, iconScale);
					tx = x + 6 + iw + (w - 6 - iw) / 2;
				}
				Theme.centered(c, label, tx, y + (h - 8) / 2, Theme.TEXT);
			}
		}
	}
}
