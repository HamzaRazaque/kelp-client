# Kelp Client (Fabric, Minecraft 1.21.11)

Open the menu with **Right Shift** in a world.

## Menus
- **Right Shift menu** – Kelp icon bar on top (Mod Menu, Kelp Mods, Waypoints, Social, Import/Export, Settings). Drag HUD modules around underneath, right-click one to switch it off.
- **Mod Menu** (Kelp icon) – grid of mod tiles with icon, gear and Enabled/Disabled, category tabs, search, Presets, Reset.
- **Kelp Mods** (shirt icon) – HUD / Gameplay / Crosshair / Waypoints / Social sidebar with a switch per module.

## Modules (29)
HUD: FPS, Coordinates, Direction, Clock, Memory Usage, Biome, Server Info, Ping, Day Counter, Speed, Saturation, Armor Status, Potion Effects, Keystrokes, CPS Counter, Active Modules
Gameplay: Combo Counter, Health Indicator, Full Bright, Remove Hurt Shake, Hide Boss Bar, Auto Sprint, Zoom (hold C)
Crosshair: Custom Crosshair (cross / dot / circle, size, gap, thickness, colour)
Waypoints: Waypoints (add from the pin icon, shown on screen with distance)
Social: Friends Online, Chat Timestamps

Settings are saved to `config/kelpclient.json`.

## Build
Check `gradle.properties` against https://fabricmc.net/develop (Minecraft 1.21.11) – the yarn, loader and Fabric API versions are best guesses.
Then: `./gradlew build` -> `build/libs/kelpclient-1.0.0.jar`
