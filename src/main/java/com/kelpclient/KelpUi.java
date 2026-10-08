package com.kelpclient;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/** Shared look of every Kelp screen. Vanilla buttons stay as the click targets; we paint over them. */
public final class KelpUi {
    private KelpUi() {}
    public static final int ACCENT = 0xFF3DDC6B, TEXT = 0xFFE8FFF0, DIM = 0xFF8FB8A3, BORDER = 0xFF1F5C46,
            CARD = 0xFF12302A, CARD_HOVER = 0xFF1B4A3C, PANEL = 0xFF0A1A16, RED = 0xFFFF6B6B;
    public static final Identifier LOGO = Identifier.of("kelpclient", "textures/gui/logo.png");

    public static void background(DrawContext c, int w, int h, boolean inWorld) {
        if (inWorld) { c.fill(0, 0, w, h, 0xC0050E0B); return; }
        double t = Util.getMeasuringTimeMs() / 1000.0;
        c.fillGradient(0, 0, w, h, 0xFF03161C, 0xFF0A5A3A);
        for (int i = 0; i < 5; i++) {
            int rx = (int) (w * (i + 0.5) / 5 + Math.sin(t * 0.3 + i * 2) * w * 0.04);
            c.fillGradient(rx - 18, 0, rx + 18, (int) (h * 0.8), 0x1AFFFFFF, 0x00FFFFFF);
        }
        kelp(c, w, h, t, 10, 0xFF0F4A2E, 20, 4, 1.3, 0.55, 5);
        kelp(c, w, h, t, 14, 0xFF1E8B3A, 14, 5, 0.0, 0.62, 8);
        for (int i = 0; i < 18; i++) {
            double sp = 0.03 + 0.02 * (i % 5);
            int py = (int) (h - ((t * sp * h + i * h / 18.0) % h));
            int px = (int) (w * ((i * 53 % 100) / 100.0) + Math.sin(t + i) * 6);
            int sz = 1 + (i % 3);
            c.fill(px, py, px + sz, py + sz, 0x66BFFFEF);
        }
    }

    private static void kelp(DrawContext c, int w, int h, double t, int n, int color, int seg, int thick, double phase, double frac, double amp) {
        for (int i = 0; i < n; i++) {
            int baseX = (int) (w * (i + 0.5) / n);
            int top = (int) (h * (1 - frac)) + (i * 37 % 60);
            for (int y = h; y > top; y -= seg) {
                int sway = (int) (Math.sin(t * 1.1 + y * 0.025 + i * 1.7 + phase) * (amp + (h - y) * 0.025));
                c.fill(baseX + sway, y - seg, baseX + sway + thick, y, color);
            }
        }
    }

    /** Opaque rounded-corner panel. */
    public static void panel(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        c.fill(x + 1, y, x + w - 1, y + h, fill);
        c.fill(x, y + 1, x + w, y + h - 1, fill);
        c.fill(x + 1, y, x + w - 1, y + 1, border);
        c.fill(x + 1, y + h - 1, x + w - 1, y + h, border);
        c.fill(x, y + 1, x + 1, y + h - 1, border);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, border);
    }

    /** Flat bordered rectangle (fully opaque, so it hides whatever is below). */
    public static void rect(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        c.fill(x, y, x + w, y + h, border);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    /** Paint a styled button over a vanilla ButtonWidget (needs height >= 22 for an icon). */
    public static void card(DrawContext c, TextRenderer tr, ButtonWidget b, String icon, boolean accent) {
        boolean hov = b.isHovered();
        int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
        int fill = accent ? (hov ? 0xFF1F8A52 : 0xFF176B40) : (hov ? CARD_HOVER : CARD);
        rect(c, x, y, w, h, fill, hov ? ACCENT : BORDER);
        int tx = x + w / 2;
        if (icon != null) {
            int bs = h - 6;
            c.fill(x + 3, y + 3, x + 3 + bs, y + 3 + bs, 0x66000000);
            KelpIcons.at(c, icon, x + 3 + (bs - 16) / 2, y + 3 + (bs - 16) / 2);
            tx = x + bs + 3 + (w - bs - 3) / 2;
        }
        c.drawCenteredTextWithShadow(tr, b.getMessage().getString(), tx, y + (h - 8) / 2, TEXT);
    }

    /** A settings row with a label, a description and an on/off switch. */
    public static void toggleRow(DrawContext c, TextRenderer tr, ButtonWidget b, String label, String desc, boolean on) {
        boolean hov = b.isHovered();
        int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
        rect(c, x, y, w, h, hov ? CARD_HOVER : CARD, hov ? ACCENT : BORDER);
        boolean showDesc = desc != null && h >= 26;
        c.drawTextWithShadow(tr, label, x + 8, showDesc ? y + 5 : y + (h - 8) / 2, TEXT);
        if (showDesc) c.drawTextWithShadow(tr, desc, x + 8, y + h - 13, DIM);
        int tx = x + w - 36, ty = y + (h - 12) / 2;
        c.fill(tx, ty, tx + 28, ty + 12, on ? ACCENT : 0xFF33433D);
        int kx = on ? tx + 16 : tx + 2;
        c.fill(kx, ty + 2, kx + 10, ty + 10, 0xFFFFFFFF);
    }
}
