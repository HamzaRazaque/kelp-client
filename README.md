# Kelp Client (Fabric, Minecraft 1.21.11)

Open the menu with **Right Shift** in a world. Settings are saved to `config/kelpclient.json`.

## Screens
- **Right Shift menu** – icon bar (Mod Menu, Cosmetics, Waypoints, Social, Import/Export, Settings) over a draggable HUD editor. Right-click a HUD module to switch it off.
- **Mod Menu** – tile grid with icons, gear and Enabled/Disabled, category tabs, search, Presets, Reset. Header buttons open Kelp Mods and Screenshots.
- **Kelp Mods** – category sidebar (HUD, Gameplay, Visual, Crosshair, Waypoints, Cosmetics, Social) with a switch per module.
- **Screenshots** – gallery of every screenshot in your screenshots folder, shown inside Minecraft. Buttons on the title screen and pause menu. Click one for the big view (arrows / scroll to browse, Delete).
- Everything fades in, switches slide, tiles glow on hover.

## Modules
HUD: FPS, Coordinates, Direction, Clock, Memory Usage, Biome Display, Server Information, Ping, Day Counter, Speed Display, Saturation, Armor Durability HUD, Potion Effects HUD, Keystrokes, CPS Counter, Active Modules, Session Timer
Gameplay: Combo Counter, Health Indicator, Hide Boss Bar, Auto Sprint
Visual: Zoom (hold C), Fullbright, Custom Sky, Custom Weather, Custom Time, Motion Blur, Custom Particles, Custom Hurt Camera, Damage Tilt Toggle, View Bobbing Toggle, FOV Changer, Custom Fog
Crosshair: Custom Crosshair
Waypoints: Waypoints
Cosmetics: Particle Trail, Aura, Halo, Wings (particles, client-side)
Social: Friends Online, Chat Timestamps

## Build
Check `gradle.properties` against https://fabricmc.net/develop (Minecraft 1.21.11).
`./gradlew build` -> `build/libs/kelpclient-1.1.0.jar`
