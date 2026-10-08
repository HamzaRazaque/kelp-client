package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** The menu that opens with Right Shift. */
public class KelpMenuScreen extends Screen {
    private final Screen parent;
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private int px, py, pw, ph;

    public KelpMenuScreen(Screen parent) { super(Text.literal("Kelp Client")); this.parent = parent; }

    private ButtonWidget card(ButtonWidget b, String icon, boolean accent) {
        addDrawableChild(b);
        overlays.add(c -> KelpUi.card(c, textRenderer, b, icon, accent));
        return b;
    }
    private static Text tagLabel() { return Text.literal("Kelp Tag: " + (KelpConfig.get().kelpTag ? "ON" : "OFF")); }
    private static Text uiLabel() { return Text.literal(KelpConfig.get().kelpUi ? "Switch to Minecraft UI" : "Switch to Kelp UI"); }

    @Override
    protected void init() {
        overlays.clear();
        var c = KelpConfig.get();
        pw = 240; ph = 6 * 28 + 44;
        px = (width - pw) / 2; py = Math.max(6, (height - ph) / 2);
        int x = px + 12, w = pw - 24, y = py + 32, st = 28;
        card(ButtonWidget.builder(Text.literal("Kelp Mods"), b -> client.setScreen(new KelpModsScreen(this))).dimensions(x, y, w, 24).build(), "kelpmods", true);
        card(ButtonWidget.builder(tagLabel(), b -> { c.kelpTag = !c.kelpTag; KelpConfig.save(); b.setMessage(tagLabel()); }).dimensions(x, y + st, w, 24).build(), "tag", false);
        card(ButtonWidget.builder(Text.literal("Accounts"), b -> client.setScreen(new KelpAccountScreen(this))).dimensions(x, y + st * 2, w, 24).build(), "account", false);
        card(ButtonWidget.builder(uiLabel(), b -> {
            c.kelpUi = !c.kelpUi; KelpConfig.save(); b.setMessage(uiLabel());
            if (client.world == null) client.setScreen(c.kelpUi ? new KelpTitleScreen() : new TitleScreen());
        }).dimensions(x, y + st * 3, w, 24).build(), "switch", false);
        card(ButtonWidget.builder(Text.translatable("menu.options"), b -> client.setScreen(new OptionsScreen(this, client.options))).dimensions(x, y + st * 4, w, 24).build(), "options", false);
        card(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x, y + st * 5 + 4, w, 24).build(), null, false);
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) {
        KelpUi.background(ctx, width, height, client.world != null);
        KelpUi.panel(ctx, px, py, pw, ph, KelpUi.PANEL, KelpUi.BORDER);
        ctx.drawCenteredTextWithShadow(textRenderer, "KELP CLIENT", width / 2, py + 12, KelpUi.ACCENT);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        for (var o : overlays) o.accept(ctx);
    }
}
