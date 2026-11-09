package com.kelpclient.ui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Loads another mod's own icon (from its fabric.mod.json) as a GUI texture. */
public final class ModIcons {
	public static final class Icon {
		public final Identifier id; public final int w, h;
		Icon(Identifier id, int w, int h) { this.id = id; this.w = w; this.h = h; }
	}

	private static final Map<String, Icon> CACHE = new HashMap<>();
	private static final Icon NONE = new Icon(null, 0, 0);

	public static boolean loaded(String modId) { return FabricLoader.getInstance().isModLoaded(modId); }

	/** The icon, or null if the mod has none. */
	public static Icon get(String modId) {
		Icon i = CACHE.get(modId);
		if (i == null) {
			i = load(modId);
			CACHE.put(modId, i);
		}
		return i == NONE ? null : i;
	}

	private static Icon load(String modId) {
		try {
			Optional<ModContainer> mc = FabricLoader.getInstance().getModContainer(modId);
			if (mc.isEmpty()) return NONE;
			Optional<String> p = mc.get().getMetadata().getIconPath(64);
			if (p.isEmpty()) return NONE;
			Optional<Path> path = mc.get().findPath(p.get());
			if (path.isEmpty()) return NONE;
			try (InputStream in = Files.newInputStream(path.get())) {
				NativeImage img = NativeImage.read(in);
				int w = img.getWidth(), h = img.getHeight();
				NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "kelp_modicon_" + modId, img);
				tex.upload();
				Identifier id = Identifier.of("kelpclient", "modicon/" + modId.toLowerCase());
				MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
				return new Icon(id, w, h);
			}
		} catch (Throwable t) {
			return NONE;
		}
	}

	private ModIcons() { }
}
