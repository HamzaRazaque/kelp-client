package com.kelpclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

/** Kelp Tag: shows the kelp icon in front of your own name (name tag and tab list) when enabled. */
public final class KelpTag {
    private KelpTag() {}

    private static final Text ICON = Text.literal("\uE000")
            .setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("kelpclient", "icons"))));

    public static Text prefix(Text name) { return Text.empty().append(ICON).append(Text.literal(" ")).append(name); }

    public static boolean isKelp(UUID id) {
        if (!KelpConfig.get().kelpTag || id == null) return false;
        var mc = MinecraftClient.getInstance();
        return mc.player != null && id.equals(mc.player.getUuid());
    }
}
