package com.kelpclient.ui;

import com.kelpclient.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class TransferScreen extends KScreen {
	private String status = "Export copies your whole Kelp setup to the clipboard.";

	public TransferScreen(Screen parent) { super("IMPORT / EXPORT", parent); }

	@Override
	protected void layout() {
		pw = Math.min(width - 16, 340); ph = 120;
		px = (width - pw) / 2; py = (height - ph) / 2;
	}

	@Override
	protected void build() {
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 10, py + 30, (pw - 30) / 2, 20, "Export to clipboard", "arrows", b -> {
			client.keyboard.setClipboard(Config.toJson());
			status = "Copied your settings to the clipboard.";
		}));
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + 20 + (pw - 30) / 2, py + 30, (pw - 30) / 2, 20, "Import from clipboard", "arrows", b -> {
			try {
				Config.fromJson(client.keyboard.getClipboard());
				Config.save();
				status = "Imported settings from the clipboard.";
			} catch (Exception e) {
				status = "Clipboard does not hold Kelp settings.";
			}
		}));
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		Theme.text(c, Theme.fit(status, pw - 20), px + 10, py + 62, Theme.DIM);
	}
}
