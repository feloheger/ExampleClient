package com.example.client.module;

import com.example.client.module.settings.Setting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name, description;
    private final Category category;
    private boolean enabled = false;
    private int keybind = GLFW.GLFW_KEY_UNKNOWN;
    protected final MinecraftClient mc = MinecraftClient.getInstance();
    protected final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    public void onEnable()  {}
    public void onDisable() {}
    public void onTick()    {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    protected <T extends Setting<?>> T addSetting(T s) { settings.add(s); return s; }

    public boolean           isEnabled()      { return enabled; }
    public String            getName()        { return name; }
    public String            getDescription() { return description; }
    public Category          getCategory()    { return category; }
    public int               getKeybind()     { return keybind; }
    public void              setKeybind(int k){ this.keybind = k; }
    public List<Setting<?>>  getSettings()    { return settings; }

    public enum Category { RENDER, MOVEMENT, COMBAT, WORLD, MISC }
}
