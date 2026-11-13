package com.kelpclient.cosmetic;

import com.kelpclient.module.Module;
import com.kelpclient.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/** Real previews of the 3D cosmetics for the Wardrobe cards: each model is software-rendered once into a small texture. */
public final class ModelPreview {
	private static final int SIZE = 62;
	private static final Map<String, Identifier> CACHE = new HashMap<>();
	private static int counter;

	/** Returns false if this cosmetic has no 3D model (particle effects), so the caller can draw an icon instead. */
	public static boolean draw(DrawContext c, Module m, int x, int y, int w, int h) {
		int v = 0;
		for (Setting s : m.settings) if (s.key.equals("color")) v = s.v;
		String stamp = "";
		if (m.id.equals("customcape")) {
			CustomAssets.CapeArt a = CustomAssets.cape();
			if (a == null) return false;
			stamp = "" + a.stamp;
		}
		String key = m.id + "|" + v + "|" + stamp;
		Identifier id = CACHE.get(key);
		if (id == null) {
			VoxelModel model = build(m.id, v);
			if (model == null) return false;
			float yaw = 0.7f, pitch = 0.3f;
			if (m.id.startsWith("wings") || m.id.equals("sword") || m.id.equals("customcape")) { yaw = (float) Math.PI + 0.35f; pitch = 0.15f; }
			if (m.id.equals("sword")) { yaw = (float) Math.PI; pitch = 0f; }
			int[] argb = model.raster(SIZE, yaw, pitch);
			NativeImage img = new NativeImage(NativeImage.Format.RGBA, SIZE, SIZE, false);
			for (int i = 0; i < SIZE; i++) for (int j = 0; j < SIZE; j++) img.setColorArgb(j, i, argb[i * SIZE + j]);
			NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "kelp_preview", img);
			tex.upload();
			id = Identifier.of("kelpclient", "preview/p" + (counter++));
			MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
			CACHE.put(key, id);
		}
		c.drawTexture(RenderPipelines.GUI_TEXTURED, id, x + (w - SIZE) / 2, y + (h - SIZE) / 2, 0f, 0f, SIZE, SIZE, SIZE, SIZE, SIZE, SIZE);
		return true;
	}

	private static VoxelModel build(String id, int v) {
		switch (id) {
			case "tophat": return CosmeticModels.hat();
			case "wizardhat": return CosmeticModels.wizardHat();
			case "crown": return CosmeticModels.crown();
			case "catears": return CosmeticModels.catEars();
			case "sword": return CosmeticModels.sword(v);
			case "customcape": return CosmeticModels.customCape();
			case "axolotl": {
				VoxelModel m = new VoxelModel().merge(CosmeticModels.axolotl(v), 0, 0, 0, 0, 0);
				m.merge(CosmeticModels.axolotlGills(v, -1), 0, 0, 0, 0, 0).merge(CosmeticModels.axolotlGills(v, 1), 0, 0, 0, 0, 0);
				return m.merge(CosmeticModels.axolotlTail(v), 0, 0, 0, 0, 0);
			}
			case "redpanda": {
				VoxelModel m = new VoxelModel().merge(CosmeticModels.pandaBody(), 0, 0, 0, 0, 0);
				for (int i = 0; i < 4; i++) m.merge(CosmeticModels.pandaLeg(), 0, 0, (i % 2 == 0 ? -1.8f : 1.8f), -3f, (i < 2 ? -3.2f : 2.6f));
				return m.merge(CosmeticModels.pandaTail(), 0, 0, 0, -3.2f, 4.4f);
			}
			default:
		}
		int style = -1;
		switch (id) {
			case "wings": style = 0; break;
			case "wings_angel": style = 1; break;
			case "wings_butterfly": style = 2; break;
			case "wings_raven": style = 3; break;
			case "wings_crystal": style = 4; break;
			default:
		}
		if (style < 0) return null;
		VoxelModel m = new VoxelModel();
		m.merge(CosmeticModels.wing(style, v, true), 0.5f, 0.25f, -1.5f, 1.5f, 0);
		m.merge(CosmeticModels.wing(style, v, false), -0.5f, -0.25f, 1.5f, 1.5f, 0);
		return m;
	}

	private ModelPreview() { }
}
