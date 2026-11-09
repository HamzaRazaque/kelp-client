package com.kelpclient.ui;

import org.lwjgl.glfw.GLFW;

/** Short readable names for GLFW key codes. */
public final class KeyNames {
	public static String of(int key) {
		if (key < 0) return "None";
		if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F25) return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
		switch (key) {
			case GLFW.GLFW_KEY_SPACE: return "Space";
			case GLFW.GLFW_KEY_TAB: return "Tab";
			case GLFW.GLFW_KEY_ENTER: return "Enter";
			case GLFW.GLFW_KEY_LEFT_SHIFT: return "L-Shift";
			case GLFW.GLFW_KEY_RIGHT_SHIFT: return "R-Shift";
			case GLFW.GLFW_KEY_LEFT_CONTROL: return "L-Ctrl";
			case GLFW.GLFW_KEY_RIGHT_CONTROL: return "R-Ctrl";
			case GLFW.GLFW_KEY_LEFT_ALT: return "L-Alt";
			case GLFW.GLFW_KEY_RIGHT_ALT: return "R-Alt";
			case GLFW.GLFW_KEY_UP: return "Up";
			case GLFW.GLFW_KEY_DOWN: return "Down";
			case GLFW.GLFW_KEY_LEFT: return "Left";
			case GLFW.GLFW_KEY_RIGHT: return "Right";
			case GLFW.GLFW_KEY_INSERT: return "Insert";
			case GLFW.GLFW_KEY_HOME: return "Home";
			case GLFW.GLFW_KEY_END: return "End";
			case GLFW.GLFW_KEY_PAGE_UP: return "PgUp";
			case GLFW.GLFW_KEY_PAGE_DOWN: return "PgDn";
			default:
		}
		String n = GLFW.glfwGetKeyName(key, 0);
		return n != null ? n.toUpperCase() : "Key " + key;
	}
	private KeyNames() { }
}
