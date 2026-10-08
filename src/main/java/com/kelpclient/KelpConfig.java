package com.kelpclient;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public class KelpConfig {
    public static class Waypoint {
        public String name = "Waypoint";
        public int x, y, z, color = 4;
        public boolean enabled = true;
        public String world = "", dim = "", kind = "normal"; // kind: normal | death_latest | death_old
        public Waypoint() {}
        public Waypoint(String n, int x, int y, int z) { name = n; this.x = x; this.y = y; this.z = z; }
    }

    public static final int[] COLORS = {0xFFFFFFFF, 0xFF55FF55, 0xFFFF5555, 0xFF55FFFF, 0xFFFFFF55, 0xFFFF55FF};
    public static final String[] STYLES = {"Cross", "Dot", "Square", "Plus (gap)"};
    public static final int[] WP_COLORS = {0xFFE8E8E8, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF2EBF4A, 0xFF00AA00,
            0xFF55FFFF, 0xFF5577FF, 0xFFAA44CC, 0xFFFF55FF, 0xFF999999, 0xFF555555};

    public boolean kelpUi = true;
    // every Kelp mod starts OFF
    public boolean fps, coords, direction, day, ping, serverInfo, potions, combo, healthIndicator, waypoints;
    public boolean customCrosshair, kelpTag;
    public int crosshairStyle = 0, crosshairColor = 1, crosshairSize = 5, crosshairGap = 2, crosshairThick = 1;
    public Map<String, float[]> hudPos = new HashMap<>();
    public List<Waypoint> waypointList = new ArrayList<>();
    public List<String> accounts = new ArrayList<>();
    // cosmetics
    public boolean skinOn, capeOn, slim;
    public String skinFile = "", capeFile = "";

    private static KelpConfig inst;
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("kelpclient.json"); }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static KelpConfig get() { if (inst == null) load(); return inst; }
    public static void load() {
        try { inst = Files.exists(file()) ? GSON.fromJson(Files.readString(file()), KelpConfig.class) : new KelpConfig(); }
        catch (Exception e) { inst = new KelpConfig(); }
        if (inst == null) inst = new KelpConfig();
        if (inst.hudPos == null) inst.hudPos = new HashMap<>();
        if (inst.waypointList == null) inst.waypointList = new ArrayList<>();
        if (inst.accounts == null) inst.accounts = new ArrayList<>();
        inst.waypointList.removeIf(w -> w == null || w.world == null || w.world.isEmpty()); // old-format waypoints
    }
    public static void save() { try { Files.writeString(file(), GSON.toJson(get())); } catch (Exception ignored) {} }
}
