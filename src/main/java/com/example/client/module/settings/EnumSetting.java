package com.example.client.module.settings;
public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final E[] values;
    public EnumSetting(String name, String desc, E def) {
        super(name, desc, def);
        this.values = def.getDeclaringClass().getEnumConstants();
    }
    public void cycle() { value = values[(value.ordinal()+1) % values.length]; }
    public E[] getValues() { return values; }
}
