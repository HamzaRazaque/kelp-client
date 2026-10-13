package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

public class SocialScreen extends ListScreen {
	public SocialScreen(Screen parent) { super("SOCIAL", parent); }

	@Override protected Module linked() { return Modules.BY_ID.get("friends"); }
	@Override protected String addLabel() { return "Add friend"; }
	@Override protected String hint() { return "Player name"; }
	@Override protected List<String> rowsText() { return new ArrayList<>(Config.friends); }
	@Override protected void removeAt(int i) { if (i >= 0 && i < Config.friends.size()) Config.friends.remove(i); Config.save(); }

	@Override
	protected void addEntry(String text) {
		if (text.isEmpty()) return;
		for (String f : Config.friends) if (f.equalsIgnoreCase(text)) return;
		Config.friends.add(text);
		Config.save();
	}
}
