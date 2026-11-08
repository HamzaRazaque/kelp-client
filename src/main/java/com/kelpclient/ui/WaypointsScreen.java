package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

public class WaypointsScreen extends ListScreen {
	public WaypointsScreen(Screen parent) { super("WAYPOINTS", parent); }

	@Override protected Module linked() { return Modules.BY_ID.get("waypoints"); }
	@Override protected String addLabel() { return "Add here"; }
	@Override protected String hint() { return "Name (optional)"; }

	@Override
	protected List<String> rowsText() {
		List<String> out = new ArrayList<>();
		for (Config.Waypoint w : Config.waypoints) {
			String d = w.dim.contains("nether") ? "Nether" : w.dim.contains("end") ? "End" : "Overworld";
			out.add(w.name + "  (" + w.x + ", " + w.y + ", " + w.z + ")  " + d);
		}
		return out;
	}

	@Override protected void removeAt(int i) { if (i >= 0 && i < Config.waypoints.size()) Config.waypoints.remove(i); Config.save(); }

	@Override
	protected void addEntry(String text) {
		if (client == null || client.player == null || client.world == null) return;
		String name = text.isEmpty() ? "Waypoint " + (Config.waypoints.size() + 1) : text;
		Config.waypoints.add(new Config.Waypoint(name, client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ(),
				client.world.getRegistryKey().getValue().toString()));
		Config.save();
	}
}
