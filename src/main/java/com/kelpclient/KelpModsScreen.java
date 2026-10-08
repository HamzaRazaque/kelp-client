package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KelpModsScreen extends Screen {
    private static final String[] TABS = {"HUD", "Gameplay", "Crosshair", "Social"};
    private static final String[] TAB_ICONS = {"hud", "sword", "crosshair", "tag"};
    private static int tab = 0;

    private final Screen parent;
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private int px, py, pw, ph, cx, cy, cw, rowH;

    private record T(String name, String desc, BooleanSupplier get, Consumer<Boolean> set) {}

    public KelpModsScreen(Screen parent) { super(Text.literal("Kelp Mods")); this.parent = parent; }

    private int rowY(int row) { return cy + row * (rowH + 4); }

    private ButtonWidget add(ButtonWidget b) { return addDrawableChild(b); }

    private void toggle(int row, T t, int w) {
        ButtonWidget b = add(ButtonWidget.builder(Text.empty(), x -> { t.set().accept(!t.get().getAsBoolean()); KelpConfig.save(); })
                .dimensions(cx, rowY(row), w, rowH).build());
        overlays.add(c -> KelpUi.toggleRow(c, textRenderer, b, t.name(), t.desc(), t.get().getAsBoolean()));
    }

    private void stepper(int row, String label, Supplier<String> value, Runnable dec, Runnable inc, int w) {
        int y = rowY(row), by = y + (rowH - 18) / 2;
        ButtonWidget minus = add(ButtonWidget.builder(Text.literal("<"), x -> { dec.run(); KelpConfig.save(); }).dimensions(cx + w - 94, by, 18, 18).build());
        ButtonWidget plus = add(ButtonWidget.builder(Text.literal(">"), x -> { inc.run(); KelpConfig.save(); }).dimensions(cx + w - 24, by, 18, 18).build());
        overlays.add(c -> {
            KelpUi.rect(c, cx, y, w, rowH, KelpUi.CARD, KelpUi.BORDER);
            c.drawTextWithShadow(textRenderer, label, cx + 8, y + (rowH - 8) / 2, KelpUi.TEXT);
            KelpUi.card(c, textRenderer, minus, null, false);
            KelpUi.card(c, textRenderer, plus, null, false);
            c.drawCenteredTextWithShadow(textRenderer, value.get(), cx + w - 59, y + (rowH - 8) / 2, KelpUi.ACCENT);
        });
    }

    @Override
    protected void init() {
        overlays.clear();
        var c = KelpConfig.get();
        pw = Math.min(450, width - 16); ph = Math.min(300, height - 16);
        px = (width - pw) / 2; py = (height - ph) / 2;
        int sw = 104;
        cx = px + sw + 16; cy = py + 30; cw = pw - sw - 28;
        int contentH = ph - 30 - 34;
        rowH = Math.max(20, Math.min(30, contentH / 7 - 4));

        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            ButtonWidget b = add(ButtonWidget.builder(Text.literal(TABS[i]), x -> { tab = idx; clearAndInit(); })
                    .dimensions(px + 8, cy + i * 28, sw, 24).build());
            overlays.add(g -> KelpUi.card(g, textRenderer, b, TAB_ICONS[idx], tab == idx));
        }
        ButtonWidget done = add(ButtonWidget.builder(Text.literal("Done"), x -> close()).dimensions(px + pw - 92, py + ph - 28, 84, 22).build());
        overlays.add(g -> KelpUi.card(g, textRenderer, done, null, true));

        switch (tab) {
            case 0 -> {
                toggle(0, new T("FPS", "Show your frame rate", () -> c.fps, v -> c.fps = v), cw);
                toggle(1, new T("Coordinates", "XYZ position", () -> c.coords, v -> c.coords = v), cw);
                toggle(2, new T("Direction", "Facing and yaw", () -> c.direction, v -> c.direction = v), cw);
                toggle(3, new T("Day Counter", "Current world day", () -> c.day, v -> c.day = v), cw);
                toggle(4, new T("Ping", "Latency on servers", () -> c.ping, v -> c.ping = v), cw);
                toggle(5, new T("Server Info", "Address and version", () -> c.serverInfo, v -> c.serverInfo = v), cw);
                toggle(6, new T("Potion Effects", "Active effects and time left", () -> c.potions, v -> c.potions = v), cw);
            }
            case 1 -> {
                toggle(0, new T("Combo Counter", "Counts your hits in a row", () -> c.combo, v -> c.combo = v), cw);
                toggle(1, new T("Waypoints", "Show waypoints on the HUD", () -> c.waypoints, v -> c.waypoints = v), cw);
                ButtonWidget addWp = add(ButtonWidget.builder(Text.literal("Add waypoint here"), x -> {
                    if (client.player == null) return;
                    c.waypointList.add(new KelpConfig.Waypoint("WP" + (c.waypointList.size() + 1),
                            MathHelper.floor(client.player.getX()), MathHelper.floor(client.player.getY()), MathHelper.floor(client.player.getZ())));
                    KelpConfig.save(); clearAndInit();
                }).dimensions(cx, rowY(2), cw, Math.max(22, rowH)).build());
                overlays.add(g -> KelpUi.card(g, textRenderer, addWp, "plus", true));
                int shown = Math.min(4, c.waypointList.size());
                for (int i = 0; i < shown; i++) {
                    final int idx = i; int y = rowY(3 + i);
                    ButtonWidget del = add(ButtonWidget.builder(Text.literal("x"), x -> {
                        if (idx < c.waypointList.size()) { c.waypointList.remove(idx); KelpConfig.save(); clearAndInit(); }
                    }).dimensions(cx + cw - 24, y + (rowH - 20) / 2, 20, 20).build());
                    overlays.add(g -> {
                        if (idx >= c.waypointList.size()) return;
                        var w = c.waypointList.get(idx);
                        KelpUi.rect(g, cx, y, cw, rowH, KelpUi.CARD, KelpUi.BORDER);
                        g.drawTextWithShadow(textRenderer, w.name, cx + 8, y + (rowH - 8) / 2, KelpUi.TEXT);
                        g.drawTextWithShadow(textRenderer, w.x + ", " + w.y + ", " + w.z, cx + 60, y + (rowH - 8) / 2, KelpUi.DIM);
                        boolean hov = del.isHovered();
                        KelpUi.rect(g, del.getX(), del.getY(), 20, 20, hov ? 0xFF5A1F1F : 0xFF3A1717, hov ? KelpUi.RED : 0xFF5C2A2A);
                        KelpIcons.at(g, "trash", del.getX() + 2, del.getY() + 2);
                    });
                }
                if (c.waypointList.size() > 4) overlays.add(g -> g.drawTextWithShadow(textRenderer,
                        "Showing 4 of " + c.waypointList.size(), cx, rowY(7) + 2, KelpUi.DIM));
            }
            case 2 -> {
                int w = cw - 112;
                toggle(0, new T("Custom Crosshair", null, () -> c.customCrosshair, v -> c.customCrosshair = v), w);
                int n = KelpConfig.STYLES.length;
                stepper(1, "Style", () -> KelpConfig.STYLES[c.crosshairStyle],
                        () -> c.crosshairStyle = (c.crosshairStyle + n - 1) % n, () -> c.crosshairStyle = (c.crosshairStyle + 1) % n, w);
                // colors
                int y = rowY(2);
                for (int i = 0; i < KelpConfig.COLORS.length; i++) {
                    final int idx = i;
                    ButtonWidget sw2 = add(ButtonWidget.builder(Text.empty(), x -> { c.crosshairColor = idx; KelpConfig.save(); })
                            .dimensions(cx + w - 6 * 20 - 4 + i * 20, y + (rowH - 16) / 2, 16, 16).build());
                    overlays.add(g -> {
                        boolean sel = c.crosshairColor == idx;
                        g.fill(sw2.getX() - 2, sw2.getY() - 2, sw2.getX() + 18, sw2.getY() + 18, sel ? KelpUi.ACCENT : (sw2.isHovered() ? KelpUi.DIM : KelpUi.BORDER));
                        g.fill(sw2.getX(), sw2.getY(), sw2.getX() + 16, sw2.getY() + 16, KelpConfig.COLORS[idx]);
                    });
                }
                overlays.add(g -> {
                    KelpUi.rect(g, cx, y, w - 130, rowH, KelpUi.CARD, KelpUi.BORDER);
                    g.drawTextWithShadow(textRenderer, "Color", cx + 8, y + (rowH - 8) / 2, KelpUi.TEXT);
                });
                stepper(3, "Size", () -> String.valueOf(c.crosshairSize),
                        () -> c.crosshairSize = Math.max(1, c.crosshairSize - 1), () -> c.crosshairSize = Math.min(15, c.crosshairSize + 1), w);
                stepper(4, "Gap", () -> String.valueOf(c.crosshairGap),
                        () -> c.crosshairGap = Math.max(0, c.crosshairGap - 1), () -> c.crosshairGap = Math.min(8, c.crosshairGap + 1), w);
                stepper(5, "Thickness", () -> String.valueOf(c.crosshairThick),
                        () -> c.crosshairThick = Math.max(1, c.crosshairThick - 1), () -> c.crosshairThick = Math.min(4, c.crosshairThick + 1), w);
                int pvx = cx + cw - 104, pvy = cy;
                overlays.add(g -> {
                    KelpUi.rect(g, pvx, pvy, 104, 104, 0xFF0B1512, KelpUi.BORDER);
                    g.drawCenteredTextWithShadow(textRenderer, "Preview", pvx + 52, pvy + 6, KelpUi.DIM);
                    KelpHud.drawCrosshairAt(g, pvx + 52, pvy + 58);
                });
            }
            default -> {
                toggle(0, new T("Kelp Tag", "Kelp icon before your name", () -> c.kelpTag, v -> c.kelpTag = v), cw);
                overlays.add(g -> {
                    int y = rowY(1);
                    KelpUi.rect(g, cx, y, cw, 62, KelpUi.CARD, KelpUi.BORDER);
                    String[] t = {"Shows in the tab list and on your name tag",
                            "in third person.", "Other Kelp players can't be detected without",
                            "a server, so it only shows on your own name."};
                    for (int i = 0; i < t.length; i++) g.drawTextWithShadow(textRenderer, t[i], cx + 8, y + 8 + i * 12, KelpUi.DIM);
                });
            }
        }
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) {
        KelpUi.background(ctx, width, height, client.world != null);
        KelpUi.panel(ctx, px, py, pw, ph, KelpUi.PANEL, KelpUi.BORDER);
        ctx.drawTextWithShadow(textRenderer, "KELP MODS", px + 12, py + 10, KelpUi.ACCENT);
        ctx.drawTextWithShadow(textRenderer, TABS[tab], cx, py + 10, KelpUi.DIM);
        ctx.fill(px + 8 + 104 + 4, py + 28, px + 8 + 104 + 5, py + ph - 32, KelpUi.BORDER);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        for (var o : overlays) o.accept(ctx);
    }
}
