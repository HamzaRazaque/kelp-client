package com.kelpclient.module;

public enum Cat {
	HUD("HUD", "monitor", "Move HUD modules with the Right Shift menu.\nRight-click a module there to switch it off."),
	GAMEPLAY("Gameplay", "gamepad", "Move either module with the Right Shift menu.\nRight-click a module there to switch it off."),
	VISUAL("Visual", "sun", "Client-side visuals: sky, fog, weather, time, FOV and more.\nThese only change what you see, never the server."),
	CROSSHAIR("Crosshair", "cross", "Replaces the vanilla crosshair.\nUse the gear to change style, size, gap and colour."),
	WAYPOINTS("Waypoints", "pin", "Add waypoints from the pin icon in the Right Shift menu.\nThey show on screen with their distance."),
	COSMETICS("Cosmetics", "sparkle", "3D cosmetics (wings, sword, cape, hat, pets) and particle effects.\nSwitch them on, then look at yourself in third person (F5).\nThey are client-side, so only you see them."),
	SOCIAL("Social", "users", "Add friends from the person icon in the Right Shift menu.\nFriends online show on your HUD.");

	public final String label, icon, hint;
	Cat(String label, String icon, String hint) { this.label = label; this.icon = icon; this.hint = hint; }
}
