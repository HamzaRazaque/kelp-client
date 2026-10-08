package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WaypointEditScreen extends Screen {
    private final Screen parent;
    private final KelpConfig.Waypoint wp;
    private final boolean isNew;
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private String tName, tX, tY, tZ, msg = "";
    private int color;
    private boolean enabled;
    private int px, py, pw, ph;

    public WaypointEditScreen(Screen parent, KelpConfig.Waypoint wp, boolean isNew) {
        super(Text.literal(isNew ? "New Waypoint" : "Edit Waypoint"));
        this.parent = parent; this.wp = wp; this.isNew = isNew;
        tName = wp.name; tX = String.valueOf(wp.x); tY = String.valueOf(wp.y); tZ = String.valueOf(wp.z);
        color = wp.color; enabled = wp.enabled;
    }

    private TextFieldWidget field(int x, int y, int w, String text, Consumer<String> onChange, String hint) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal(hint));
        f.setMaxLength(32);
        f.setText(text);
        f.setChangedListener(onChange);
        return addDrawableChild(f);
    }

    private void confirm() {
        int xi, yi, zi;
        try { xi = Integer.parseInt(tX.trim()); yi = Integer.parseInt(tY.trim()); zi = Integer.parseInt(tZ.trim()); }
        catch (NumberFormatException e) { msg = "Coordinates must be whole numbers"; return; }
        wp.name = tName.trim().isEmpty() ? "Waypoint" : tName.trim();
        wp.x = xi; wp.y = yi; wp.z = zi; wp.color = color; wp.enabled = enabled;
        if (isNew) KelpConfig.get().waypointList.add(wp);
        KelpConfig.save();
        close();
    }

    @Override
    protected void init() {
        overlays.clear();
        pw = Math.min(360, width - 16); ph = Math.min(250, height - 16);
        px = (width - pw) / 2; py = (height - ph) / 2;
        int x = px + 12, w = pw - 24;

        field(x, py + 40, w, tName, s -> tName = s, "Name");
        int fw = (w - 16) / 3;
        field(x, py + 82, fw, tX, s -> tX = s, "X");
        field(x + fw + 8, py + 82, fw, tY, s -> tY = s, "Y");
        field(x + 2 * (fw + 8), py + 82, fw, tZ, s -> tZ = s, "Z");

        for (int i = 0; i < KelpConfig.WP_COLORS.length; i++) {
            final int idx = i;
            ButtonWidget sw = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> color = idx)
                    .dimensions(x + 2 + i * ((w - 4) / 12), py + 122, 18, 18).build());
            overlays.add(g -> {
                boolean sel = color == idx;
                g.fill(sw.getX() - 2, sw.getY() - 2, sw.getX() + 20, sw.getY() + 20, sel ? KelpUi.ACCENT : (sw.isHovered() ? KelpUi.DIM : KelpUi.BORDER));
                g.fill(sw.getX(), sw.getY(), sw.getX() + 18, sw.getY() + 18, KelpConfig.WP_COLORS[idx]);
            });
        }

        ButtonWidget en = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> enabled = !enabled).dimensions(x, py + 150, w, 24).build());
        overlays.add(g -> KelpUi.toggleRow(g, textRenderer, en, "Enabled", null, enabled));

        int by = py + ph - 30, n = isNew ? 2 : 3, bw = (w - (n - 1) * 8) / n;
        ButtonWidget ok = addDrawableChild(ButtonWidget.builder(Text.literal("Confirm"), b -> confirm()).dimensions(x, by, bw, 22).build());
        ButtonWidget cancel = addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> close()).dimensions(x + bw + 8, by, bw, 22).build());
        overlays.add(g -> { KelpUi.card(g, textRenderer, ok, null, true); KelpUi.card(g, textRenderer, cancel, null, false); });
        if (!isNew) {
            ButtonWidget del = addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> {
                KelpConfig.get().waypointList.remove(wp); KelpConfig.save(); close();
            }).dimensions(x + 2 * (bw + 8), by, bw, 22).build());
            overlays.add(g -> {
                boolean hov = del.isHovered();
                KelpUi.rect(g, del.getX(), del.getY(), del.getWidth(), del.getHeight(), hov ? 0xFF5A1F1F : 0xFF3A1717, hov ? KelpUi.RED : 0xFF5C2A2A);
                g.drawCenteredTextWithShadow(textRenderer, "Delete", del.getX() + del.getWidth() / 2, del.getY() + 7, KelpUi.TEXT);
            });
        }
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) {
        KelpUi.background(ctx, width, height, client.world != null);
        KelpUi.panel(ctx, px, py, pw, ph, KelpUi.PANEL, KelpUi.BORDER);
        ctx.drawTextWithShadow(textRenderer, isNew ? "NEW WAYPOINT" : "EDIT WAYPOINT", px + 12, py + 10, KelpUi.ACCENT);
        ctx.drawTextWithShadow(textRenderer, "Name", px + 12, py + 29, KelpUi.DIM);
        int fw = (pw - 24 - 16) / 3;
        ctx.drawTextWithShadow(textRenderer, "X", px + 12, py + 71, KelpUi.DIM);
        ctx.drawTextWithShadow(textRenderer, "Y", px + 12 + fw + 8, py + 71, KelpUi.DIM);
        ctx.drawTextWithShadow(textRenderer, "Z", px + 12 + 2 * (fw + 8), py + 71, KelpUi.DIM);
        ctx.drawTextWithShadow(textRenderer, "Color", px + 12, py + 111, KelpUi.DIM);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        for (var o : overlays) o.accept(ctx);
        if (!msg.isEmpty()) ctx.drawTextWithShadow(textRenderer, msg, px + 12, py + ph - 46, KelpUi.RED);
    }
}
