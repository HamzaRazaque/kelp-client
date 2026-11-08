package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Cat;
import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class ClientSettingsScreen extends KScreen {
	private String status = "Right Shift opens this menu anywhere in a world.";

	public ClientSettingsScreen(Screen parent) { super("CLIENT SETTINGS", parent); }

	@Override
	protected void layout() {
		pw = Math.min(width - 16, 340); ph = Math.min(height - 16, 210);
		px = (width - pw) / 2; py = (height - ph) / 2;
	}

	@Override
	protected void build() {
		int w = pw - 20, y = py + 28;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, w, 20, "Screenshots", "camera", b -> client.setScreen(new ScreenshotsScreen(this))));
		y += 24;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, w, 20, "Kelp Mods (all categories)", "kelp", b -> client.setScreen(new ModSettingsScreen(this, Cat.HUD))));
		y += 24;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, w, 20, "Reset HUD positions", "monitor", b -> {
			for (Module m : Modules.ALL) m.resetPos();
			Config.save(); status = "HUD modules are back in their default spots.";
		}));
		y += 24;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, w, 20, "Turn every module off", "x", b -> {
			for (Module m : Modules.ALL) m.setEnabled(false);
			Config.save(); status = "All modules are off.";
		}));
		y += 24;
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, w, 20, "Reset everything to default", "gear", b -> {
			Modules.resetAll(); status = "Modules, settings and positions reset.";
		}));
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		Theme.text(c, Theme.fit(status, pw - 100), px + 10, py + ph - 20, Theme.DIM);
	}
}
