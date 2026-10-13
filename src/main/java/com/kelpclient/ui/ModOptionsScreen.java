package com.kelpclient.ui;

import com.kelpclient.Config;
import com.kelpclient.module.Module;
import com.kelpclient.module.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Per-module options: enable, each setting, reset position. */
public class ModOptionsScreen extends KScreen {
	private final Module m;

	public ModOptionsScreen(Screen parent, Module m) { super(m.name.toUpperCase(), parent); this.m = m; }

	@Override
	protected void layout() {
		pw = Math.min(width - 16, 340);
		ph = Math.min(height - 16, 60 + (m.settings.size() + 2) * 24 + 30);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
	}

	@Override
	protected void build() {
		int y = py + 30;
		addDrawableChild(new KButton(KButton.Mode.SWITCH, px + pw - 48, y, 38, 18, "", null, b -> { m.setEnabled(!m.enabled); Config.save(); }).on(() -> m.enabled));
		y += 24;
		for (Setting s : m.settings) {
			final Setting st = s;
			addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 110, y, 100, 18, st.display(), null, b -> { st.next(); b.setMessage(Text.literal(st.display())); Config.save(); }));
			y += 24;
		}
		if (m.kind == Module.Kind.HUD) {
			addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, y, 120, 18, "Reset position", null, b -> { m.resetPos(); Config.save(); }));
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		Theme.text(c, Theme.fit(m.desc, pw - 20), px + 10, py + 20 - 0, Theme.DIM);
		int y = py + 30;
		Theme.text(c, "Enabled", px + 10, y + 5, Theme.TEXT);
		y += 24;
		for (Setting s : m.settings) { Theme.text(c, s.label, px + 10, y + 5, Theme.TEXT); y += 24; }
	}
}
