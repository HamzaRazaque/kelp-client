package com.kelpclient;

import com.google.gson.*;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import com.kelpclient.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final List<String> friends = new ArrayList<>();
	public static final List<Waypoint> waypoints = new ArrayList<>();

	public static final class Waypoint {
		public final String name, dim;
		public final int x, y, z;
		public Waypoint(String name, int x, int y, int z, String dim) { this.name = name; this.x = x; this.y = y; this.z = z; this.dim = dim; }
	}

	private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("kelpclient.json"); }

	public static String toJson() {
		JsonObject root = new JsonObject();
		JsonObject mods = new JsonObject();
		for (Module m : Modules.ALL) {
			JsonObject o = new JsonObject();
			o.addProperty("enabled", m.enabled);
			if (m.placed()) { o.addProperty("x", m.x); o.addProperty("y", m.y); }
			JsonObject s = new JsonObject();
			for (Setting st : m.settings) s.addProperty(st.key, st.v);
			o.add("settings", s);
			mods.add(m.id, o);
		}
		root.add("modules", mods);
		JsonArray f = new JsonArray();
		for (String n : friends) f.add(n);
		root.add("friends", f);
		JsonArray w = new JsonArray();
		for (Waypoint p : waypoints) {
			JsonObject o = new JsonObject();
			o.addProperty("name", p.name); o.addProperty("x", p.x); o.addProperty("y", p.y); o.addProperty("z", p.z); o.addProperty("dim", p.dim);
			w.add(o);
		}
		root.add("waypoints", w);
		return GSON.toJson(root);
	}

	public static void fromJson(String json) {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		if (root.has("modules")) {
			JsonObject mods = root.getAsJsonObject("modules");
			for (Module m : Modules.ALL) {
				if (!mods.has(m.id)) continue;
				JsonObject o = mods.getAsJsonObject(m.id);
				if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
				if (o.has("x") && o.has("y")) { m.x = o.get("x").getAsInt(); m.y = o.get("y").getAsInt(); }
				if (o.has("settings")) {
					JsonObject s = o.getAsJsonObject("settings");
					for (Setting st : m.settings) if (s.has(st.key)) { st.v = s.get(st.key).getAsInt(); st.clamp(); }
				}
			}
		}
		friends.clear();
		if (root.has("friends")) for (JsonElement e : root.getAsJsonArray("friends")) friends.add(e.getAsString());
		waypoints.clear();
		if (root.has("waypoints")) for (JsonElement e : root.getAsJsonArray("waypoints")) {
			JsonObject o = e.getAsJsonObject();
			waypoints.add(new Waypoint(o.get("name").getAsString(), o.get("x").getAsInt(), o.get("y").getAsInt(), o.get("z").getAsInt(), o.get("dim").getAsString()));
		}
	}

	public static void save() {
		try { Files.writeString(file(), toJson()); } catch (Exception e) { System.err.println("[Kelp] could not save config: " + e); }
	}

	public static void load() {
		try {
			if (Files.exists(file())) fromJson(Files.readString(file()));
		} catch (Exception e) { System.err.println("[Kelp] could not read config: " + e); }
	}
}
