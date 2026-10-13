package com.kelpclient.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

/** One widget class for every Kelp button style. */
public class KButton extends ButtonWidget {
	public enum Mode { PLAIN, PRIMARY, TAB, ICON, GHOST, SWITCH, STATE, HIT }

	public final Mode mode;
	public String icon, tip;
	public int iconScale = 1;
	public BooleanSupplier on = () -> false;

	public KButton(Mode mode, int x, int y, int w, int h, String label, String icon, PressAction action) {
		super(x, y, w, h, Text.literal(label), action == null ? b -> { } : action, DEFAULT_NARRATION_SUPPLIER);
		this.mode = mode;
		this.icon = icon;
	}

	public KButton tip(String t) { this.tip = t; return this; }
	public KButton on(BooleanSupplier s) { this.on = s; return this; }
	public KButton scale(int s) { this.iconScale = s; return this; }

	@Override
	public void renderWidget(DrawContext c, int mx, int my, float delta) {
		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		boolean hov = isHovered(), isOn = on.getAsBoolean();
		String label = getMessage().getString();
		switch (mode) {
			case HIT:
				if (hov) Theme.outline(c, x, y, w, h, Theme.ACCENT);
				break;
			case GHOST:
				if (icon != null) Icons.drawCentered(c, icon, x + w / 2, y + h / 2, iconScale, hov ? Theme.ACCENT : 0xFF8FB3A4);
				break;
			case ICON: {
				Theme.box(c, x, y, w, h, hov ? Theme.HOVER : Theme.PANEL2, hov || isOn ? Theme.ACCENT : Theme.BORDER);
				if (icon != null) {
					int s = Math.max(1, (int) (Math.min(w, h) * 0.62 / Math.max(Icons.w(icon), Icons.h(icon))));
					Icons.drawCentered(c, icon, x + w / 2, y + h / 2, s);
				}
				break;
			}
			case SWITCH: {
				int tw = Math.min(26, w), th = Math.min(12, h), tx = x + w - tw, ty = y + (h - th) / 2;
				c.fill(tx, ty, tx + tw, ty + th, isOn ? 0xFF2FD36B : 0xFF3A4A45);
				int kw = th - 4;
				int kx = isOn ? tx + tw - kw - 2 : tx + 2;
				c.fill(kx, ty + 2, kx + kw, ty + 2 + kw, 0xFFFFFFFF);
				break;
			}
			case STATE: {
				c.fill(x, y, x + w, y + h, isOn ? 0xFF1B6B3A : (hov ? Theme.HOVER : 0xFF16231F));
				Theme.centered(c, isOn ? "Enabled" : "Disabled", x + w / 2, y + (h - 8) / 2, isOn ? 0xFFFFFFFF : Theme.DIM);
				break;
			}
			default: {
				boolean sel = mode == Mode.TAB && isOn;
				int fill = mode == Mode.PRIMARY ? (hov ? 0xFF1F8A5E : 0xFF16724F) : sel ? Theme.SEL : hov ? Theme.HOVER : Theme.PANEL2;
				int border = sel || (mode == Mode.PRIMARY) || hov ? (sel ? Theme.ACCENT : Theme.BORDER) : Theme.BORDER;
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
