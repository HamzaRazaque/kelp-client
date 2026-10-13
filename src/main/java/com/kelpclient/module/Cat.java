package com.kelpclient.module;

public enum Cat {
	HUD("HUD", "monitor", "Move HUD modules with the Right Shift menu.\nRight-click a module there to switch it off."),
	GAMEPLAY("Gameplay", "sword", "Move either module with the Right Shift menu.\nRight-click a module there to switch it off."),
	CROSSHAIR("Crosshair", "cross", "Replaces the vanilla crosshair.\nUse the gear to change style, size, gap and colour."),
	WAYPOINTS("Waypoints", "pin", "Add waypoints from the pin icon in the Right Shift menu.\nThey show on screen with their distance."),
	SOCIAL("Social", "kelp", "Add friends from the person icon in the Right Shift menu.\nFriends online show on your HUD.");

	public final String label, icon, hint;
	Cat(String label, String icon, String hint) { this.label = label; this.icon = icon; this.hint = hint; }
}
