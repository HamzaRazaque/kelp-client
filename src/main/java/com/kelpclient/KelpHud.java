package com.kelpclient;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Draggable HUD modules. Content is rebuilt only every few ticks so frames stay cheap. */
public final class KelpHud {
    private KelpHud() {}
    /** True while the Right Shift menu is open (modules show sample data and can be dragged). */
    public static boolean editing;

    static final int GREEN = 0xFF55FF55, YELLOW = 0xFFFFFF55, RED = 0xFFFF5555, ORANGE = 0xFFFFAA00, WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA;
    private static int combo, lastHurt;
    private static long lastHitMs, lastTargetMs;
    private static LivingEntity lastTarget;

    public record Row(String label, String value, int color) {}

    public abstract static class HudModule {
        public final String id, name;
        public final boolean fast;
        private final BooleanSupplier on;
        private final Consumer<Boolean> setter;
        public int x, y, w = 60, h = 18;
        HudModule(String id, String name, boolean fast, BooleanSupplier on, Consumer<Boolean> setter) {
            this.id = id; this.name = name; this.fast = fast; this.on = on; this.setter = setter;
        }
        public boolean isOn() { return on.getAsBoolean(); }
        public void setOn(boolean v) { setter.accept(v); }
        abstract void update(MinecraftClient mc);
        abstract boolean hasContent();
        abstract void draw(DrawContext ctx, TextRenderer tr, int x, int y);
    }

    static class RowModule extends HudModule {
        private final Supplier<List<Row>> real;
        private final List<Row> sample;
        private List<Row> rows = List.of();
        RowModule(String id, String name, boolean fast, BooleanSupplier on, Consumer<Boolean> set, Supplier<List<Row>> real, List<Row> sample) {
            super(id, name, fast, on, set); this.real = real; this.sample = sample;
        }
        @Override void update(MinecraftClient mc) {
            List<Row> r = real.get();
            if (r.isEmpty() && editing) r = sample;
            rows = r;
            TextRenderer tr = mc.textRenderer;
            int mw = 0;
            for (Row row : rows) mw = Math.max(mw, tr.getWidth(row.label()) + (row.value().isEmpty() ? 0 : 5 + tr.getWidth(row.value())));
            w = mw + 14; h = rows.size() * 11 + 7;
        }
        @Override boolean hasContent() { return !rows.isEmpty(); }
        @Override void draw(DrawContext ctx, TextRenderer tr, int x, int y) {
            KelpUi.soft(ctx, x, y, w, h, 0xB0081410);
            ctx.fill(x + 1, y + 2, x + 3, y + h - 2, KelpUi.ACCENT);
            int ty = y + 4;
            for (Row r : rows) {
                ctx.drawTextWithShadow(tr, r.label(), x + 8, ty, KelpUi.DIM);
                if (!r.value().isEmpty()) ctx.drawTextWithShadow(tr, r.value(), x + 8 + tr.getWidth(r.label()) + 5, ty, r.color());
                ty += 11;
            }
        }
    }

    static class HealthModule extends HudModule {
        private String nameTxt = "", hpTxt = "";
        private float hp, max = 20;
        HealthModule() { super("health", "Health Indicator", true, () -> KelpConfig.get().healthIndicator, v -> KelpConfig.get().healthIndicator = v); }
        private static String fmt(float f) { return f == Math.floor(f) ? String.valueOf((int) f) : String.format("%.1f", f); }
        @Override void update(MinecraftClient mc) {
            LivingEntity t = null;
            long now = System.currentTimeMillis();
            Entity te = mc.targetedEntity;
            if (te instanceof LivingEntity le && le.isAlive() && le != mc.player) { t = le; lastTarget = le; lastTargetMs = now; }
            else if (lastTarget != null && lastTarget.isAlive() && now - lastTargetMs < 3000) t = lastTarget;
            if (t != null) { nameTxt = t.getName().getString(); hp = t.getHealth(); max = Math.max(1f, t.getMaxHealth()); }
            else if (editing) { nameTxt = "Zombie"; hp = 14f; max = 20f; }
            else { nameTxt = ""; return; }
            hpTxt = fmt(hp) + " / " + fmt(max);
            TextRenderer tr = mc.textRenderer;
            int hearts = (int) Math.ceil(max / 2f);
            boolean bar = hearts > 20;
            int heartsW = bar ? 90 : Math.min(hearts, 10) * 8;
            int hRows = bar ? 1 : (hearts + 9) / 10;
            w = Math.max(tr.getWidth(nameTxt) + 10 + tr.getWidth(hpTxt), heartsW) + 14;
            h = 4 + 11 + (bar ? 8 : hRows * 8) + 5;
        }
        @Override boolean hasContent() { return !nameTxt.isEmpty(); }
        @Override void draw(DrawContext ctx, TextRenderer tr, int x, int y) {
            KelpUi.soft(ctx, x, y, w, h, 0xB0081410);
            ctx.fill(x + 1, y + 2, x + 3, y + h - 2, 0xFFE23B3B);
            ctx.drawTextWithShadow(tr, nameTxt, x + 8, y + 4, WHITE);
            float ratio = hp / max;
            ctx.drawTextWithShadow(tr, hpTxt, x + w - 6 - tr.getWidth(hpTxt), y + 4, ratio > 0.5f ? GREEN : ratio > 0.25f ? YELLOW : RED);
            int hearts = (int) Math.ceil(max / 2f);
            if (hearts > 20) {
                int bw = w - 16;
                ctx.fill(x + 8, y + 16, x + 8 + bw, y + 22, 0xFF3A2A2A);
                ctx.fill(x + 8, y + 16, x + 8 + (int) (bw * Math.min(1f, ratio)), y + 22, 0xFFE23B3B);
            } else {
                for (int i = 0; i < hearts; i++) heart(ctx, x + 8 + (i % 10) * 8, y + 16 + (i / 10) * 8, hp >= (i + 1) * 2 ? 2 : hp >= i * 2 + 1 ? 1 : 0);
            }
        }
    }

    private static final int[][] HEART_RECTS = {{0, 1, 3}, {0, 4, 6}, {1, 0, 7}, {2, 0, 7}, {3, 1, 6}, {4, 2, 5}, {5, 3, 4}};
    private static void heart(DrawContext c, int x, int y, int state) {
        int full = 0xFFE23B3B, empty = 0xFF3A2A2A;
        for (int[] r : HEART_RECTS) {
            int yy = y + r[0];
            if (state == 2) c.fill(x + r[1], yy, x + r[2], yy + 1, full);
            else if (state == 0) c.fill(x + r[1], yy, x + r[2], yy + 1, empty);
            else {
                int a1 = Math.min(r[2], 4);
                if (a1 > r[1]) c.fill(x + r[1], yy, x + a1, yy + 1, full);
                int b0 = Math.max(r[1], 4);
                if (r[2] > b0) c.fill(x + b0, yy, x + r[2], yy + 1, empty);
            }
        }
        if (state == 2) c.fill(x + 1, y + 1, x + 2, y + 2, 0x99FFFFFF);
    }

    // ---- row suppliers ----
    private static MinecraftClient mc() { return MinecraftClient.getInstance(); }
    private static List<Row> rowsFps() {
        int f = mc().getCurrentFps();
        return List.of(new Row("FPS", String.valueOf(f), f >= 60 ? GREEN : f >= 30 ? YELLOW : RED));
    }
    private static List<Row> rowsPing() {
        var m = mc();
        if (m.player == null || m.isInSingleplayer() || m.getNetworkHandler() == null) return List.of();
        PlayerListEntry e = m.getNetworkHandler().getPlayerListEntry(m.player.getUuid());
        if (e == null) return List.of();
        int p = e.getLatency();
        return List.of(new Row("Ping", p + " ms", p < 80 ? GREEN : p < 150 ? YELLOW : RED));
    }
    private static List<Row> rowsServer() {
        var s = mc().getCurrentServerEntry();
        if (s == null) return List.of();
        return List.of(new Row("Server", s.address, WHITE), new Row("Version", s.version.getString(), GRAY));
    }
    private static List<Row> rowsCoords() {
        var p = mc().player;
        if (p == null) return List.of();
        return List.of(new Row("XYZ", MathHelper.floor(p.getX()) + " / " + MathHelper.floor(p.getY()) + " / " + MathHelper.floor(p.getZ()), WHITE));
    }
    private static List<Row> rowsDir() {
        var p = mc().player;
        if (p == null) return List.of();
        var d = p.getHorizontalFacing();
        String n = d.name().charAt(0) + d.name().substring(1).toLowerCase();
        return List.of(new Row("Facing", n + " (" + Math.round(MathHelper.wrapDegrees(p.getYaw())) + ")", WHITE));
    }
    private static List<Row> rowsDay() {
        var w = mc().world;
        if (w == null) return List.of();
        return List.of(new Row("Day", String.valueOf(w.getTimeOfDay() / 24000L + 1), WHITE));
    }
    private static List<Row> rowsPotions() {
        var p = mc().player;
        if (p == null) return List.of();
        List<Row> out = new ArrayList<>();
        for (StatusEffectInstance ef : p.getStatusEffects()) {
            int s = ef.getDuration() / 20;
            String name = ef.getEffectType().value().getName().getString() + (ef.getAmplifier() > 0 ? " " + (ef.getAmplifier() + 1) : "");
            out.add(new Row(name, s / 60 + ":" + String.format("%02d", s % 60), ef.getEffectType().value().isBeneficial() ? GREEN : RED));
        }
        return out;
    }
    private static List<Row> rowsCombo() { return combo > 1 ? List.of(new Row("Combo", combo + "x", ORANGE)) : List.of(); }

    public static final List<HudModule> MODULES = new ArrayList<>();
    static {
        MODULES.add(new RowModule("fps", "FPS", false, () -> KelpConfig.get().fps, v -> KelpConfig.get().fps = v, KelpHud::rowsFps, List.of(new Row("FPS", "240", GREEN))));
        MODULES.add(new RowModule("ping", "Ping", false, () -> KelpConfig.get().ping, v -> KelpConfig.get().ping = v, KelpHud::rowsPing, List.of(new Row("Ping", "32 ms", GREEN))));
        MODULES.add(new RowModule("server", "Server Info", false, () -> KelpConfig.get().serverInfo, v -> KelpConfig.get().serverInfo = v, KelpHud::rowsServer,
                List.of(new Row("Server", "play.example.net", WHITE), new Row("Version", "1.21.11", GRAY))));
        MODULES.add(new RowModule("coords", "Coordinates", false, () -> KelpConfig.get().coords, v -> KelpConfig.get().coords = v, KelpHud::rowsCoords, List.of(new Row("XYZ", "120 / 64 / -33", WHITE))));
        MODULES.add(new RowModule("direction", "Direction", false, () -> KelpConfig.get().direction, v -> KelpConfig.get().direction = v, KelpHud::rowsDir, List.of(new Row("Facing", "North (180)", WHITE))));
        MODULES.add(new RowModule("day", "Day Counter", false, () -> KelpConfig.get().day, v -> KelpConfig.get().day = v, KelpHud::rowsDay, List.of(new Row("Day", "12", WHITE))));
        MODULES.add(new RowModule("potions", "Potion Effects", false, () -> KelpConfig.get().potions, v -> KelpConfig.get().potions = v, KelpHud::rowsPotions,
                List.of(new Row("Speed 2", "1:24", GREEN), new Row("Strength", "0:42", GREEN))));
        MODULES.add(new RowModule("combo", "Combo Counter", true, () -> KelpConfig.get().combo, v -> KelpConfig.get().combo = v, KelpHud::rowsCombo, List.of(new Row("Combo", "5x", ORANGE))));
        MODULES.add(new HealthModule());
    }

    static void onAttack(Entity e) {
        combo++; lastHitMs = System.currentTimeMillis();
        if (e instanceof LivingEntity le) { lastTarget = le; lastTargetMs = lastHitMs; }
    }

    static void init() {
        HudElementRegistry.addLast(Identifier.of("kelpclient", "hud"), KelpHud::render);
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, prev -> (ctx, tc) -> {
            if (KelpConfig.get().customCrosshair) drawCrosshair(ctx); else prev.render(ctx, tc);
        });
    }

    static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) { combo = 0; return; }
        int hurt = mc.player.hurtTime;
        if (hurt > 0 && lastHurt == 0) combo = 0;
        lastHurt = hurt;
        if (combo > 0 && System.currentTimeMillis() - lastHitMs > 3000) combo = 0;
        boolean slow = mc.player.age % 5 == 0;
        for (HudModule m : MODULES) if (m.isOn() && (m.fast || slow || editing)) m.update(mc);
    }

    private static void render(DrawContext ctx, RenderTickCounter tc) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        var cfg = KelpConfig.get();
        TextRenderer tr = mc.textRenderer;
        int W = ctx.getScaledWindowWidth(), H = ctx.getScaledWindowHeight(), stackY = 6;
        for (HudModule m : MODULES) {
            if (!m.isOn() || !m.hasContent()) continue;
            float[] p = cfg.hudPos.get(m.id);
            if (p != null && p.length == 2) {
                m.x = Math.round(p[0] * Math.max(0, W - m.w));
                m.y = Math.round(p[1] * Math.max(0, H - m.h));
            } else switch (m.id) {
                case "combo" -> { m.x = W / 2 - m.w / 2; m.y = H - 88 - m.h; }   // above the hotbar
                case "health" -> { m.x = W / 2 - m.w / 2; m.y = 12; }
                case "potions" -> { m.x = W - m.w - 6; m.y = 6; }
                default -> { m.x = 6; m.y = stackY; stackY += m.h + 4; }
            }
            m.draw(ctx, tr, m.x, m.y);
        }
        Waypoints.render(ctx, mc, tc);
    }

    private static void drawCrosshair(DrawContext ctx) {
        drawCrosshairAt(ctx, ctx.getScaledWindowWidth() / 2, ctx.getScaledWindowHeight() / 2);
    }

    public static void drawCrosshairAt(DrawContext ctx, int cx, int cy) {
        var c = KelpConfig.get();
        int col = KelpConfig.COLORS[c.crosshairColor % KelpConfig.COLORS.length];
        int s = c.crosshairSize, g = c.crosshairGap, t = c.crosshairThick, h = t / 2;
        switch (c.crosshairStyle) {
            case 1 -> ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, col);
            case 2 -> { ctx.fill(cx - s, cy - s, cx + s + 1, cy - s + t, col); ctx.fill(cx - s, cy + s + 1 - t, cx + s + 1, cy + s + 1, col);
                        ctx.fill(cx - s, cy - s, cx - s + t, cy + s + 1, col); ctx.fill(cx + s + 1 - t, cy - s, cx + s + 1, cy + s + 1, col); }
            case 3 -> { ctx.fill(cx - g - s, cy - h, cx - g, cy - h + t, col); ctx.fill(cx + g + 1, cy - h, cx + g + 1 + s, cy - h + t, col);
                        ctx.fill(cx - h, cy - g - s, cx - h + t, cy - g, col); ctx.fill(cx - h, cy + g + 1, cx - h + t, cy + g + 1 + s, col); }
            default -> { ctx.fill(cx - s, cy - h, cx + s + 1, cy - h + t, col); ctx.fill(cx - h, cy - s, cx - h + t, cy + s + 1, col); }
        }
    }
}
