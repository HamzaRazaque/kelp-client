package com.kelpclient;

import com.kelpclient.mixin.MinecraftClientAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/** Offline (username-only) account switcher. Real Microsoft accounts need a browser OAuth login and are not handled here. */
public class KelpAccountScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget field;

    public KelpAccountScreen(Screen parent) { super(Text.literal("Accounts")); this.parent = parent; }

    private void switchTo(String name) {
        name = name.trim();
        if (name.isEmpty() || name.length() > 16) return;
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        ((MinecraftClientAccessor) client).kelp$setSession(new Session(name, id, "", Optional.empty(), Optional.empty()));
        var c = KelpConfig.get();
        if (!c.accounts.contains(name)) c.accounts.add(name);
        KelpConfig.save();
        client.setScreen(parent instanceof KelpTitleScreen ? new KelpTitleScreen() : parent);
    }

    @Override
    protected void init() {
        field = new TextFieldWidget(textRenderer, width / 2 - 100, 50, 200, 20, Text.literal("Username"));
        field.setMaxLength(16);
        field.setPlaceholder(Text.literal("Username"));
        addDrawableChild(field);
        addDrawableChild(ButtonWidget.builder(Text.literal("Add & Switch"), b -> switchTo(field.getText())).dimensions(width / 2 - 100, 74, 200, 20).build());
        int y = 106;
        for (String n : KelpConfig.get().accounts) {
            if (y > height - 60) break;
            final String name = n;
            addDrawableChild(ButtonWidget.builder(Text.literal(name), b -> switchTo(name)).dimensions(width / 2 - 100, y, 170, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> { KelpConfig.get().accounts.remove(name); KelpConfig.save(); clearAndInit(); })
                    .dimensions(width / 2 + 74, y, 26, 20).build());
            y += 24;
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close()).dimensions(width / 2 - 100, height - 28, 200, 20).build());
    }

    @Override public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        ctx.drawCenteredTextWithShadow(textRenderer, "Current: " + client.getSession().getUsername(), width / 2, 20, 0xFF7CFC7C);
        ctx.drawCenteredTextWithShadow(textRenderer, "Offline accounts (cannot join online-mode servers)", width / 2, 34, 0xFFAAAAAA);
    }
}
