package com.kelpclient;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public class KelpConfig {
    public static class Waypoint { public String name; public int x, y, z; public Waypoint(String n, int x, int y, int z){name=n;this.x=x;this.y=y;this.z=z;} }
    public static final int[] COLORS = {0xFFFFFFFF, 0xFF55FF55, 0xFFFF5555, 0xFF55FFFF, 0xFFFFFF55, 0xFFFF55FF};
    public static final String[] STYLES = {"Cross", "Dot", "Square", "Plus (gap)"};

    public boolean kelpUi = true;
    // every Kelp mod starts OFF
    public boolean fps, coords, direction, day, ping, serverInfo, potions, combo, waypoints;
    public boolean customCrosshair, kelpTag;
    public int crosshairStyle = 0, crosshairColor = 1, crosshairSize = 5, crosshairGap = 2, crosshairThick = 1;
    public List<Waypoint> waypointList = new ArrayList<>();
    public List<String> accounts = new ArrayList<>();

    private static KelpConfig inst;
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("kelpclient.json"); }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static KelpConfig get() { if (inst == null) load(); return inst; }
    public static void load() {
        try { inst = Files.exists(file()) ? GSON.fromJson(Files.readString(file()), KelpConfig.class) : new KelpConfig(); }
        catch (Exception e) { inst = new KelpConfig(); }
        if (inst == null) inst = new KelpConfig();
    }
    public static void save() { try { Files.writeString(file(), GSON.toJson(get())); } catch (Exception ignored) {} }
}
