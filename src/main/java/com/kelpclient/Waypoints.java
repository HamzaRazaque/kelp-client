package com.kelpclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** In-world waypoint markers, death waypoints and helpers for the waypoint screens. */
public final class Waypoints {
    private Waypoints() {}
    private static double lx, ly, lz;
    private static boolean dead;

    public static String worldKey(MinecraftClient mc) {
        if (mc.getCurrentServerEntry() != null) return "mp:" + mc.getCurrentServerEntry().address;
        var srv = mc.getServer();
        if (srv != null) return "sp:" + srv.getSaveProperties().getLevelName();
        return "unknown";
    }
    public static String dimKey(MinecraftClient mc) {
        return mc.world == null ? "" : mc.world.getRegistryKey().getValue().toString();
    }

    public static KelpConfig.Waypoint newHere(MinecraftClient mc) {
        String wk = worldKey(mc);
        int n = 1;
        for (var w : KelpConfig.get().waypointList) if ("normal".equals(w.kind) && wk.equals(w.world)) n++;
        var w = new KelpConfig.Waypoint("Waypoint " + n, MathHelper.floor(mc.player.getX()), MathHelper.floor(mc.player.getY()), MathHelper.floor(mc.player.getZ()));
        w.world = wk; w.dim = dimKey(mc); w.color = 4;
        return w;
    }

    /** Waypoints shown in the manager: this world's when in a world, otherwise all of them. */
    public static List<KelpConfig.Waypoint> listForManager(MinecraftClient mc) {
        List<KelpConfig.Waypoint> out = new ArrayList<>();
        String wk = mc.world != null ? worldKey(mc) : null;
        for (var w : KelpConfig.get().waypointList) if (wk == null || wk.equals(w.world)) out.add(w);
        out.sort(Comparator.comparingInt(w -> "normal".equals(w.kind) ? 0 : "death_latest".equals(w.kind) ? 1 : 2));
        return out;
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) { dead = false; return; }
        if (mc.player.getHealth() > 0f) {
            lx = mc.player.getX(); ly = mc.player.getY(); lz = mc.player.getZ(); dead = false;
        } else if (!dead) {
            dead = true;
            recordDeath(mc);
        }
    }

    private static void recordDeath(MinecraftClient mc) {
        var c = KelpConfig.get();
        String wk = worldKey(mc);
        c.waypointList.removeIf(w -> "death_old".equals(w.kind) && wk.equals(w.world));
        for (var w : c.waypointList) if ("death_latest".equals(w.kind) && wk.equals(w.world)) { w.kind = "death_old"; w.name = "Old Death"; }
        var n = new KelpConfig.Waypoint("Latest Death", MathHelper.floor(lx), MathHelper.floor(ly), MathHelper.floor(lz));
        n.kind = "death_latest"; n.world = wk; n.dim = dimKey(mc); n.color = 0;
        c.waypointList.add(n);
        KelpConfig.save();
    }

    public static void render(DrawContext ctx, MinecraftClient mc, RenderTickCounter tc) {
        var cfg = KelpConfig.get();
        if (!cfg.waypoints || mc.world == null || mc.player == null || cfg.waypointList.isEmpty()) return;
        Camera cam = mc.gameRenderer.getCamera();
        Vec3d cp = cam.getCameraPos();
        double yr = Math.toRadians(cam.getYaw()), pr = Math.toRadians(cam.getPitch());
        double fx = -Math.sin(yr) * Math.cos(pr), fy = -Math.sin(pr), fz = Math.cos(yr) * Math.cos(pr);
        double rx = -Math.cos(yr), ry = 0, rz = -Math.sin(yr);
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx;
        int W = ctx.getScaledWindowWidth(), H = ctx.getScaledWindowHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0), aspect = W / (double) H;
        String wk = worldKey(mc), dk = dimKey(mc);
        TextRenderer tr = mc.textRenderer;
        for (var w : cfg.waypointList) {
            if (!w.enabled || !wk.equals(w.world) || !dk.equals(w.dim)) continue;
            double dx = w.x + 0.5 - cp.x, dy = w.y + 1.0 - cp.y, dz = w.z + 0.5 - cp.z;
            double z = dx * fx + dy * fy + dz * fz;
            if (z < 0.1) continue;
            double x = dx * rx + dy * ry + dz * rz, y = dx * ux + dy * uy + dz * uz;
            int sx = (int) (W / 2.0 * (1 + x / (z * tanHalf * aspect)));
            int sy = (int) (H / 2.0 * (1 - y / (z * tanHalf)));
            if (sx < -30 || sx > W + 30 || sy < -30 || sy > H + 30) continue;
            double dist = Math.sqrt(dx * dx + (w.y - cp.y) * (w.y - cp.y) + dz * dz);
            boolean near = Math.abs(sx - W / 2) < 26 && Math.abs(sy - H / 2) < 26;
            marker(ctx, tr, w, sx, sy, dist, near);
        }
    }

    private static void marker(DrawContext ctx, TextRenderer tr, KelpConfig.Waypoint w, int sx, int sy, double dist, boolean near) {
        int col = KelpConfig.WP_COLORS[Math.floorMod(w.color, KelpConfig.WP_COLORS.length)];
        if (w.kind.startsWith("death")) {
            KelpIcons.at(ctx, "skull", sx - 8, sy - 8);
        } else {
            ctx.fill(sx - 8, sy - 8, sx + 8, sy + 8, 0xFF0A0A0A);
            ctx.fill(sx - 7, sy - 7, sx + 7, sy + 7, col);
            String ini = w.name.isEmpty() ? "?" : w.name.substring(0, 1).toUpperCase();
            ctx.drawCenteredTextWithShadow(tr, ini, sx, sy - 4, 0xFFFFFFFF);
        }
        ctx.drawCenteredTextWithShadow(tr, w.name, sx, sy + 10, 0xFFFFFFFF);
        if (near) ctx.drawCenteredTextWithShadow(tr, (int) dist + "m", sx, sy + 20, KelpUi.DIM);
    }
}
