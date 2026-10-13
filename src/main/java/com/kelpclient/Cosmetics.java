package com.kelpclient;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/** Client-side skin and cape (PNG or animated GIF) shown on your own player. */
public final class Cosmetics {
    private Cosmetics() {}
    static { System.setProperty("java.awt.headless", "true"); }

    private static final Path DIR = FabricLoader.getInstance().getGameDir().resolve("kelpclient").resolve("cosmetics");
    private static boolean loaded;
    private static int counter;
    private static Identifier skinId;
    private static Identifier[] capeIds = new Identifier[0];
    private static int[] capeDelay = new int[0];
    private static int capeTotal = 1, capeW = 64, capeH = 32;

    public static boolean hasSkin() { return skinId != null; }
    public static Identifier skinId() { return skinId; }
    public static boolean hasCape() { return capeIds.length > 0; }
    public static int capeW() { return capeW; }
    public static int capeH() { return capeH; }
    public static Identifier capeTexture() { return capeIds.length == 0 ? null : capeIds[capeIndex()]; }
    public static int capeFrames() { return capeIds.length; }

    private static int capeIndex() {
        if (capeIds.length <= 1) return 0;
        long t = Util.getMeasuringTimeMs() % Math.max(1, capeTotal);
        for (int i = 0; i < capeDelay.length; i++) { t -= capeDelay[i]; if (t < 0) return i; }
        return 0;
    }

    public static void tick(MinecraftClient mc) {
        if (loaded || mc.getTextureManager() == null) return;
        loaded = true;
        var c = KelpConfig.get();
        try { if (!c.skinFile.isEmpty() && Files.exists(DIR.resolve(c.skinFile))) registerSkin(normalizeSkin(ImageIO.read(DIR.resolve(c.skinFile).toFile()))); }
        catch (Exception e) { c.skinFile = ""; }
        try { if (!c.capeFile.isEmpty() && Files.exists(DIR.resolve(c.capeFile))) loadCape(DIR.resolve(c.capeFile)); }
        catch (Exception e) { c.capeFile = ""; }
    }

    /** Called from the player-skin mixin. */
    public static SkinTextures apply(SkinTextures orig) {
        var c = KelpConfig.get();
        boolean s = c.skinOn && skinId != null, cp = c.capeOn && capeIds.length > 0;
        if (!s && !cp) return orig;
        Optional<AssetInfo.TextureAssetInfo> body = s ? Optional.of(new AssetInfo.TextureAssetInfo(skinId, skinId)) : Optional.empty();
        Optional<AssetInfo.TextureAssetInfo> cape = Optional.empty();
        if (cp) { Identifier id = capeIds[capeIndex()]; cape = Optional.of(new AssetInfo.TextureAssetInfo(id, id)); }
        Optional<PlayerSkinType> model = s ? Optional.of(c.slim ? PlayerSkinType.SLIM : PlayerSkinType.WIDE) : Optional.empty();
        return orig.withOverride(new SkinTextures.SkinOverride(body, cape, cape, model));
    }

    // ---------- file picker ----------
    public static String pickFile(String title, String desc, String... patterns) {
        try (MemoryStack st = MemoryStack.stackPush()) {
            PointerBuffer pb = st.mallocPointer(patterns.length);
            for (String p : patterns) pb.put(st.UTF8(p));
            pb.flip();
            return TinyFileDialogs.tinyfd_openFileDialog(title, null, pb, desc, false);
        }
    }

    // ---------- skin ----------
    public static void setSkin(Path src) throws IOException {
        BufferedImage img = ImageIO.read(src.toFile());
        BufferedImage out = normalizeSkin(img);
        Files.createDirectories(DIR);
        ImageIO.write(out, "png", DIR.resolve("skin.png").toFile());
        registerSkin(out);
        var c = KelpConfig.get();
        c.skinFile = "skin.png"; c.skinOn = true;
        KelpConfig.save();
    }
    public static void clearSkin() {
        var c = KelpConfig.get();
        c.skinOn = false; c.skinFile = ""; skinId = null;
        try { Files.deleteIfExists(DIR.resolve("skin.png")); } catch (IOException ignored) {}
        KelpConfig.save();
    }
    private static void registerSkin(BufferedImage out) throws IOException {
        Identifier id = Identifier.of("kelpclient", "cosmetic/skin_" + (counter++));
        var tex = new NativeImageBackedTexture(() -> "kelp skin", toNative(out));
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
        skinId = id;
    }
    private static BufferedImage normalizeSkin(BufferedImage img) throws IOException {
        if (img == null) throw new IOException("That file is not a valid image");
        if (img.getWidth() != 64 || (img.getHeight() != 64 && img.getHeight() != 32))
            throw new IOException("Skin must be 64x64 (or old 64x32), yours is " + img.getWidth() + "x" + img.getHeight());
        BufferedImage out = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics(); g.drawImage(img, 0, 0, null); g.dispose();
        if (img.getHeight() == 32) {
            int[][] cp = {{4,16,16,32,4,4},{8,16,16,32,4,4},{0,20,24,32,4,12},{4,20,16,32,4,12},{8,20,8,32,4,12},{12,20,16,32,4,12},
                          {44,16,-8,32,4,4},{48,16,-8,32,4,4},{40,20,0,32,4,12},{44,20,-8,32,4,12},{48,20,-16,32,4,12},{52,20,-8,32,4,12}};
            for (int[] a : cp) copyMirrored(out, a[0], a[1], a[2], a[3], a[4], a[5]);
        }
        noAlpha(out, 0, 0, 32, 16); noAlpha(out, 0, 16, 64, 32); noAlpha(out, 16, 48, 48, 64);
        return out;
    }
    private static void copyMirrored(BufferedImage im, int x, int y, int ox, int oy, int w, int h) {
        for (int dy = 0; dy < h; dy++) for (int dx = 0; dx < w; dx++) im.setRGB(x + ox + dx, y + oy + dy, im.getRGB(x + (w - 1 - dx), y + dy));
    }
    private static void noAlpha(BufferedImage im, int x0, int y0, int x1, int y1) {
        for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) im.setRGB(x, y, im.getRGB(x, y) | 0xFF000000);
    }

    // ---------- cape ----------
    public static void setCape(Path src) throws IOException {
        String ext = src.getFileName().toString().toLowerCase().endsWith(".gif") ? "gif" : "png";
        loadCape(src);
        Files.createDirectories(DIR);
        Files.deleteIfExists(DIR.resolve("cape.png")); Files.deleteIfExists(DIR.resolve("cape.gif"));
        Files.copy(src, DIR.resolve("cape." + ext), StandardCopyOption.REPLACE_EXISTING);
        var c = KelpConfig.get();
        c.capeFile = "cape." + ext; c.capeOn = true;
        KelpConfig.save();
    }
    public static void clearCape() {
        var c = KelpConfig.get();
        c.capeOn = false; c.capeFile = ""; capeIds = new Identifier[0];
        try { Files.deleteIfExists(DIR.resolve("cape.png")); Files.deleteIfExists(DIR.resolve("cape.gif")); } catch (IOException ignored) {}
        KelpConfig.save();
    }
    private static void loadCape(Path src) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();
        if (src.getFileName().toString().toLowerCase().endsWith(".gif")) readGif(src, frames, delays);
        else {
            BufferedImage img = ImageIO.read(src.toFile());
            if (img == null) throw new IOException("That file is not a valid image");
            if (img.getWidth() == 64 && img.getHeight() > 32 && img.getHeight() % 32 == 0) {
                for (int i = 0; i < Math.min(120, img.getHeight() / 32); i++) { frames.add(img.getSubimage(0, i * 32, 64, 32)); delays.add(100); }
            } else { frames.add(img); delays.add(1000); }
        }
        if (frames.isEmpty()) throw new IOException("No frames found in that file");
        Identifier[] ids = new Identifier[frames.size()];
        int[] dl = new int[frames.size()];
        int total = 0, base = counter++;
        var tm = MinecraftClient.getInstance().getTextureManager();
        for (int i = 0; i < frames.size(); i++) {
            BufferedImage f = normalizeCape(frames.get(i));
            if (i == 0) { capeW = f.getWidth(); capeH = f.getHeight(); }
            ids[i] = Identifier.of("kelpclient", "cosmetic/cape_" + base + "_" + i);
            tm.registerTexture(ids[i], new NativeImageBackedTexture(() -> "kelp cape", toNative(f)));
            dl[i] = Math.max(20, delays.get(i)); total += dl[i];
        }
        capeIds = ids; capeDelay = dl; capeTotal = total;
    }
    private static BufferedImage normalizeCape(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out;
        if (w >= 64 && w == h * 2 && w <= 512) { out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); Graphics2D g = out.createGraphics(); g.drawImage(src, 0, 0, null); g.dispose(); return out; }
        out = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        if (w <= 64 && h <= 32) g.drawImage(src, 0, 0, null);
        else { g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR); g.drawImage(src, 0, 0, 64, 32, null); }
        g.dispose();
        return out;
    }
    private static BufferedImage copy(BufferedImage src) {
        BufferedImage o = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = o.createGraphics(); g.drawImage(src, 0, 0, null); g.dispose();
        return o;
    }
    private static void readGif(Path p, List<BufferedImage> frames, List<Integer> delays) throws IOException {
        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("gif");
        if (!it.hasNext()) throw new IOException("GIF is not supported on this system");
        ImageReader r = it.next();
        try (ImageInputStream in = ImageIO.createImageInputStream(p.toFile())) {
            r.setInput(in, false);
            int n = r.getNumImages(true), cw = 0, ch = 0;
            try {
                IIOMetadataNode sm = (IIOMetadataNode) r.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
                NodeList l = sm.getElementsByTagName("LogicalScreenDescriptor");
                if (l.getLength() > 0) {
                    IIOMetadataNode d = (IIOMetadataNode) l.item(0);
                    cw = Integer.parseInt(d.getAttribute("logicalScreenWidth")); ch = Integer.parseInt(d.getAttribute("logicalScreenHeight"));
                }
            } catch (Exception ignored) {}
            if (cw <= 0 || ch <= 0) { BufferedImage f0 = r.read(0); cw = f0.getWidth(); ch = f0.getHeight(); }
            BufferedImage canvas = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < Math.min(n, 120); i++) {
                BufferedImage f = r.read(i);
                IIOMetadataNode root = (IIOMetadataNode) r.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                int left = 0, top = 0, delay = 10;
                String disp = "none";
                NodeList idn = root.getElementsByTagName("ImageDescriptor");
                if (idn.getLength() > 0) {
                    IIOMetadataNode d = (IIOMetadataNode) idn.item(0);
                    left = Integer.parseInt(d.getAttribute("imageLeftPosition")); top = Integer.parseInt(d.getAttribute("imageTopPosition"));
                }
                NodeList gce = root.getElementsByTagName("GraphicControlExtension");
                if (gce.getLength() > 0) {
                    IIOMetadataNode d = (IIOMetadataNode) gce.item(0);
                    delay = Integer.parseInt(d.getAttribute("delayTime")); disp = d.getAttribute("disposalMethod");
                }
                BufferedImage before = copy(canvas);
                Graphics2D g = canvas.createGraphics(); g.drawImage(f, left, top, null); g.dispose();
                frames.add(copy(canvas)); delays.add(Math.max(2, delay) * 10);
                if (disp.equals("restoreToBackgroundColor")) {
                    Graphics2D g2 = canvas.createGraphics(); g2.setComposite(AlphaComposite.Clear); g2.fillRect(left, top, f.getWidth(), f.getHeight()); g2.dispose();
                } else if (disp.equals("restoreToPrevious")) canvas = before;
            }
        } finally { r.dispose(); }
    }

    private static NativeImage toNative(BufferedImage img) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bo);
        return NativeImage.read(new ByteArrayInputStream(bo.toByteArray()));
    }
}
