package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KelpModsScreen extends Screen {
    private static final String[] TABS = {"HUD", "Gameplay", "Crosshair", "Waypoints", "Social"};
    private static final String[] TAB_ICONS = {"hud", "sword", "crosshair", "pin", "tag"};
    static int tab = 0;
    private static int wpPage = 0;

    private final Screen parent;
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private int px, py, pw, ph, cx, cy, cw, rowH;

    private record T(String name, String desc, BooleanSupplier get, Consumer<Boolean> set) {}

    public KelpModsScreen(Screen parent) { super(Text.literal("Kelp Mods")); this.parent = parent; }
    public static void openTab(int t) { tab = t; }

    private int rowY(int row) { return cy + row * (rowH + 4); }
    private ButtonWidget add(ButtonWidget b) { return addDrawableChild(b); }

    private void toggle(int row, T t, int w) {
        ButtonWidget b = add(ButtonWidget.builder(Text.empty(), x -> { t.set().accept(!t.get().getAsBoolean()); KelpConfig.save(); })
                .dimensions(cx, rowY(row), w, rowH).build());
        overlays.add(g -> KelpUi.toggleRow(g, textRenderer, b, t.name(), t.desc(), t.get().getAsBoolean()));
    }

    private void info(int row, int rows, String... lines) {
        overlays.add(g -> {
            int y = rowY(row), h = rows * (rowH + 4) - 4;
            KelpUi.rect(g, cx, y, cw, h, KelpUi.CARD, KelpUi.BORDER);
            for (int i = 0; i < lines.length; i++) g.drawTextWithShadow(textRenderer, lines[i], cx + 8, y + 8 + i * 12, KelpUi.DIM);
        });
    }

    private void stepper(int row, String label, Supplier<String> value, Runnable dec, Runnable inc, int w) {
        int y = rowY(row), by = y + (rowH - 18) / 2;
        ButtonWidget minus = add(ButtonWidget.builder(Text.literal("<"), x -> { dec.run(); KelpConfig.save(); }).dimensions(cx + w - 94, by, 18, 18).build());
        ButtonWidget plus = add(ButtonWidget.builder(Text.literal(">"), x -> { inc.run(); KelpConfig.save(); }).dimensions(cx + w - 24, by, 18, 18).build());
        overlays.add(g -> {
            KelpUi.rect(g, cx, y, w, rowH, KelpUi.CARD, KelpUi.BORDER);
            g.drawTextWithShadow(textRenderer, label, cx + 8, y + (rowH - 8) / 2, KelpUi.TEXT);
            KelpUi.card(g, textRenderer, minus, null, false);
            KelpUi.card(g, textRenderer, plus, null, false);
            g.drawCenteredTextWithShadow(textRenderer, value.get(), cx + w - 59, y + (rowH - 8) / 2, KelpUi.ACCENT);
        });
    }

    @Override
    protected void init() {
        overlays.clear();
        var c = KelpConfig.get();
        pw = Math.min(450, width - 16); ph = Math.min(320, height - 16);
        px = (width - pw) / 2; py = (height - ph) / 2;
        int sw = 104;
        cx = px + sw + 16; cy = py + 30; cw = pw - sw - 28;
        rowH = Math.max(20, Math.min(30, (ph - 64) / 8 - 4));

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
                info(7, 1, "Tip: press Right Shift in a world to drag modules.");
            }
            case 1 -> {
                toggle(0, new T("Combo Counter", "Hits in a row, shown above the hotbar", () -> c.combo, v -> c.combo = v), cw);
                toggle(1, new T("Health Indicator", "Hearts of the mob or player you target", () -> c.healthIndicator, v -> c.healthIndicator = v), cw);
                info(2, 2, "Move either module with the Right Shift menu.", "Right-click a module there to switch it off.");
            }
            case 2 -> {
                int w = cw - 112;
                toggle(0, new T("Custom Crosshair", null, () -> c.customCrosshair, v -> c.customCrosshair = v), w);
                int n = KelpConfig.STYLES.length;
                stepper(1, "Style", () -> KelpConfig.STYLES[c.crosshairStyle],
                        () -> c.crosshairStyle = (c.crosshairStyle + n - 1) % n, () -> c.crosshairStyle = (c.crosshairStyle + 1) % n, w);
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
            case 3 -> buildWaypoints(c);
            default -> {
                toggle(0, new T("Kelp Tag", "Kelp icon before your name", () -> c.kelpTag, v -> c.kelpTag = v), cw);
                info(1, 2, "Shows in the tab list and on your name tag", "in third person. Other Kelp players cannot be",
                        "detected without a server, so it only shows", "on your own name.");
            }
        }
    }

    private void buildWaypoints(KelpConfig c) {
        toggle(0, new T("Show Waypoints", "Draw markers in the world", () -> c.waypoints, v -> c.waypoints = v), cw);
        ButtonWidget addWp = add(ButtonWidget.builder(Text.literal("Add waypoint here"), x -> {
            if (client.player != null) client.setScreen(new WaypointEditScreen(this, Waypoints.newHere(client), true));
        }).dimensions(cx, rowY(1), cw, Math.max(22, rowH)).build());
        overlays.add(g -> KelpUi.card(g, textRenderer, addWp, "plus", client.player != null));

        List<KelpConfig.Waypoint> list = Waypoints.listForManager(client);
        int per = 5, pages = Math.max(1, (list.size() + per - 1) / per);
        wpPage = Math.min(wpPage, pages - 1);
        if (list.isEmpty()) overlays.add(g -> g.drawTextWithShadow(textRenderer, "No waypoints yet. Press B in a world to add one.", cx, rowY(2) + 6, KelpUi.DIM));
        for (int i = 0; i < per; i++) {
            int idx = wpPage * per + i;
            if (idx >= list.size()) break;
            KelpConfig.Waypoint wp = list.get(idx);
            int y = rowY(2 + i), bs = Math.min(20, rowH - 4), by = y + (rowH - bs) / 2;
            ButtonWidget eye = add(ButtonWidget.builder(Text.literal("show"), x -> { wp.enabled = !wp.enabled; KelpConfig.save(); })
                    .dimensions(cx + cw - 3 * (bs + 4), by, bs, bs).build());
            ButtonWidget edit = add(ButtonWidget.builder(Text.literal("edit"), x -> client.setScreen(new WaypointEditScreen(this, wp, false)))
                    .dimensions(cx + cw - 2 * (bs + 4), by, bs, bs).build());
            ButtonWidget del = add(ButtonWidget.builder(Text.literal("delete"), x -> { KelpConfig.get().waypointList.remove(wp); KelpConfig.save(); clearAndInit(); })
                    .dimensions(cx + cw - (bs + 4), by, bs, bs).build());
            overlays.add(g -> {
                KelpUi.rect(g, cx, y, cw, rowH, KelpUi.CARD, KelpUi.BORDER);
                int textColor = wp.enabled ? KelpUi.TEXT : KelpUi.DIM;
                if (wp.kind.startsWith("death")) KelpIcons.at(g, "skull", cx + 4, y + (rowH - 16) / 2);
                else {
                    int col = KelpConfig.WP_COLORS[Math.floorMod(wp.color, KelpConfig.WP_COLORS.length)];
                    g.fill(cx + 6, y + (rowH - 12) / 2, cx + 18, y + (rowH - 12) / 2 + 12, wp.enabled ? col : 0xFF444444);
                }
                boolean two = rowH >= 26;
                g.drawTextWithShadow(textRenderer, wp.name, cx + 24, two ? y + 4 : y + (rowH - 8) / 2, textColor);
                if (two) g.drawTextWithShadow(textRenderer, wp.x + ", " + wp.y + ", " + wp.z, cx + 24, y + rowH - 12, KelpUi.DIM);
                KelpUi.iconButton(g, eye, wp.enabled ? "eye" : "eye_off", KelpUi.ACCENT);
                KelpUi.iconButton(g, edit, "edit", KelpUi.ACCENT);
                KelpUi.iconButton(g, del, "trash", KelpUi.RED);
            });
        }
        if (pages > 1) {
            int fy = py + ph - 28;
            ButtonWidget prev = add(ButtonWidget.builder(Text.literal("<"), x -> { wpPage = Math.max(0, wpPage - 1); clearAndInit(); }).dimensions(cx, fy, 22, 22).build());
            ButtonWidget next = add(ButtonWidget.builder(Text.literal(">"), x -> { wpPage = Math.min(pages - 1, wpPage + 1); clearAndInit(); }).dimensions(cx + 26, fy, 22, 22).build());
            final int pg = wpPage;
            overlays.add(g -> {
                KelpUi.card(g, textRenderer, prev, null, false);
                KelpUi.card(g, textRenderer, next, null, false);
                g.drawTextWithShadow(textRenderer, (pg + 1) + "/" + pages, cx + 54, fy + 7, KelpUi.DIM);
            });
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
