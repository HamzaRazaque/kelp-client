package com.kelpclient.ui;

import com.kelpclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Shared editor for a list of text rows (waypoints, friends). */
public abstract class ListScreen extends KScreen {
	protected TextFieldWidget field;
	protected int off, rows, listY;
	private static final int ROW_H = 18;
	private final List<ClickableWidget> rowW = new ArrayList<>();

	protected ListScreen(String title, Screen parent) { super(title, parent); }

	protected abstract List<String> rowsText();
	protected abstract void removeAt(int i);
	protected abstract void addEntry(String text);
	protected abstract String addLabel();
	protected abstract String hint();
	protected Module linked() { return null; }

	@Override
	protected void build() {
		int y = py + 28;
		Module lm = linked();
		if (lm != null) {
			addDrawableChild(new KButton(KButton.Mode.SWITCH, px + pw - 48, y, 38, 16, "", null, b -> lm.setEnabled(!lm.enabled)).on(() -> lm.enabled));
			y += 22;
		}
		field = new TextFieldWidget(textRenderer, px + 10, y, pw - 20 - 90, 16, Text.literal(""));
		field.setPlaceholder(Text.literal(hint()));
		field.setMaxLength(24);
		addDrawableChild(field);
		addDrawableChild(new KButton(KButton.Mode.PLAIN, px + pw - 10 - 84, y, 84, 16, addLabel(), null, b -> {
			addEntry(field.getText().trim());
			field.setText("");
			rebuild();
		}));
		listY = y + 22;
		rows = Math.max(1, (py + ph - 34 - listY) / ROW_H);
		rebuild();
	}

	protected void rebuild() {
		for (ClickableWidget w : rowW) remove(w);
		rowW.clear();
		List<String> r = rowsText();
		off = Math.max(0, Math.min(off, Math.max(0, r.size() - rows)));
		for (int i = 0; i < rows && off + i < r.size(); i++) {
			final int idx = off + i;
			KButton x = new KButton(KButton.Mode.GHOST, px + pw - 28, listY + i * ROW_H, 16, 16, "", "x", b -> { removeAt(idx); rebuild(); });
			rowW.add(x); addDrawableChild(x);
		}
	}

	@Override
	protected void drawContent(DrawContext c, int mx, int my) {
		Module lm = linked();
		if (lm != null) Theme.text(c, lm.name + " module", px + 10, py + 32, Theme.TEXT);
		List<String> r = rowsText();
		if (r.isEmpty()) Theme.text(c, "Nothing here yet.", px + 10, listY + 4, Theme.DIM);
		for (int i = 0; i < rows && off + i < r.size(); i++) {
			int y = listY + i * ROW_H;
			Theme.box(c, px + 10, y, pw - 44, 16, Theme.PANEL, Theme.BORDER);
			Theme.text(c, Theme.fit(r.get(off + i), pw - 56), px + 16, y + 4, Theme.TEXT);
		}
	}

	@Override protected boolean onScroll(int dir) { off += dir; rebuild(); return true; }
}
