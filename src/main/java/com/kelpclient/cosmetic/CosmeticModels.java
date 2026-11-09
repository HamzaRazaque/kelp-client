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
			int body = Colors.shift(0xFFF4B3CC, v), belly = Colors.shift(0xFFFFDDE9, v), gill = Colors.shift(0xFFE0568A, v),
					gillTip = Colors.shift(0xFFC73A74, v), fin = Colors.shift(0xFFF9C6DA, v), dark = 0xFF2A1030;
			VoxelModel m = new VoxelModel();
			// flat, wide head and long body, lying the way a real axolotl does
			m.box(-3f, -2.2f, -4.6f, 3f, 0f, -0.4f, body);
			m.box(-2.6f, -2.5f, -4.2f, 2.6f, -2.2f, -0.8f, body);
			m.box(-2.5f, -2f, -0.5f, 2.5f, 0f, 5.2f, body);
			m.box(-1.8f, -0.5f, -0.5f, 1.8f, 0f, 5.2f, belly);
			// wide smiling mouth and little eyes on top of the head
			m.box(-2.2f, -0.9f, -4.7f, 2.2f, -0.6f, -4.55f, 0xFFD9809F);
			m.box(-2.4f, -1.1f, -4.7f, -2.2f, -0.8f, -4.55f, 0xFFD9809F);
			m.box(2.2f, -1.1f, -4.7f, 2.4f, -0.8f, -4.55f, 0xFFD9809F);
			m.box(-2.3f, -2.9f, -3.6f, -1.5f, -2.4f, -2.8f, dark);
			m.box(1.5f, -2.9f, -3.6f, 2.3f, -2.4f, -2.8f, dark);
			m.box(-2.0f, -2.95f, -3.5f, -1.7f, -2.85f, -3.2f, 0xFFFFFFFF);
			m.box(1.7f, -2.95f, -3.5f, 2.0f, -2.85f, -3.2f, 0xFFFFFFFF);
			// feathery external gills: three stalks per side, each with two filaments
			for (int i = 0; i < 3; i++) {
				float y = -2.1f - i * 0.15f, z = -2.4f + i * 0.9f, lift = i * 0.7f;
				for (int sd = -1; sd <= 1; sd += 2) {
					float x0 = sd * 3f, x1 = sd * (5f - i * 0.2f);
					m.box(Math.min(x0, x1), y - lift - 0.5f, z, Math.max(x0, x1), y - lift + 0.1f, z + 0.6f, gill);
					float f0 = x1, f1 = x1 + sd * 1.3f;
					m.box(Math.min(f0, f1), y - lift - 1.4f, z, Math.max(f0, f1), y - lift - 0.8f, z + 0.5f, gillTip);
					m.box(Math.min(f0, f1), y - lift + 0.1f, z, Math.max(f0, f1), y - lift + 0.7f, z + 0.5f, gillTip);
				}
			}
			// tail with a dorsal fin, tiny legs
			m.box(-0.8f, -1.8f, 5.2f, 0.8f, 0f, 7.4f, body);
			m.box(-0.5f, -1.3f, 7.4f, 0.5f, 0f, 10f, body);
			m.box(-0.15f, -3.0f, 0.6f, 0.15f, -2.0f, 7.4f, fin);
			m.box(-0.15f, -2.2f, 7.4f, 0.15f, -1.2f, 10f, fin);
			for (int i = 0; i < 4; i++) {
				float x = i % 2 == 0 ? -3.4f : 2.4f, z = i < 2 ? -2.2f : 3.2f;
				m.box(x, -0.2f, z, x + 1f, 0.8f, z + 1.3f, body);
			}
			return m;
		});
	}

	// ---- red panda: body (with head), leg and tail are separate so they can animate ----
	static VoxelModel pandaBody() {
		return get("panda", () -> {
			int rust = 0xFFC4501C, rust2 = 0xFFDD6A2A, cream = 0xFFF6E9D2, dark = 0xFF2A1812, dk2 = 0xFF4A2414;
			VoxelModel m = new VoxelModel();
			m.cube(0, -2.6f, 0f, 5.2f, 3.6f, 9f, rust);
			m.cube(0, -6.1f, 0f, 4.4f, 0.8f, 7.5f, rust2);
			m.cube(0, -0.2f, 0f, 4.2f, 1.2f, 7.6f, dk2);
			// head: round, white-cheeked, with tear marks
			m.cube(0, -2.4f, -5.7f, 5.6f, 4.6f, 4.4f, rust2);
			m.cube(0, -2.4f, -8f, 3.2f, 2.4f, 1.2f, cream);
			m.cube(0, -3.7f, -8.7f, 1.3f, 1f, 0.5f, dark);
			m.cube(-2.9f, -2.3f, -6.4f, 0.9f, 2f, 2.6f, cream);
			m.cube(2.9f, -2.3f, -6.4f, 0.9f, 2f, 2.6f, cream);
			m.cube(0, -5.9f, -6.9f, 3.6f, 0.9f, 1.2f, cream);
			m.cube(-1.8f, -4.3f, -7.9f, 1f, 1f, 0.5f, dark);
			m.cube(1.8f, -4.3f, -7.9f, 1f, 1f, 0.5f, dark);
			m.cube(-1.8f, -2.9f, -8.1f, 0.4f, 1.5f, 0.4f, dk2);
			m.cube(1.8f, -2.9f, -8.1f, 0.4f, 1.5f, 0.4f, dk2);
			m.cube(-2.4f, -7.1f, -5f, 2f, 2f, 1.3f, rust);
			m.cube(2.4f, -7.1f, -5f, 2f, 2f, 1.3f, rust);
			m.cube(-2.4f, -7.0f, -4.3f, 1.2f, 1.3f, 0.5f, cream);
			m.cube(2.4f, -7.0f, -4.3f, 1.2f, 1.3f, 0.5f, cream);
			return m;
		});
	}

	static VoxelModel pandaLeg() { return get("pleg", () -> new VoxelModel().cube(0, 0, 0, 2f, 3f, 2f, 0xFF2A1812).cube(0, -2.6f, 0, 2.2f, 0.4f, 2.2f, 0xFFC4501C)); }

	static VoxelModel pandaTail() {
		return get("ptail", () -> {
			VoxelModel m = new VoxelModel();
			for (int i = 0; i < 9; i++) {
				float w = 3.6f - Math.abs(i - 4) * 0.12f + (i > 6 ? -0.5f * (i - 6) : 0f);
				m.box(-w / 2, -w / 2, i * 1.6f, w / 2, w / 2, (i + 1) * 1.6f, i % 2 == 0 ? 0xFFC4501C : (i == 8 ? 0xFF2A1812 : 0xFF8F3A14));
			}
			return m;
		});
	}

	/** The player's own cape.png, turned into voxels (reloads when the file changes). */
	static VoxelModel customCape() {
		CustomAssets.CapeArt a = CustomAssets.cape();
		if (a == null) return cape();
		return get("ccape" + a.stamp, () -> new VoxelModel().grid(a.rows, a.pal, 5f, 0f, 0f, 0.5f, 0.6f, -1, 0));
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
