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
