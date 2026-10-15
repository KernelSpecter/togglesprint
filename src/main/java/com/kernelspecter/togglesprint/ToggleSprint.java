package com.kernelspecter.togglesprint;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Toggle sprint: tap the sprint key once to keep sprinting, tap again to stop.
 * It just holds the vanilla sprint key down for you, so Minecraft still decides when you can
 * actually sprint (moving forward, enough food, not using an item, not blind).
 */
@Mod(modid = "togglesprint", name = "ToggleSprint", version = "1.0",
     acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class ToggleSprint {
    private final Minecraft mc = Minecraft.getMinecraft();
    private Configuration cfg;
    private boolean toggled = false;
    private boolean showHud = true;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        cfg = new Configuration(new File(e.getModConfigurationDirectory(), "togglesprint.cfg"));
        cfg.load();
        toggled = cfg.getBoolean("toggled", "togglesprint", false, "Sprint toggle state (kept between restarts)");
        showHud = cfg.getBoolean("showHud", "togglesprint", true, "Show [Sprinting (Toggled)] in the top-left corner");
        cfg.save();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        int code = mc.gameSettings.keyBindSprint.getKeyCode();
        if (code > 0 && Keyboard.getEventKey() == code && Keyboard.getEventKeyState()) toggle();
    }

    @SubscribeEvent
    public void onMouse(InputEvent.MouseInputEvent e) {
        int code = mc.gameSettings.keyBindSprint.getKeyCode();
        if (code < 0 && Mouse.getEventButton() == code + 100 && Mouse.getEventButtonState()) toggle();
    }

    private void toggle() {
        if (mc.currentScreen != null || mc.thePlayer == null) return;
        toggled = !toggled;
        if (!toggled) {
            // hand the key back to the real button
            int code = mc.gameSettings.keyBindSprint.getKeyCode();
            boolean down = code < 0 ? Mouse.isButtonDown(code + 100) : code > 0 && Keyboard.isKeyDown(code);
            KeyBinding.setKeyBindState(code, down);
        }
        cfg.get("togglesprint", "toggled", false).set(toggled);
        cfg.save();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.START || !toggled) return;
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        // releasing the key (or a menu closing) clears it; hold it again before the player updates
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text e) {
        if (!toggled || !showHud || mc.gameSettings.showDebugInfo || mc.thePlayer == null) return;
        String text = mc.thePlayer.isSprinting() ? "[Sprinting (Toggled)]" : "[Sprint Toggled]";
        mc.fontRendererObj.drawStringWithShadow(text, 2, 2, 0xFFFFFF);
    }
}
