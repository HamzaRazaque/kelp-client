package com.kelpclient.module;

public final class Setting {
	public final String key, label;
	public final String[] opts;
	public final int min, max, step, def;
	public final boolean cycle;
	public int v;

	private Setting(String key, String label, boolean cycle, int def, int min, int max, int step, String[] opts) {
		this.key = key; this.label = label; this.cycle = cycle; this.def = def; this.v = def;
		this.min = min; this.max = max; this.step = step; this.opts = opts;
	}

	public static Setting cycle(String key, String label, int def, String... opts) {
		return new Setting(key, label, true, def, 0, opts.length - 1, 1, opts);
	}

	public static Setting range(String key, String label, int def, int min, int max, int step) {
		return new Setting(key, label, false, def, min, max, step, null);
	}

	public String display() { return cycle ? opts[Math.max(0, Math.min(v, opts.length - 1))] : String.valueOf(v); }

	public void next() {
		if (cycle) v = (v + 1) % opts.length;
		else { v += step; if (v > max) v = min; }
	}

	public void clamp() {
		if (cycle) { if (v < 0 || v >= opts.length) v = def; }
		else v = Math.max(min, Math.min(max, v));
	}
}
