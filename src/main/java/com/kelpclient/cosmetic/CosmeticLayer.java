package com.kelpclient.cosmetic;

import com.kelpclient.module.Module;
import com.kelpclient.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

/** Draws Kelp's 3D cosmetics (top hat, axolotl, red panda, sword, dragon wings, cape) on your own player. */
public final class CosmeticLayer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	private static Identifier white;

	public CosmeticLayer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> ctx) { super(ctx); }

	private static RenderLayer layer() {
		if (white == null) {
			NativeImage img = new NativeImage(NativeImage.Format.RGBA, 2, 2, false);
			for (int y = 0; y < 2; y++) for (int x = 0; x < 2; x++) img.setColorArgb(x, y, 0xFFFFFFFF);
			NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "kelp_white", img);
			tex.upload();
			Identifier id = Identifier.of("kelpclient", "white");
			MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
			white = id;
		}
		return RenderLayers.entityCutoutNoCull(white);
	}

	private static boolean on(String id) { Module m = Modules.BY_ID.get(id); return m != null && m.enabled; }
	private static int opt(String id) { Module m = Modules.BY_ID.get(id); return m == null ? 0 : m.get("color"); }

	private static void draw(MatrixStack m, OrderedRenderCommandQueue q, VoxelModel model, int light) {
		q.getBatchingQueue(0).submitCustom(m, layer(), (entry, vc) -> model.emit(entry, vc, light));
	}

	@Override
	public void render(MatrixStack m, OrderedRenderCommandQueue q, int light, PlayerEntityRenderState s, float limbAngle, float limbDistance) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || s.id != mc.player.getId() || s.invisible || s.spectator) return;
		PlayerEntityModel model = getContextModel();
		float age = s.age;
		boolean sneak = s.isInSneakingPose;
		boolean hat = on("tophat");

		if (hat) {
			m.push();
			model.head.applyTransform(m);
			draw(m, q, CosmeticModels.hat(), light);
			m.pop();
		}
		if (on("axolotl")) {
			m.push();
			model.head.applyTransform(m);
			m.translate(0f, ((hat ? -17.2f : -8.7f) + (float) Math.sin(age * 0.12f) * 0.2f) / 16f, -0.2f / 16f);
			draw(m, q, CosmeticModels.axolotl(opt("axolotl")), light);
			m.pop();
		}
		if (on("redpanda")) {
			m.push();
			m.translate(14f / 16f, 24f / 16f, -3f / 16f);
			m.scale(1.2f, 1.2f, 1.2f);
			float bob = (float) Math.sin(age * 0.1f) * 0.2f;
			m.translate(0f, bob / 16f, 0f);
			draw(m, q, CosmeticModels.pandaBody(), light);
			for (int i = 0; i < 4; i++) {
				float lx = (i % 2 == 0 ? -1.8f : 1.8f), lz = (i < 2 ? -3.2f : 2.6f);
				float swing = (float) Math.sin(limbAngle * 0.6662f + (i % 2 == 0 ? 0 : Math.PI)) * Math.min(1f, limbDistance) * 1.8f;
				m.push();
				m.translate(lx / 16f, 0f, (lz + swing) / 16f);
				draw(m, q, CosmeticModels.pandaLeg(), light);
				m.pop();
			}
			m.push();
			m.translate(0f, -3.2f / 16f, 4.4f / 16f);
			m.multiply(new Quaternionf().rotateY((float) Math.sin(age * 0.15f) * 0.3f).rotateX(-0.45f));
			draw(m, q, CosmeticModels.pandaTail(), light);
			m.pop();
			m.pop();
		}
		if (on("cape")) {
			m.push();
			model.body.applyTransform(m);
			m.translate(0f, 0.4f / 16f, 3.4f / 16f);
			float b = 0.06f + Math.min(1f, limbDistance) * 0.45f + (sneak ? 0.22f : 0f);
			m.multiply(new Quaternionf().rotateX(b));
			draw(m, q, CosmeticModels.cape(), light);
			m.pop();
		}
		if (on("customcape")) {
			m.push();
			model.body.applyTransform(m);
			m.translate(0f, 0.4f / 16f, 3.4f / 16f);
			m.multiply(new Quaternionf().rotateX(0.06f + Math.min(1f, limbDistance) * 0.45f + (sneak ? 0.22f : 0f)));
			draw(m, q, CosmeticModels.customCape(), light);
			m.pop();
		}
		if (on("sword")) {
			m.push();
			model.body.applyTransform(m);
			draw(m, q, CosmeticModels.sword(opt("sword")), light);
			m.pop();
		}
		if (on("wings")) {
			int v = opt("wings");
			float a = 0.5f + (float) Math.sin(age * 0.12f) * 0.08f + Math.min(1f, limbDistance) * (float) Math.sin(age * 0.5f) * 0.15f + (sneak ? 0.15f : 0f);
			for (int side = -1; side <= 1; side += 2) {
				m.push();
				model.body.applyTransform(m);
				m.translate(side * 1.5f / 16f, 1.5f / 16f, 3.6f / 16f);
				m.multiply(new Quaternionf().rotateY(-side * a).rotateZ(-side * 0.25f));
				draw(m, q, CosmeticModels.wing(v, side < 0), light);
				m.pop();
			}
		}
	}
}
