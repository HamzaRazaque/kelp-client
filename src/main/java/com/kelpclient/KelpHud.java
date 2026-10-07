package com.kelpclient;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/** All text is rebuilt only every few ticks, so rendering a frame is just a handful of draw calls (no FPS cost). */
public final class KelpHud {
    private static final List<String> lines = new ArrayList<>();
    private static int combo, lastHurt;
    private static long lastHitMs;
    private static String comboText = "";

    static void onAttack() { combo++; lastHitMs = System.currentTimeMillis(); }

    static void init() {
        HudElementRegistry.addLast(Identifier.of("kelpclient", "hud"), KelpHud::render);
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, prev -> (ctx, tc) -> {
            if (KelpConfig.get().customCrosshair) drawCrosshair(ctx); else prev.render(ctx, tc);
        });
    }

    static void tick(MinecraftClient mc) {
        var c = KelpConfig.get();
        if (mc.player == null || mc.world == null) { combo = 0; return; }

        // combo logic: reset when we get hurt or after 3s idle
        int hurt = mc.player.hurtTime;
        if (hurt > 0 && lastHurt == 0) combo = 0;
        lastHurt = hurt;
        if (combo > 0 && System.currentTimeMillis() - lastHitMs > 3000) combo = 0;
        comboText = combo > 1 ? combo + " Combo" : "";

        // hitboxes (same thing F3+B toggles)
        if (mc.getEntityRenderDispatcher().shouldRenderHitboxes() != c.hitboxes)
            mc.getEntityRenderDispatcher().setRenderHitboxes(c.hitboxes);

        if (mc.player.age % 5 != 0) return;
        lines.clear();
        if (c.fps) lines.add("FPS: " + mc.getCurrentFps());
        if (c.ping && !mc.isInSingleplayer() && mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (e != null) lines.add("Ping: " + e.getLatency() + " ms");
        }
        if (c.serverInfo) {
            var s = mc.getCurrentServerEntry();
            if (s != null) lines.add("Server: " + s.address + " (" + s.version.getString() + ")");
        }
        if (c.coords) lines.add(String.format("XYZ: %d / %d / %d", MathHelper.floor(mc.player.getX()),
                MathHelper.floor(mc.player.getY()), MathHelper.floor(mc.player.getZ())));
        if (c.direction) {
            var d = mc.player.getHorizontalFacing();
            lines.add("Facing: " + d.name().charAt(0) + d.name().substring(1).toLowerCase()
                    + " (" + Math.round(MathHelper.wrapDegrees(mc.player.getYaw())) + ")");
        }
        if (c.day) lines.add("Day: " + (mc.world.getTimeOfDay() / 24000L + 1));
        if (c.waypoints) for (var w : c.waypointList) {
            double dx = w.x + 0.5 - mc.player.getX(), dz = w.z + 0.5 - mc.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            double ang = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(-dx, dz)) - mc.player.getYaw());
            String arrow = Math.abs(ang) < 22.5 ? "^" : Math.abs(ang) > 157.5 ? "v" : ang > 0 ? ">" : "<";
            lines.add(w.name + " " + arrow + " " + (int) dist + "m");
        }
        if (c.potions) for (StatusEffectInstance ef : mc.player.getStatusEffects()) {
            int s = ef.getDuration() / 20;
            lines.add(ef.getEffectType().value().getName().getString() + " " + (ef.getAmplifier() + 1)
                    + " (" + s / 60 + ":" + String.format("%02d", s % 60) + ")");
        }
    }

    private static void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tc) {
        var mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;
        var tr = mc.textRenderer;
        int y = 4;
        for (String s : lines) { ctx.drawTextWithShadow(tr, s, 4, y, 0xFFFFFFFF); y += 10; }
        if (KelpConfig.get().combo && !comboText.isEmpty())
            ctx.drawCenteredTextWithShadow(tr, comboText, ctx.getScaledWindowWidth() / 2,
                    ctx.getScaledWindowHeight() / 2 + 20, 0xFFFFAA00);
    }

    private static void drawCrosshair(DrawContext ctx) {
        var c = KelpConfig.get();
        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
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
