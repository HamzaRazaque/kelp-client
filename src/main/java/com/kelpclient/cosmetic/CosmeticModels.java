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

	private static int[] axoCols(int v) {
		return new int[]{Colors.shift(0xFFF7B3CD, v), Colors.shift(0xFFFFDDE9, v), Colors.shift(0xFFE8669A, v), Colors.shift(0xFFD13F78, v), Colors.shift(0xFFF9C6DA, v)};
	}

	/** Head, body, legs and fin of the axolotl: pink, wide blocky head, dark side eyes, little smile. */
	static VoxelModel axolotl(int v) {
		return get("axo" + v, () -> {
			int[] k = axoCols(v);
			int body = k[0], belly = k[1], fin = k[4], dark = 0xFF2A1030, mouth = Colors.shift(0xFFD9709A, v);
			VoxelModel m = new VoxelModel();
			m.box(-3.4f, -3.4f, -4.6f, 3.4f, 0f, -0.2f, body);      // big square head
			m.box(-2.6f, -3.0f, -0.2f, 2.6f, 0f, 5.4f, body);        // body
			m.box(-2.0f, -0.6f, -0.2f, 2.0f, 0f, 5.4f, belly);
			m.box(-1.3f, -1.1f, -4.7f, 1.3f, -0.8f, -4.5f, mouth);   // small mouth
			m.box(-1.6f, -1.4f, -4.7f, -1.3f, -1.1f, -4.5f, mouth);
			m.box(1.3f, -1.4f, -4.7f, 1.6f, -1.1f, -4.5f, mouth);
			// eyes sit at the sides of the front of the head
			m.box(-2.7f, -2.5f, -4.7f, -1.7f, -1.3f, -4.5f, dark);
			m.box(1.7f, -2.5f, -4.7f, 2.7f, -1.3f, -4.5f, dark);
			m.box(-0.25f, -3.4f, -3f, 0.25f, -3.8f, 5.4f, fin);
			for (int i = 0; i < 4; i++) {
				float x = i % 2 == 0 ? -3.5f : 2.5f, z = i < 2 ? -2.0f : 3.4f;
				m.box(x, -0.2f, z, x + 1f, 0.8f, z + 1.3f, body);
			}
			return m;
		});
	}

	/** The red-pink gill fans on one side (sd = -1 or +1); they flutter in the layer. */
	static VoxelModel axolotlGills(int v, int sd) {
		return get("axog" + v + sd, () -> {
			VoxelModel m = new VoxelModel();
			java.util.Map<Character, Integer> pal = new java.util.HashMap<>();
			for (java.util.Map.Entry<Character, Integer> e : CosmeticArt.GILL_PAL.entrySet()) pal.put(e.getKey(), e.getValue());
			// two fans per side, one above the other, like the real thing
			m.grid(CosmeticArt.GILL, pal, sd * 3.3f, -6.2f, -2.6f, 0.75f, 0.8f, sd, v);
			m.grid(CosmeticArt.GILL, pal, sd * 3.3f, -3.4f, -1.5f, 0.75f, 0.8f, sd, v);
			return m;
		});
	}

	/** Tail (pivot is at z = 5.4). */
	static VoxelModel axolotlTail(int v) {
		return get("axot" + v, () -> {
			int[] k = axoCols(v);
			VoxelModel m = new VoxelModel();
			m.box(-1.0f, -2.4f, 5.4f, 1.0f, 0f, 7.8f, k[0]);
			m.box(-0.6f, -1.6f, 7.8f, 0.6f, 0f, 10.6f, k[0]);
			m.box(-0.25f, -3.8f, 5.4f, 0.25f, -2.4f, 7.8f, k[4]);
			m.box(-0.25f, -2.8f, 7.8f, 0.25f, -1.6f, 10.6f, k[4]);
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

	/** Hangs down from the hip (origin = hip joint) so it can swing. */
	static VoxelModel pandaLeg() { return get("pleg", () -> new VoxelModel().box(-1f, 0f, -1f, 1f, 3f, 1f, 0xFF2A1812).box(-1.1f, 0f, -1.1f, 1.1f, 1f, 1.1f, 0xFFC4501C)); }

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
		if (a == null) return null;
		return get("ccape" + a.stamp, () -> new VoxelModel().grid(a.rows, a.pal, 5f, 0f, 0f, 0.5f, 0.6f, -1, 0));
	}

	static VoxelModel sword(int v) {
		return get("sword" + v, () -> new VoxelModel().grid(CosmeticArt.SWORD, CosmeticArt.SWORD_PAL, 9f, -2f, 4.4f, 1.1f, 1.2f, -1, v));
	}

	/** style: 0 dragon, 1 angel, 2 butterfly, 3 raven, 4 crystal. */
	static VoxelModel wing(int style, int v, boolean right) {
		return get("wing" + style + "_" + v + right, () -> new VoxelModel().grid(CosmeticArt.WING_ART[style], CosmeticArt.WING_PALS.get(style), 0f, -11f, 0f, 1.1f, 1f, right ? -1 : 1, v));
	}

	// ---- more hats (origin: top of the head at y = -8.5) ----
	static VoxelModel crown() {
		return get("crown", () -> {
			int gold = 0xFFF2C230, dark = 0xFFB8860B;
			VoxelModel m = new VoxelModel().box(-4.5f, -10.4f, -4.5f, 4.5f, -8.5f, 4.5f, gold).box(-4.6f, -9.2f, -4.6f, 4.6f, -8.9f, 4.6f, dark);
			for (int i = -1; i <= 1; i++) for (int sd = -1; sd <= 1; sd += 2) {
				m.box(i * 3.2f - 0.7f, -12.6f, sd * 4f - 0.5f, i * 3.2f + 0.7f, -10.4f, sd * 4f + 0.5f, gold);
			}
			m.box(-4.5f, -12.4f, -0.7f, -3.6f, -10.4f, 0.7f, gold).box(3.6f, -12.4f, -0.7f, 4.5f, -10.4f, 0.7f, gold);
			m.box(-0.8f, -10.0f, -4.7f, 0.8f, -9.0f, -4.5f, 0xFFE5484D).box(-3.2f, -10.0f, -4.7f, -2.0f, -9.0f, -4.5f, 0xFF4AA3F0).box(2.0f, -10.0f, -4.7f, 3.2f, -9.0f, -4.5f, 0xFF3CD16B);
			return m;
		});
	}

	static VoxelModel wizardHat() {
		return get("wizard", () -> {
			int c1 = 0xFF4B2A86, c2 = 0xFF5E36A8;
			VoxelModel m = new VoxelModel().box(-6.2f, -9.4f, -6.2f, 6.2f, -8.5f, 6.2f, c1);
			m.box(-4.6f, -12.4f, -4.6f, 4.6f, -9.4f, 4.6f, c2).box(-4.7f, -10.6f, -4.7f, 4.7f, -9.6f, 4.7f, 0xFFF2C230);
			m.box(-3.6f, -15.2f, -3.2f, 3.6f, -12.4f, 4.0f, c1).box(-2.6f, -17.6f, -2.6f, 2.6f, -15.2f, 4.6f, c2).box(-1.6f, -19.6f, -1.8f, 1.6f, -17.6f, 5.2f, c1).box(-0.8f, -21.2f, -0.8f, 0.8f, -19.6f, 6.0f, c2);
			m.box(-1f, -13.6f, -3.3f, 1f, -12.6f, -3.1f, 0xFFFFE680).box(1.6f, -16.0f, -2.7f, 2.6f, -15.2f, -2.5f, 0xFFFFE680);
			return m;
		});
	}

	static VoxelModel catEars() {
		return get("catears", () -> {
			int fur = 0xFF2B2B33, in = 0xFFF7A8C8;
			VoxelModel m = new VoxelModel().box(-4.3f, -9.0f, -4.4f, 4.3f, -8.5f, -3.4f, fur);
			for (int sd = -1; sd <= 1; sd += 2) {
				float x = sd * 2.8f;
				m.box(x - 1.6f, -10.0f, -4.6f, x + 1.6f, -8.5f, -3.2f, fur).box(x - 1.1f, -11.4f, -4.4f, x + 1.1f, -10.0f, -3.4f, fur).box(x - 0.5f, -12.4f, -4.2f, x + 0.5f, -11.4f, -3.6f, fur);
				m.box(x - 0.7f, -10.0f, -4.7f, x + 0.7f, -8.9f, -4.6f, in);
			}
			return m;
		});
	}

	private CosmeticModels() { }
}
