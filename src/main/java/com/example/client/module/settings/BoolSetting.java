package com.example.client.module.settings;
public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, String desc, boolean def) { super(name, desc, def); }
    public void toggle() { value = !value; }
}
