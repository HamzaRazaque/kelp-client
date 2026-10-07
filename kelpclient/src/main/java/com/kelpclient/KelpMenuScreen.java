package com.kelpclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** The menu that opens with Right Shift. */
public class KelpMenuScreen extends Screen {
    private final Screen parent;
    private final List<ButtonWidget> btns = new ArrayList<>();
    private final List<String> names = new ArrayList<>();

    public KelpMenuScreen(Screen parent) { super(Text.literal("Kelp Client")); this.parent = parent; }

    private ButtonWidget add(ButtonWidget b, String icon) { btns.add(b); names.add(icon); return addDrawableChild(b); }
    private static Text tagLabel() { return Text.literal("Kelp Tag: " + (KelpConfig.get().kelpTag ? "ON" : "OFF")); }
    private static Text uiLabel() { return Text.literal(KelpConfig.get().kelpUi ? "Switch to Minecraft UI" : "Switch to Kelp UI"); }

    @Override
    protected void init() {
        btns.clear(); names.clear();
        var c = KelpConfig.get();
        int x = width / 2 - 100, y = height / 2 - 84, st = 24;
        add(ButtonWidget.builder(Text.literal("Kelp Mods"), b -> client.setScreen(new KelpModsScreen(this))).dimensions(x, y, 200, 20).build(), "kelpmods");
        add(ButtonWidget.builder(tagLabel(), b -> { c.kelpTag = !c.kelpTag; KelpConfig.save(); b.setMessage(tagLabel()); }).dimensions(x, y + st, 200, 20).build(), "kelpmods");
        add(ButtonWidget.builder(Text.literal("Accounts"), b -> client.setScreen(new KelpAccountScreen(this))).dimensions(x, y + st * 2, 200, 20).build(), "account");
        add(ButtonWidget.builder(uiLabel(), b -> {
            c.kelpUi = !c.kelpUi; KelpConfig.save(); b.setMessage(uiLabel());
            if (client.world == null) client.setScreen(c.kelpUi ? new KelpTitleScreen() : new TitleScreen());
        }).dimensions(x, y + st * 3, 200, 20).build(), "switch");
        add(ButtonWidget.builder(Text.translatable("menu.options"), b -> client.setScreen(new OptionsScreen(this, client.options))).dimensions(x, y + st * 4, 200, 20).build(), "options");

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x, height - 28, 200, 20).build());
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        for (int i = 0; i < btns.size(); i++) KelpIcons.draw(ctx, btns.get(i), names.get(i));
        ctx.drawCenteredTextWithShadow(textRenderer, "Kelp Client", width / 2, height / 2 - 104, 0xFF7CFC7C);
    }
}
