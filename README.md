# Kelp Client (Fabric, Minecraft 1.21.11)

Open the menu by **double-tapping Right Shift** in a world (tap once to close). Settings are saved to `config/kelpclient.json`.

## Screens
- **Right Shift menu** – centred, square tiles (Mods, Cosmetics, Waypoints, Social, Transfer, Settings) that pop in with an animation, over a draggable HUD editor. Right-click a HUD module to switch it off.
- **Mod Menu** – tile grid with icons, gear and Enabled/Disabled, category tabs, search, Presets, Reset. Header buttons open Kelp Mods and Screenshots.
- **Kelp Mods** – category sidebar (HUD, Gameplay, Visual, Crosshair, Waypoints, Cosmetics, Social) with a switch per module.
- **Screenshots** – gallery of every screenshot as sharp square thumbnails (real textures, not a pixel mosaic). Buttons on the title screen and pause menu. Click one for the big view (arrows / scroll to browse, Delete).
- Everything fades in, switches slide, tiles glow on hover.

## Modules
HUD: FPS, Coordinates, Direction, Clock, Memory Usage, Biome Display, Server Information, Ping, Day Counter, Speed Display, Saturation, Armor Durability HUD, Potion Effects HUD, Keystrokes, CPS Counter, Active Modules, Session Timer
Gameplay: Combo Counter, Health Indicator, Hide Boss Bar, Auto Sprint
Visual: Zoom (hold C), Fullbright, Custom Sky, Custom Weather, Custom Time, Motion Blur, Custom Particles, Custom Hurt Camera, Damage Tilt Toggle, View Bobbing Toggle, FOV Changer, Custom Fog
Crosshair: Custom Crosshair
Waypoints: Waypoints
Cosmetics: Dragon Wings, Golden Sword, Portrait Cape, Top Hat, Axolotl Pet, Red Panda (3D, only you see them - press F5), plus Particle Trail, Aura, Halo
Social: Friends Online, Chat Timestamps

## Build
Check `gradle.properties` against https://fabricmc.net/develop (Minecraft 1.21.11).
`./gradlew build` -> `build/libs/kelpclient-1.2.0.jar`
