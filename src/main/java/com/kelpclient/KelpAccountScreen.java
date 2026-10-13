package com.kelpclient;

import com.kelpclient.mixin.MinecraftClientAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Offline (username-only) account switcher. Real Microsoft accounts need a browser OAuth login and are not handled here. */
public class KelpAccountScreen extends Screen {
    private final Screen parent;
    private final List<Consumer<DrawContext>> overlays = new ArrayList<>();
    private TextFieldWidget field;
    private int page = 0, px, py, pw, ph, msgColor = KelpUi.DIM;
    private String msg = "";

    public KelpAccountScreen(Screen parent) { super(Text.literal("Accounts")); this.parent = parent; }

    private void switchTo(String raw) {
        String name = raw.trim();
        if (!name.matches("[A-Za-z0-9_]{3,16}")) { msg = "Names are 3-16 letters, numbers or _"; msgColor = KelpUi.RED; return; }
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        ((MinecraftClientAccessor) client).kelp$setSession(new Session(name, id, "", Optional.empty(), Optional.empty()));
        var c = KelpConfig.get();
        if (!c.accounts.contains(name)) c.accounts.add(name);
        KelpConfig.save();
        msg = "Switched to " + name; msgColor = KelpUi.ACCENT;
        clearAndInit();
    }

    @Override
    protected void init() {
        overlays.clear();
        pw = Math.min(340, width - 16); ph = Math.min(310, height - 16);
        px = (width - pw) / 2; py = (height - ph) / 2;
        int x = px + 12, w = pw - 24;

        overlays.add(c -> {
            KelpUi.rect(c, x, py + 26, w, 38, KelpUi.CARD, KelpUi.ACCENT);
            c.fill(x + 4, py + 30, x + 34, py + 60, 0x66000000);
            KelpIcons.at(c, "account", x + 11, py + 37);
            c.drawTextWithShadow(textRenderer, client.getSession().getUsername(), x + 42, py + 33, KelpUi.TEXT);
            c.drawTextWithShadow(textRenderer, "Active | offline account", x + 42, py + 46, KelpUi.ACCENT);
        });

        field = new TextFieldWidget(textRenderer, x, py + 72, w - 66, 22, Text.literal("Username"));
        field.setMaxLength(16);
        field.setPlaceholder(Text.literal("Enter a username"));
        addDrawableChild(field);
        ButtonWidget add = addDrawableChild(ButtonWidget.builder(Text.literal("Add"), b -> switchTo(field.getText()))
                .dimensions(x + w - 62, py + 72, 62, 22).build());
        overlays.add(c -> KelpUi.card(c, textRenderer, add, "plus", true));
        overlays.add(c -> { if (!msg.isEmpty()) c.drawTextWithShadow(textRenderer, msg, x, py + 99, msgColor); });
        overlays.add(c -> c.drawTextWithShadow(textRenderer, "Saved accounts", x, py + 113, KelpUi.DIM));

        var accs = KelpConfig.get().accounts;
        int listY = py + 126, rowH = 28;
        int per = Math.max(1, (ph - 126 - 36) / (rowH + 4));
        int pages = Math.max(1, (accs.size() + per - 1) / per);
        page = Math.min(page, pages - 1);
        for (int i = 0; i < per; i++) {
            int idx = page * per + i;
            if (idx >= accs.size()) break;
            String name = accs.get(idx);
            int y = listY + i * (rowH + 4);
            ButtonWidget sel = addDrawableChild(ButtonWidget.builder(Text.literal(name), b -> switchTo(name)).dimensions(x, y, w - 26, rowH).build());
            ButtonWidget del = addDrawableChild(ButtonWidget.builder(Text.literal("x"), b -> { accs.remove(name); KelpConfig.save(); clearAndInit(); })
                    .dimensions(x + w - 22, y, 22, rowH).build());
            overlays.add(c -> {
                boolean act = name.equals(client.getSession().getUsername()), hov = sel.isHovered();
                KelpUi.rect(c, x, y, w - 26, rowH, hov ? KelpUi.CARD_HOVER : KelpUi.CARD, act || hov ? KelpUi.ACCENT : KelpUi.BORDER);
                KelpIcons.at(c, "account", x + 6, y + (rowH - 16) / 2);
                c.drawTextWithShadow(textRenderer, name, x + 28, y + 5, KelpUi.TEXT);
                c.drawTextWithShadow(textRenderer, act ? "Active" : "Click to switch", x + 28, y + 16, act ? KelpUi.ACCENT : KelpUi.DIM);
                boolean dh = del.isHovered();
                KelpUi.rect(c, x + w - 22, y, 22, rowH, dh ? 0xFF5A1F1F : 0xFF3A1717, dh ? KelpUi.RED : 0xFF5C2A2A);
                KelpIcons.at(c, "trash", x + w - 19, y + (rowH - 16) / 2);
            });
        }
        if (accs.isEmpty()) overlays.add(c -> c.drawTextWithShadow(textRenderer, "No saved accounts yet. Add one above.", x, listY + 6, KelpUi.DIM));

        int fy = py + ph - 28;
        ButtonWidget prev = addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> { page = Math.max(0, page - 1); clearAndInit(); }).dimensions(x, fy, 22, 22).build());
        ButtonWidget next = addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> { page++; clearAndInit(); }).dimensions(x + 26, fy, 22, 22).build());
        final int pg = page, total = pages;
        overlays.add(c -> {
            KelpUi.card(c, textRenderer, prev, null, false);
            KelpUi.card(c, textRenderer, next, null, false);
            c.drawTextWithShadow(textRenderer, (pg + 1) + "/" + total, x + 54, fy + 7, KelpUi.DIM);
        });
        ButtonWidget back = addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close()).dimensions(x + w - 84, fy, 84, 22).build());
        overlays.add(c -> KelpUi.card(c, textRenderer, back, null, true));
    }

    @Override
    public void close() { client.setScreen(parent instanceof KelpTitleScreen ? new KelpTitleScreen() : parent); }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float d) {
        KelpUi.background(ctx, width, height, client.world != null);
        KelpUi.panel(ctx, px, py, pw, ph, KelpUi.PANEL, KelpUi.BORDER);
        ctx.drawTextWithShadow(textRenderer, "ACCOUNTS", px + 12, py + 10, KelpUi.ACCENT);
        ctx.drawTextWithShadow(textRenderer, "Offline only", px + pw - 12 - textRenderer.getWidth("Offline only"), py + 10, KelpUi.DIM);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float d) {
        super.render(ctx, mx, my, d);
        for (var o : overlays) o.accept(ctx);
    }
}
