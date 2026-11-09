package com.kelpclient.cosmetic;

import java.util.HashMap;
import java.util.Map;

/** Bakes every 3D cosmetic once (per colour variant). Units are model pixels; up is -y, the player's back is +z. */
final class CosmeticModels {
	private static final Map<String, VoxelModel> CACHE = new HashMap<>();

	private interface Maker { VoxelModel make(); }

	private static VoxelModel get(String key, Maker m) { return CACHE.computeIfAbsent(key, k -> m.make()); }

	static VoxelModel hat() {
		return get("hat", () -> new VoxelModel()
				.box(-6.2f, -9.5f, -6.2f, 6.2f, -8.5f, 6.2f, 0xFF1B1F22)
				.box(-4.6f, -17f, -4.6f, 4.6f, -9.5f, 4.6f, 0xFF262B2F)
				.box(-4.8f, -11.8f, -4.8f, 4.8f, -10f, 4.8f, 0xFFB02A2E)
				.box(-4.6f, -17.2f, -4.6f, 4.6f, -17f, 4.6f, 0xFF30363B));
	}

	static VoxelModel axolotl(int v) {
		return get("axo" + v, () -> {
			int body = Colors.shift(0xFFF2A5C4, v), belly = Colors.shift(0xFFFFD3E4, v), gill = Colors.shift(0xFFE0568A, v), eye = 0xFF2A1030;
			VoxelModel m = new VoxelModel()
					.cube(0, 0, 0.8f, 5f, 2.6f, 6f, body)
					.cube(0, -0.1f, 0.8f, 3.6f, 0.4f, 4.4f, belly)
					.cube(0, 0.4f, -3.4f, 5f, 3.6f, 4f, body)
					.cube(0, -2.6f, 6.2f, 1.2f, 2f, 5f, body)
					.box(-1.9f, -2.3f, -5.55f, -0.9f, -1.3f, -5.3f, eye)
					.box(0.9f, -2.3f, -5.55f, 1.9f, -1.3f, -5.3f, eye)
					.box(-1f, -0.7f, -5.5f, 1f, -0.4f, -5.3f, Colors.shift(0xFFC9779B, v));
			for (int i = 0; i < 3; i++) {
				float y = -3.1f + i * 1.15f;
				m.box(2.5f, y, -4.6f, 4.9f - i * 0.2f, y + 0.8f, -3.7f, gill);
				m.box(-4.9f + i * 0.2f, y, -4.6f, -2.5f, y + 0.8f, -3.7f, gill);
			}
			for (int i = 0; i < 4; i++) m.box((i % 2 == 0 ? -2.9f : 2.0f), 0f, (i < 2 ? -1.8f : 2.6f), (i % 2 == 0 ? -2.0f : 2.9f), 0.9f, (i < 2 ? -0.9f : 3.5f), belly);
			return m;
		});
	}

	// ---- red panda: body (with head), leg and tail are separate so they can animate ----
	static VoxelModel pandaBody() {
		return get("panda", () -> {
			int o = 0xFFC9511F, o2 = 0xFFE0702E, cream = 0xFFF4E2BC, dark = 0xFF2B1B14;
			return new VoxelModel()
					.cube(0, -3f, 0f, 5f, 4f, 8f, o)
					.cube(0, -7f, 0f, 4f, 0.6f, 6f, o2)
					.cube(0, -2.5f, -5.4f, 5f, 4.5f, 4f, o)
					.cube(0, -2.4f, -7.9f, 2.6f, 2f, 1.8f, cream)
					.cube(0, -3.2f, -8.9f, 1f, 0.9f, 0.4f, dark)
					.cube(-2.7f, -2.2f, -6.1f, 0.8f, 1.6f, 2.4f, cream)
					.cube(2.7f, -2.2f, -6.1f, 0.8f, 1.6f, 2.4f, cream)
					.cube(-1.7f, -4.4f, -7.65f, 0.9f, 0.9f, 0.5f, dark)
					.cube(1.7f, -4.4f, -7.65f, 0.9f, 0.9f, 0.5f, dark)
					.cube(-2f, -6.9f, -5f, 1.8f, 1.8f, 1.2f, o)
					.cube(2f, -6.9f, -5f, 1.8f, 1.8f, 1.2f, o)
					.cube(-2f, -6.9f, -4.3f, 1.0f, 1.0f, 0.5f, cream)
					.cube(2f, -6.9f, -4.3f, 1.0f, 1.0f, 0.5f, cream);
		});
	}

	static VoxelModel pandaLeg() { return get("pleg", () -> new VoxelModel().cube(0, 0, 0, 2f, 3f, 2f, 0xFF2B1B14)); }

	static VoxelModel pandaTail() {
		return get("ptail", () -> {
			VoxelModel m = new VoxelModel();
			for (int i = 0; i < 6; i++) m.box(-1.6f, -1.6f, i * 1.7f, 1.6f, 1.6f, (i + 1) * 1.7f, i % 2 == 0 ? 0xFFC9511F : 0xFF7A2E12);
			return m;
		});
	}

	static VoxelModel sword(int v) {
		return get("sword" + v, () -> new VoxelModel().grid(CosmeticArt.SWORD, CosmeticArt.SWORD_PAL, 9f, -2f, 4.4f, 1.1f, 1.2f, -1, v));
	}

	static VoxelModel wing(int v, boolean right) {
		return get("wing" + v + right, () -> new VoxelModel().grid(CosmeticArt.WING, CosmeticArt.WING_PAL, 0f, -11f, 0f, 1.1f, 1f, right ? -1 : 1, v));
	}

	static VoxelModel cape() {
		return get("cape", () -> new VoxelModel().grid(CosmeticArt.CAPE, CosmeticArt.CAPE_PAL, 5f, 0f, 0f, 0.5f, 0.6f, -1, 0));
	}

	private CosmeticModels() { }
}
